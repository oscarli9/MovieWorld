package com.hfad.movieworld.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hfad.movieworld.R;
import com.hfad.movieworld.listeners.TweetListener;
import com.hfad.movieworld.movies.MovieApiService;
import com.hfad.movieworld.movies.MovieMap;
import com.hfad.movieworld.movies.MoviePoster;
import com.hfad.movieworld.utils.Tweet;
import com.hfad.movieworld.utils.Util;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TweetListAdapter extends RecyclerView.Adapter<TweetListAdapter.TweetViewHolder> {
    private String userId;
    private List<Tweet> tweets;
    private TweetListener listener;
    private static HashMap<String, Integer> movieMap = null;
    private static Retrofit retrofit = null;
    private final static String TAG = TweetListAdapter.class.getSimpleName();
    private final static String BASE_URL = "https://api.themoviedb.org/3/";
    private final static String API_KEY = "2ae11ada89bb0991f85cb8e1f6027d41";

    public TweetListAdapter(@NonNull String userId, @NonNull List<Tweet> tweets) {
        super();
        this.userId = userId;
        this.tweets = tweets;
    }

    public void setListener(TweetListener listener) {
        this.listener = listener;
    }

    public void updateTweets(List<Tweet> newTweets) {
        tweets.clear();
        tweets.addAll(newTweets);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TweetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tweet, parent, false);
        return new TweetListAdapter.TweetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TweetViewHolder holder, int position) {
        holder.bind(userId, tweets.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    public static class TweetViewHolder extends RecyclerView.ViewHolder {

        private ViewGroup layout;
        private TextView username;
        private TextView text;
        private ImageView image;
        private TextView date;
        private ImageView like;
        private TextView likeCount;
        private ImageView retweet;
        private TextView retweetCount;

        TweetViewHolder(View view) {
            super(view);
            this.layout = view.findViewById(R.id.tweetLayout);
            this.username = view.findViewById(R.id.tweetUsername);
            this.text = view.findViewById(R.id.tweetText);
            this.image = view.findViewById(R.id.tweetImage);
            this.date = view.findViewById(R.id.tweetDate);
            this.like = view.findViewById(R.id.tweetLike);
            this.likeCount = view.findViewById(R.id.tweetLikeCount);
            this.retweet = view.findViewById(R.id.tweetRetweet);
            this.retweetCount = view.findViewById(R.id.tweetRetweetCount);
        }

        public void bind(String userId, Tweet tweet, TweetListener listener) {
            username.setText(tweet.getUsername());
            text.setText(tweet.getText());
            if (tweet.getImageUrl() == null || tweet.getImageUrl().isEmpty()) {
                if (tweet.getHashTags() != null && tweet.getHashTags().size() > 0) {
                    String movieTitle = tweet.getHashTags().get(0).toLowerCase();

                    if (movieMap == null) {
                        movieMap = MovieMap.movieMap;
                    }

                    if (movieMap.containsKey(movieTitle)) {
                        image.setVisibility(View.VISIBLE);

                        if (retrofit == null) {
                            retrofit = new Retrofit.Builder()
                                    .baseUrl(BASE_URL)
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build();
                        }

                        MovieApiService movieApiService = retrofit.create(MovieApiService.class);

                        if (movieMap.get(movieTitle) != null) {
                            int movieId = movieMap.get(movieTitle);
                            Call<MoviePoster> call = movieApiService.getMoviePoster(movieId, API_KEY);
                            call.enqueue(new Callback<MoviePoster>() {
                                @Override
                                public void onResponse(Call<MoviePoster> call, Response<MoviePoster> response) {
                                    assert response.body() != null;
                                    String path = response.body().getPosterPath();
                                    String url = "https://image.tmdb.org/t/p/w500" + path;
                                    Picasso.get().load(url).into(image);
                                }

                                @Override
                                public void onFailure(Call<MoviePoster> call, Throwable t) {
                                    Log.e(TAG, t.toString());
                                }
                            });
                        }
                    } else image.setVisibility(View.GONE);
                } else image.setVisibility(View.GONE);
            } else {
                image.setVisibility(View.VISIBLE);
                Util.loadUrl(image, tweet.getImageUrl(), R.drawable.logo);
            }
            date.setText(Util.getDate(tweet.getTimestamp()));
            likeCount.setText(String.valueOf(tweet.getLikes().size()));
            retweetCount.setText(String.valueOf(tweet.getUserIds().size() - 1));

            layout.setOnClickListener((it) -> {
                listener.onLayoutClick(tweet);
            });
            like.setOnClickListener((it) -> {
                listener.onLike(tweet);
            });
            retweet.setOnClickListener((it) -> {
                listener.onRetweet(tweet);
            });

            if (tweet.getLikes().contains(userId)) {
                like.setImageDrawable(ContextCompat.getDrawable(like.getContext(), R.drawable.like));
            } else {
                like.setImageDrawable(ContextCompat.getDrawable(like.getContext(), R.drawable.like_inactive));
            }

            if (tweet.getUserIds().get(0).equals(userId)) {
                retweet.setImageDrawable(ContextCompat.getDrawable(retweet.getContext(), R.drawable.original));
                retweet.setClickable(false);
            } else if (tweet.getUserIds().contains(userId)) {
                retweet.setImageDrawable(ContextCompat.getDrawable(retweet.getContext(), R.drawable.retweet));
            } else {
                retweet.setImageDrawable(ContextCompat.getDrawable(retweet.getContext(), R.drawable.retweet_inactive));
            }
        }
    }
}
