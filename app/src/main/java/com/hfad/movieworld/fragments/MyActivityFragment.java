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
public class MyActivityFragment extends TwitterFragment {


    public MyActivityFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateList();
        RecyclerView tweetList = Objects.requireNonNull(MyActivityFragment.this.getView()).findViewById(R.id.tweetList);

        if (user != null) {
            tweetListener = new TwitterListenerImpl(tweetList, currentUser, callback);
            tweetsAdapter = new TweetListAdapter(user.getUid(), new ArrayList<>());
            tweetsAdapter.setListener(tweetListener);
            tweetList.setLayoutManager(new LinearLayoutManager(tweetList.getContext()));
            tweetList.setAdapter(tweetsAdapter);
            tweetList.addItemDecoration(new DividerItemDecoration(tweetList.getContext(), DividerItemDecoration.VERTICAL));

            SwipeRefreshLayout swipeRefresh = Objects.requireNonNull(MyActivityFragment.this.getView()).findViewById(R.id.swipeRefresh);
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
        RecyclerView tweetList = Objects.requireNonNull(MyActivityFragment.this.getView()).findViewById(R.id.tweetList);
        tweetList.setVisibility(View.GONE);
        List<Tweet> tweets = new ArrayList<>();

        if (user != null) {
            firebaseDB.collection(Constants.DATA_TWEETS).whereArrayContains(Constants.DATA_TWEET_USER_IDS, user.getUid()).get()
                    .addOnSuccessListener((list) -> {
                        for (DocumentSnapshot document : list.getDocuments()) {
                            Tweet tweet = document.toObject(Tweet.class);
                            if (tweet != null) {
                                tweets.add(tweet);
                            }
                        }
                        Collections.sort(tweets, new SortByTimeStamp());
                        tweetsAdapter.updateTweets(tweets);
                        tweetList.setVisibility(View.VISIBLE);
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        tweetList.setVisibility(View.VISIBLE);
                    });
        }
    }

}
