package com.simiyutin.javaii.server;

import java.util.Comparator;
import java.util.List;

public class SortAlgorithm {
    public static void sort(List<Integer> array) {
        array.sort(Comparator.comparing(Integer::intValue));
    }
}
