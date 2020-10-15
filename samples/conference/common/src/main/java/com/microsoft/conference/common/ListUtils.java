package com.microsoft.conference.common;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Collection utilities.
 */
public class ListUtils {

    /**
     * Make collection as stream.
     *
     * @param source source
     * @param <T>    element type
     * @return stream
     */
    public static <T> Stream<T> stream(Collection<T> source) {
        return source != null ? source.stream() : Stream.empty();
    }

    /**
     * Make collection as parallel stream.
     *
     * @param source source
     * @param <T>    element type
     * @return stream
     */
    public static <T> Stream<T> parallelStream(Collection<T> source) {
        return source != null ? source.parallelStream() : Stream.empty();
    }

    /**
     * Map source list to target-typed list.
     *
     * @param source source
     * @param mapper mapper
     * @param <S>    source type
     * @param <T>    target type
     * @return mapped list
     */
    public static <S, T> List<T> map(Collection<S> source, Function<S, T> mapper) {
        return stream(source).map(mapper).collect(toList());
    }

    /**
     * Filter list.
     *
     * @param source    source
     * @param predicate determine the element to keep
     * @param <T>       source type
     * @return filtered list
     */
    public static <T> List<T> filter(Collection<T> source, Predicate<T> predicate) {
        return stream(source).filter(predicate).collect(toList());
    }

    /**
     * Remove element that match predication.
     *
     * @param source    source
     * @param predicate determine the element to omit
     * @param <T>       source type
     * @return filtered list
     */
    public static <T> List<T> omit(Collection<T> source, Predicate<T> predicate) {
        return stream(source).filter(predicate.negate()).collect(toList());
    }

    /**
     * Distinct collection.
     *
     * @param source source
     * @param <T>    type
     * @return distinct list
     */
    public static <T> List<T> distinct(Collection<T> source) {
        return stream(source).distinct().collect(toList());
    }

    /**
     * Group source collection by key.
     *
     * @param source      source
     * @param classifier  classify source element by key
     * @param valueMapper map source element to target type
     * @param <S>         source type
     * @param <K>         group key type
     * @param <V>         group value type
     */
    public static <S, K, V> Map<K, List<V>> group(
            Collection<S> source, Function<S, K> classifier, Function<S, V> valueMapper) {
        return stream(source).collect(
                groupingBy(classifier, Collectors.mapping(valueMapper, toList())));
    }

    /**
     * Merge source collection with grouped elements into list.
     *
     * @param source      source
     * @param mapper      grouped elements to merge
     * @param keyProvider key relation between source and grouped elements
     * @param combiner    combine source and element
     * @param <S>         source type
     * @param <T>         target type
     * @param <K>         key relation type
     * @param <V>         grouped value type
     * @return merged list
     */
    public static <S, T, K, V> List<T> merge(
            Collection<S> source, Map<K, V> mapper, Function<S, K> keyProvider, BiFunction<S, V, T> combiner) {
        return stream(source).map(it -> {
            K key = keyProvider.apply(it);
            V value = key != null ? mapper.getOrDefault(key, null) : null;
            return combiner.apply(it, value);
        }).collect(toList());
    }

    /**
     * Merge to collection into list.
     *
     * @return merged list
     */
    public static <T1, T2, R, K> List<R> merge(
            Collection<T1> source1, Function<T1, K> keyProviderForSource1,
            Collection<T2> source2, Function<T2, K> keyProviderForSource2,
            BiFunction<T1, T2, R> combiner) {
        Map<K, T2> source2Map = stream(source2).collect(toMap(keyProviderForSource2, identity(), (a, b) -> a));
        return stream(source1)
                .map(s1 -> {
                    K key = keyProviderForSource1.apply(s1);
                    T2 s2 = source2Map.getOrDefault(key, null);
                    return combiner.apply(s1, s2);
                })
                .collect(toList());
    }

