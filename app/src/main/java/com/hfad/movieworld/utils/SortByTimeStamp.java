package com.hfad.movieworld.utils;

import java.util.Comparator;

public class SortByTimeStamp implements Comparator<Tweet> {
    @Override
    public int compare(Tweet o1, Tweet o2) {
        return (int)(o2.getTimestamp() - o1.getTimestamp());
    }
}
