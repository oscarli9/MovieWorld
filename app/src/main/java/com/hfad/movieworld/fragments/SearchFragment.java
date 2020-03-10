package com.hfad.movieworld.fragments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
public class SearchFragment extends TwitterFragment {

    private String currentHashtag;
    private boolean hashtagFollowed = false;

    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView tweetList = Objects.requireNonNull(SearchFragment.this.getView()).findViewById(R.id.tweetList);

        if (user != null) {
            tweetListener = new TwitterListenerImpl(tweetList, currentUser, callback);
            tweetsAdapter = new TweetListAdapter(user.getUid(), new ArrayList<>());
            tweetsAdapter.setListener(tweetListener);
            tweetList.setLayoutManager(new LinearLayoutManager(tweetList.getContext()));
            tweetList.setAdapter(tweetsAdapter);
            tweetList.addItemDecoration(new DividerItemDecoration(tweetList.getContext(), DividerItemDecoration.VERTICAL));
            SwipeRefreshLayout swipeRefresh = Objects.requireNonNull(SearchFragment.this.getView()).findViewById(R.id.swipeRefresh);
            swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    swipeRefresh.setRefreshing(false);
                    updateList();
                }
            });

            ImageView followHashtag = Objects.requireNonNull(SearchFragment.this.getView()).findViewById(R.id.followHashtag);
            followHashtag.setOnClickListener((imageView) -> {
                followHashtag.setClickable(false);
                if (currentUser != null) {
                    List<String> followed = currentUser.getFollowHashtags();
                    if (hashtagFollowed) {
                        followed.remove(currentHashtag);
                    } else {
                        followed.add(currentHashtag);
                    }
                    firebaseDB.collection(Constants.DATA_USERS).document(user.getUid()).update(Constants.DATA_USER_HASHTAGS, followed)
                            .addOnSuccessListener((it) -> {
                                callback.onUserUpdated();
                                followHashtag.setClickable(true);
                            })
                            .addOnFailureListener(e -> {
                                e.printStackTrace();
                                followHashtag.setClickable(true);
                            });
                }
            });
        }
    }

    public void newHashtag(String term) {
        currentHashtag = term;
        ImageView followHashtag = Objects.requireNonNull(SearchFragment.this.getView()).findViewById(R.id.followHashtag);
        followHashtag.setVisibility(View.VISIBLE);
        updateList();
    }

    public void updateList() {
        RecyclerView tweetList = Objects.requireNonNull(SearchFragment.this.getView()).findViewById(R.id.tweetList);
        tweetList.setVisibility(View.GONE);
        if (currentHashtag != null) {
            firebaseDB.collection(Constants.DATA_TWEETS).whereArrayContains(Constants.DATA_TWEET_HASHTAGS, currentHashtag).get()
                    .addOnSuccessListener((list) -> {
                        tweetList.setVisibility(View.VISIBLE);
                        ArrayList<Tweet> tweets = new ArrayList<>();
                        for (DocumentSnapshot document : list.getDocuments()) {
                            Tweet tweet = document.toObject(Tweet.class);
                            if (tweet != null) {
                                tweets.add(tweet);
                            }
                        }
                        Collections.sort(tweets, new SortByTimeStamp());
                        tweetsAdapter.updateTweets(tweets);
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                    });
            updateFollowDrawable();
        }
    }

    private void updateFollowDrawable() {
        if (currentUser != null) {
            hashtagFollowed = currentUser.followHashtags.contains(currentHashtag);
            ImageView followHashtag = Objects.requireNonNull(SearchFragment.this.getView()).findViewById(R.id.followHashtag);
            Context context = Objects.requireNonNull(SearchFragment.this.getContext());
            if (hashtagFollowed) {
                followHashtag.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.follow));
            } else {
                followHashtag.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.follow_inactive));
            }
        }
    }
}
