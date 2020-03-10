package com.hfad.movieworld.utils;

import java.util.List;

public class Tweet {
    private String tweetId;
    private List<String> userIds;
    private String username;
    private String text;
    private String imageUrl;
    private Long timestamp;
    private List<String> hashTags;
    private List<String> likes;

    public Tweet() {

    }

    public Tweet(String tweetId, List<String> userIds, String username, String text, String imageUrl, Long timestamp,
                 List<String> hashTags, List<String> likes) {
        this.tweetId = tweetId;
        this.userIds = userIds;
        this.username = username;
        this.text = text;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.hashTags = hashTags;
        this.likes = likes;
    }

    public void setTweetId(String tweetId) { this.tweetId = tweetId; }
    public void setUserIds(List<String> userIds) { this.userIds = userIds; }
    public void setUsername(String username) { this.username = username; }
    public void setText(String text) { this.text = text; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    public void setHashTags(List<String> hashTags) { this.hashTags = hashTags; }
    public void setLikes(List<String> likes) { this.likes = likes; }

    public String getTweetId() { return this.tweetId; }
    public List<String> getUserIds() { return this.userIds; }
    public String getUsername() { return this.username; }
    public String getText() { return this.text; }
    public String getImageUrl() { return this.imageUrl; }
    public Long getTimestamp() { return this.timestamp; }
    public List<String> getHashTags() { return this.hashTags; }
    public List<String> getLikes() { return this.likes; }
}
