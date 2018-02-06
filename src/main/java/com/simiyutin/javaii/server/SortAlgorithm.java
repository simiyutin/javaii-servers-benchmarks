package com.simiyutin.javaii.server;

import java.util.*;

public class SortAlgorithm {
    public static void sort(List<Integer> array) {
        array.sort(Comparator.comparing(Integer::intValue));
    }
    public static void checkSorted(List<Integer> originalArray, List<Integer> actualSortedArray) {
        List<Integer> expectedSortedArray = new ArrayList<>(originalArray);
        expectedSortedArray.sort(Comparator.comparing(Integer::intValue));
        if (!expectedSortedArray.equals(actualSortedArray)) {
            throw new AssertionError("azaza lalka");
        }
    }
}
