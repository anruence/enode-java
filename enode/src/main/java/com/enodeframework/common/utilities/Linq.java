package com.enodeframework.common.utilities;

import com.enodeframework.common.exception.ENodeRuntimeException;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * List常用操作类
 */
public class Linq {
    public static <T> T first(List<T> list) {
        if (list == null || list.size() < 1) {
            throw new ArrayIndexOutOfBoundsException("");
        }
        return list.get(0);
    }

    public static <T> T last(List<T> list) {
        if (list == null || list.size() < 1) {
            throw new ArrayIndexOutOfBoundsException("");
        }
        return list.get(list.size() - 1);
    }

    public static <T> T firstOrDefault(List<T> list) {
        if (list == null || list.size() < 1) {
            return null;
        }
        return list.get(0);
    }

    public static <T> T single(List<T> list, Predicate<T> predicate) {
        List<T> rets = list.stream().filter(predicate).collect(Collectors.toList());
        if (rets.size() == 1) {
            return rets.get(0);
        }
        if (rets.size() == 0) {
            throw new ENodeRuntimeException("not found");
        } else {
            throw new ENodeRuntimeException("find more than one result");
        }
    }

    public static <T> T singleOrDefault(List<T> list, Predicate<T> predicate) {
        List<T> rets = list.stream().filter(predicate).collect(Collectors.toList());
        if (rets.size() == 1) {
            return rets.get(0);
        }
        if (rets.size() == 0) {
            return null;
        } else {
            throw new ENodeRuntimeException("find more than one result");
        }
    }
}
