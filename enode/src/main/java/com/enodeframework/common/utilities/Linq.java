package com.enodeframework.common.utilities;


import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * List常用操作类
 *
 * @author anruence@gmail.com
 */
public class Linq {
    public static <T> T first(List<T> tList) {
        Optional<T> optional = tList.stream().findFirst();
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("first element not exist, ensure the args");
        }
        return optional.get();
    }

    public static <T> T single(List<T> tList, Predicate<T> predicate) {
        List<T> collect = tList.stream().filter(predicate).collect(Collectors.toList());
        if (collect.size() == 1) {
            return collect.get(0);
        }
        if (collect.size() == 0) {
            throw new IllegalArgumentException("not found");
        }
        throw new IllegalArgumentException("find more than one result");
    }

    public static <T> T singleOrDefault(List<T> tList, Predicate<T> predicate) {
        List<T> collect = tList.stream().filter(predicate).collect(Collectors.toList());
        if (collect.size() == 1) {
            return collect.get(0);
        }
        if (collect.size() == 0) {
            return null;
        }
        throw new IllegalArgumentException("find more than one result");
    }
}