    /**
     * Put source collection into hash map, which will drop duplicated by key.
     *
     * @param source    source
     * @param keyMapper key mapper
     * @param <K>       key type
     * @param <V>       value type
     * @return new hash map
     */
    public static <K, V> Map<K, V> toHashMap(Collection<V> source, Function<V, K> keyMapper) {
        return stream(source)
                .filter(Objects::nonNull)
                .collect(toMap(keyMapper, identity(), (v, v2) -> v));
    }

    /**
     * Put source collection into hash map, which will drop duplicated by key.
     *
     * @param source      source
     * @param keyMapper   key mapper
     * @param valueMapper value mapper
     * @param <K>         key type
     * @param <V>         value type
     * @param <R>         result value type
     * @return new hash map
     */
    public static <K, V, R> Map<K, R> toHashMap(Collection<V> source, Function<V, K> keyMapper, Function<V, R> valueMapper) {
        return stream(source)
                .filter(Objects::nonNull)
                .collect(toMap(keyMapper, valueMapper, (v, v2) -> v));
    }

    /**
     * Put source collection into set.
     *
     * @param source source
     * @param mapper key mapper
     * @param <S>    source type
     * @param <T>    type mapper
     * @return new set
     */
    public static <S, T> Set<T> toSet(Collection<S> source, Function<S, T> mapper) {
        return stream(source).map(mapper).collect(Collectors.toSet());
    }

    /**
     * Determine whether the source map is null or empty.
     *
     * @param source source
     */
    public static boolean isEmpty(Map<?, ?> source) {
        return source == null || source.isEmpty();
    }

    /**
     * Determine whether the source map is null or empty.
     *
     * @param source source
     */
    public static boolean isEmpty(Collection<?> source) {
        return source == null || source.isEmpty();
    }

    /**
     * Determine whether the source map is not not null and not empty.
     *
     * @param source source
     */
    public static boolean isNotEmpty(Map<?, ?> source) {
        return source != null && !source.isEmpty();
    }

    /**
     * Determine whether source collection is is not not null and not empty.
     *
     * @param source source
     */
    public static boolean isNotEmpty(Collection<?> source) {
        return source != null && !source.isEmpty();
    }

    /**
     * Determine whether source collection is has given size.
     *
     * @param source source
     * @param size   size
     * @param <T>    type
     */
    public static <T> boolean hasSize(Collection<T> source, int size) {
        return source != null && source.size() == size;
    }

    /**
     * Determine whether given value is in source collection.
     *
     * @param value  value
     * @param source source
     * @param <T>    type
     */
    public static <T> boolean notIn(T value, Collection<T> source) {
        return !source.contains(value);
    }

    /**
     * High order function for not in prediction.
     *
     * @param source source
     * @param <T>    type
     * @return predict function for not in.
     */
    public static <T> Predicate<T> notIn(Collection<T> source) {
        return (value) -> notIn(value, source);
    }

    /**
     * Try get value at index, or return default value for index out of range.
     *
     * @param source       source
     * @param index        index
     * @param defaultValue default value
     * @param <V>          type
     * @return value at index, or default value for exception.
     */
    public static <V> V getOrDefault(List<V> source, int index, V defaultValue) {
        try {
            return source != null && index >= 0 && index < source.size() ? source.get(index) : defaultValue;
        } catch (Throwable err) {
            return defaultValue;
        }
    }

    /**
     * Try get value at index, or return null for index out of range.
     *
     * @param source source
     * @param index  index
     * @param <V>    type
     * @return value at index, or null for exception.
     */
    public static <V> V getOrNull(List<V> source, int index) {
        return getOrDefault(source, index, null);
    }

    /**
     * High order function for collection mapping.
     *
     * @param transform transform source element to target element
     * @param <S>       source element type
     * @param <T>       target element type
     * @return function map source collection to target
     */
    public static <S, T> Function<Collection<S>, List<T>> mapTo(Function<S, T> transform) {
        return (source) -> map(source, transform);
    }
}
