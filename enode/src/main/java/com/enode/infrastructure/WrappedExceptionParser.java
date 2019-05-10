package com.enode.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WrappedExceptionParser<T extends Throwable> {

    private T exception;
    private List<Class<? extends Throwable>> expectExceptionTypes;
    private boolean disrupt;
    private When UNDO = new UndoWhen();

    private WrappedExceptionParser(T e) {
        this.exception = e;
        expectExceptionTypes = new ArrayList<>();
    }

    public static <T extends Throwable> WrappedExceptionParser<T> create(T e) {
        return new WrappedExceptionParser(e instanceof WrappedRuntimeException ? ((WrappedRuntimeException) e).getException() : e);
    }

    public static WrappedExceptionParser<Exception> create(WrappedRuntimeException wrappedExp) {
        return new WrappedExceptionParser(wrappedExp.getException());
    }

    public <ExpectType extends Throwable> When<ExpectType, T> when(Class<ExpectType> expectTypes) {
        if (!disrupt) {
            return new WhenImpl<>(expectTypes, this);
        }

        return UNDO;
    }

    public WrappedExceptionParser<T> elze(Consumer<T> consumer) {
        if (!this.disrupt) {
            this.disrupt = true;
            consumer.accept(this.exception);
        }
        return this;
    }

    static class WhenImpl<ExpectType extends Throwable, OrigType extends Throwable> implements When<ExpectType, OrigType> {

        private Class<ExpectType> expectExceptionType;
        private WrappedExceptionParser<OrigType> parser;

        public WhenImpl(Class<ExpectType> expectExceptionType, WrappedExceptionParser<OrigType> parser) {
            this.expectExceptionType = expectExceptionType;
            this.parser = parser;
        }

        @Override
        public WrappedExceptionParser<OrigType> then(Consumer<ExpectType> consumer) {
            if (parser.exception.getClass() == expectExceptionType) {
                parser.disrupt = true;
                consumer.accept((ExpectType) parser.exception);
            }

            return parser;
        }
    }

    class UndoWhen implements When {
        @Override
        public WrappedExceptionParser then(Consumer consumer) {
            return WrappedExceptionParser.this;
        }
    }
}
