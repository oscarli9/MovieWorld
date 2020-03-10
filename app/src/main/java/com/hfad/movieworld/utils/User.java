package com.hfad.movieworld.utils;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class User {
    @SerializedName("email")
    public String email;

    @SerializedName("username")
    public String username;

    @SerializedName("imageUrl")
    public String imageUrl;

    @SerializedName("followHashtags")
    public List<String> followHashtags;

    @SerializedName("followUsers")
    public List<String> followUsers;

    public User() {

    }

    public User(String email, String username, String imageUrl, List<String> followHashtags, List<String> followUsers) {
        this.email = email;
        this.username = username;
        this.imageUrl = imageUrl;
        this.followHashtags = followHashtags;
        this.followUsers = followUsers;
    }

    public final String getEmail() { return email; }
    public final String getUsername() { return username; }
    public final String getImageUrl() { return imageUrl; }
    public final List<String> getFollowHashtags() { return followHashtags; }
    public final List<String> getFollowUsers() { return followUsers; }
}
