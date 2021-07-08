package org.enodeframework.common.utils;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Assert {

    private Assert() {
        // utility class
    }

    public static <T> void nonNull(T expression, String message) {
        if (expression == null) {
            throw new IllegalArgumentException(String.format("%s should not be null.", message));
        }
    }

    /**
     * Asserts that the value of {@code state} is true. If not, an IllegalStateException is thrown.
     *
     * @param state           the state validation expression
     * @param messageSupplier Supplier of the exception message if state evaluates to false
     */
    public static void state(boolean state, Supplier<String> messageSupplier) {
        if (!state) {
            throw new IllegalStateException(messageSupplier.get());
        }
    }

    /**
     * Asserts that the given {@code expression} is true. If not, an IllegalArgumentException is thrown.
     *
     * @param expression      the state validation expression
     * @param messageSupplier Supplier of the exception message if the expression evaluates to false
     */
    public static void isTrue(boolean expression, Supplier<String> messageSupplier) {
        if (!expression) {
            throw new IllegalArgumentException(messageSupplier.get());
        }
    }

    /**
     * Asserts that the given {@code expression} is false. If not, an IllegalArgumentException is thrown.
     *
     * @param expression      the state validation expression
     * @param messageSupplier Supplier of the exception message if the expression evaluates to true
     */
    public static void isFalse(boolean expression, Supplier<String> messageSupplier) {
        if (expression) {
            throw new IllegalArgumentException(messageSupplier.get());
        }
    }

    /**
     * Assert that the given {@code value} is not {@code null}. If not, an IllegalArgumentException is
     * thrown.
     *
     * @param value           the value not to be {@code null}
     * @param messageSupplier Supplier of the exception message if the assertion fails
     */
    public static void notNull(Object value, Supplier<String> messageSupplier) {
        isTrue(value != null, messageSupplier);
    }

    /**
     * Assert that the given {@code value} is not {@code null}. If not, an IllegalArgumentException is
     * thrown.
     *
     * @param value           the value not to be {@code null}
     * @param messageSupplier Supplier of the exception message if the assertion fails
     * @return the provided {@code value}
     */
    public static <T> T nonNull(T value, Supplier<String> messageSupplier) {
        isTrue(value != null, messageSupplier);
        return value;
    }

    /**
     * Assert that the given {@code value} will result to {@code true} through the {@code assertion} {@link Predicate}.
     * If not, the {@code exceptionSupplier} provides an exception
     * to be thrown.
     *
     * @param value             a {@code T} specifying the value to assert
     * @param assertion         a {@link Predicate} to test {@code value} against
     * @param exceptionSupplier a {@link Supplier} of the exception {@code X} if {@code assertion} evaluates to false
     * @param <T>               a generic specifying the type of the {@code value}, which is the input for the
     *                          {@code assertion}
     * @param <X>               a generic extending {@link Throwable} which will be provided by the
     *                          {@code exceptionSupplier}
     * @throws X if the {@code value} asserts to {@code false} by the {@code assertion}
     */
    @SuppressWarnings("RedundantThrows") // Throws signature required for correct compilation
    public static <T, X extends Throwable> void assertThat(T value,
                                                           Predicate<T> assertion,
                                                           Supplier<? extends X> exceptionSupplier) throws X {
        if (!assertion.test(value)) {
            throw exceptionSupplier.get();
        }
    }

    /**
     * Assert that the given {@code value} is non null. If not, the {@code exceptionSupplier} provides an exception to
     * be thrown.
     *
     * @param value             a {@code T} specifying the value to assert
     * @param exceptionSupplier a {@link Supplier} of the exception {@code X} if {@code value} equals null
     * @param <T>               a generic specifying the type of the {@code value}, which is the input for the
     *                          {@code assertion}
     * @param <X>               a generic extending {@link Throwable} which will be provided by the
     *                          {@code exceptionSupplier}
     * @throws X if the {@code value} equals {@code null}
     */
    public static <T, X extends Throwable> void assertNonNull(T value,
                                                              Supplier<? extends X> exceptionSupplier) throws X {
        assertThat(value, Objects::nonNull, exceptionSupplier);
    }
}
