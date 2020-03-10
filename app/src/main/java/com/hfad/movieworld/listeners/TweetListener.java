package com.hfad.movieworld.listeners;

import com.hfad.movieworld.utils.Tweet;

public interface TweetListener {
    void onLayoutClick(Tweet tweet);
    void onLike(Tweet tweet);
    void onRetweet(Tweet tweet);
}
