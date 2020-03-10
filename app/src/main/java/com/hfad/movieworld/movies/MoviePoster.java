package com.hfad.movieworld.movies;

import com.google.gson.annotations.SerializedName;

public class MoviePoster {
    @SerializedName("title")
    private String title;
    @SerializedName("poster_path")
    private String posterPath;

    public MoviePoster(String title, String posterPath) {
        this.title = title;
        this.posterPath = posterPath;
    }

    public String getTitle() {
        return title;
    }

    public String getPosterPath() {
        return posterPath;
    }
}
