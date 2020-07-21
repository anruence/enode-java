package com.microsoft.conference.common;


import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author anruence@gmail.com
 */
public class Linq {

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
