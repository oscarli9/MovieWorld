package com.hfad.movieworld.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.DocumentSnapshot;
import com.hfad.movieworld.R;
import com.hfad.movieworld.adapters.TweetListAdapter;
import com.hfad.movieworld.listeners.TwitterListenerImpl;
import com.hfad.movieworld.utils.Constants;
import com.hfad.movieworld.utils.SortByTimeStamp;
import com.hfad.movieworld.utils.Tweet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends TwitterFragment {


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateList();

        if (user != null) {
            RecyclerView tweetList = Objects.requireNonNull(HomeFragment.this.getView()).findViewById(R.id.tweetList);
            tweetListener = new TwitterListenerImpl(tweetList, currentUser, callback);
            tweetsAdapter = new TweetListAdapter(user.getUid(), new ArrayList<>());
            tweetsAdapter.setListener(tweetListener);
            tweetList.setLayoutManager(new LinearLayoutManager(tweetList.getContext()));
            tweetList.setAdapter(tweetsAdapter);
            tweetList.addItemDecoration(new DividerItemDecoration(tweetList.getContext(), DividerItemDecoration.VERTICAL));

            SwipeRefreshLayout swipeRefresh = Objects.requireNonNull(HomeFragment.this.getView()).findViewById(R.id.swipeRefresh);
            swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    swipeRefresh.setRefreshing(false);
                    updateList();
                }
            });
        }
    }

    @Override
    public void updateList() {
        RecyclerView tweetList = Objects.requireNonNull(HomeFragment.this.getView()).findViewById(R.id.tweetList);
        tweetList.setVisibility(View.GONE);

        if (currentUser != null) {
            List<Tweet> tweets = new ArrayList<>();

            for (String hashtag : currentUser.followHashtags) {
                firebaseDB.collection(Constants.DATA_TWEETS).whereArrayContains(Constants.DATA_TWEET_HASHTAGS, hashtag).get()
                        .addOnSuccessListener((list) -> {
                            for (DocumentSnapshot document : list.getDocuments()) {
                                Tweet tweet = document.toObject(Tweet.class);
                                if (tweet != null) {
                                    tweets.add(tweet);
                                }
                                updateAdapter(tweets);
                                tweetList.setVisibility(View.VISIBLE);
                            }
                        })
                        .addOnFailureListener(e -> {
                            e.printStackTrace();
                            tweetList.setVisibility(View.VISIBLE);
                        });
            }

            for (String followedUser : currentUser.followUsers) {
                firebaseDB.collection(Constants.DATA_TWEETS).whereArrayContains(Constants.DATA_TWEET_USER_IDS, followedUser).get()
                        .addOnSuccessListener((list) -> {
                            for (DocumentSnapshot document : list.getDocuments()) {
                                Tweet tweet = document.toObject(Tweet.class);
                                if (tweet != null) {
                                    tweets.add(tweet);
                                }
                                updateAdapter(tweets);
                                tweetList.setVisibility(View.VISIBLE);
                            }
                        })
                        .addOnFailureListener(e -> {
                            e.printStackTrace();
                            tweetList.setVisibility(View.VISIBLE);
                        });
            }
        }
    }

    private void updateAdapter(List<Tweet> tweets) {
        Collections.sort(tweets, new SortByTimeStamp());
        tweetsAdapter.updateTweets(removeDuplicates(tweets));
    }

    private List<Tweet> removeDuplicates(List<Tweet> originalList) {
        List<Tweet> tweets = new ArrayList<>();
        List<String> tweetIds = new ArrayList<>();
        for (Tweet tweet : originalList) {
            String tweetId = tweet.getTweetId();
            if (!tweetIds.contains(tweetId)) {
                tweetIds.add(tweetId);
                tweets.add(tweet);
            }
        }
        return tweets;
    }

}
