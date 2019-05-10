package com.enode.common.utilities;

public class Ensure {
    public static <T> void notNull(T argument, String argumentName) {
        if (argument == null) {
            throw new IllegalArgumentException(argumentName + " should not be null.");
        }
    }

    public static void notNullOrEmpty(String argument, String argumentName) {
        if (argument == null || "".equals(argument.trim())) {
            throw new IllegalArgumentException(argumentName + " should not be null or empty.");
        }
    }

    public static void positive(int number, String argumentName) {
        if (number <= 0) {
            throw new IllegalArgumentException(argumentName + " should be positive.");
        }
    }

    public static void positive(long number, String argumentName) {
        if (number <= 0) {
            throw new IllegalArgumentException(argumentName + " should be positive.");
        }
    }

    public static void nonNegative(long number, String argumentName) {
        if (number < 0) {
            throw new IllegalArgumentException(argumentName + " should be non negative.");
        }
    }

    public static void nonNegative(int number, String argumentName) {
        if (number < 0) {
            throw new IllegalArgumentException(argumentName + " should be non negative.");
        }
    }

    public static void equal(int expected, int actual, String argumentName) {
        if (expected != actual) {
            throw new IllegalArgumentException(String.format("%s expected value: %d, actual value: %d", argumentName, expected, actual));
        }
    }

    public static void equal(long expected, long actual, String argumentName) {
        if (expected != actual) {
            throw new IllegalArgumentException(String.format("%s expected value: %d, actual value: %d", argumentName, expected, actual));
        }
    }

    public static void equal(boolean expected, boolean actual, String argumentName) {
        if (expected != actual) {
            throw new IllegalArgumentException(String.format("%s expected value: %d, actual value: %d", argumentName, expected, actual));
        }
    }
}
