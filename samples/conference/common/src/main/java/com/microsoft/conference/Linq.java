package com.microsoft.conference;

import com.enodeframework.infrastructure.WrappedRuntimeException;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 获取List中符合条件的唯一记录
 */
public class Linq {

    public static <T> T Single(List<T> list, Predicate<T> predicate) {
        List<T> rets = list.stream().filter(predicate).collect(Collectors.toList());
        if (rets.size() == 1) {
            return rets.get(0);
        }
        if (rets.size() == 0) {
            throw new WrappedRuntimeException("not found");
        } else {
            throw new WrappedRuntimeException("find more than one result");
        }
    }

    public static <T> T SingleOrDefault(List<T> list, Predicate<T> predicate) {
        List<T> rets = list.stream().filter(predicate).collect(Collectors.toList());

        if (rets.size() == 1) {
            return rets.get(0);
        }
        if (rets.size() == 0) {
            return null;
        } else {
            throw new WrappedRuntimeException("find more than one result");
        }
    }
}
