package com.simiyutin.javaii.server;

import java.util.*;

public class SortAlgorithm {
    public static long sort(List<Integer> array) {
        long start = System.currentTimeMillis();

        for (int i = 0; i < array.size() - 1; i++) {
            boolean sorted = true;
            for (int j = 0; j < array.size() - 1 - i; j++) {
                if (array.get(j) > array.get(j + 1)) {
                    Collections.swap(array, j, j + 1);
                    sorted = false;
                }
            }
            if (sorted) {
                break;
            }
        }

        long end = System.currentTimeMillis();
        return end - start;
    }
    public static void checkSorted(List<Integer> originalArray, List<Integer> actualSortedArray) {
        List<Integer> expectedSortedArray = new ArrayList<>(originalArray);
        expectedSortedArray.sort(Comparator.comparing(Integer::intValue));
        if (!expectedSortedArray.equals(actualSortedArray)) {
            throw new AssertionError("client: got wrong response from server!");
        }
    }
}
