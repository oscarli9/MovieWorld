package com.hfad.movieworld.listeners;

import android.app.AlertDialog;
import android.content.DialogInterface;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hfad.movieworld.utils.Constants;
import com.hfad.movieworld.utils.Tweet;
import com.hfad.movieworld.utils.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TwitterListenerImpl implements TweetListener {

    private RecyclerView tweetList;
    private HomeCallback homeCallback;
    private String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    private User user;
    private final FirebaseFirestore firebaseDB = FirebaseFirestore.getInstance();


    public TwitterListenerImpl() {

    }

    public TwitterListenerImpl(RecyclerView tweetList, User user, HomeCallback callback) {
        this.tweetList = tweetList;
        this.user = user;
        this.homeCallback = callback;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public void onLayoutClick(Tweet tweet) {
        if (tweet != null) {
            String owner = tweet.getUserIds().get(0);
            if (!owner.equals(userId)) {
                if (user.followUsers != null && user.followUsers.contains(owner)) {
                    AlertDialog alertDialog = new AlertDialog.Builder(tweetList.getContext())
                            .setTitle("Unfollow " + tweet.getUsername() + "?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    tweetList.setClickable(false);
                                    List<String> followUsers;
                                    if (user.followUsers == null) {
                                        followUsers = new ArrayList<>();
                                    } else {
                                        followUsers = user.followUsers;
                                    }
                                    followUsers.remove(owner);
                                    firebaseDB.collection(Constants.DATA_USERS).document(userId).update(Constants.DATA_USER_FOLLOW, followUsers)
                                            .addOnSuccessListener((it) -> {
                                                tweetList.setClickable(true);
                                                homeCallback.onRefresh();
                                            })
                                            .addOnFailureListener((it) -> {
                                                tweetList.setClickable(true);
                                            });
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(tweetList.getContext())
                            .setTitle("Follow " + tweet.getUsername() + "?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    tweetList.setClickable(false);
                                    List<String> followUsers;
                                    if (user.followUsers == null) {
                                        followUsers = new ArrayList<>();
                                    } else {
                                        followUsers = user.followUsers;
                                    }
                                    followUsers.add(owner);
                                    firebaseDB.collection(Constants.DATA_USERS).document(userId).update(Constants.DATA_USER_FOLLOW, followUsers)
                                            .addOnSuccessListener((it) -> {
                                                tweetList.setClickable(true);
                                                homeCallback.onRefresh();
                                            })
                                            .addOnFailureListener((it) -> {
                                                tweetList.setClickable(true);
                                            });
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
            }
        }
    }

    @Override
    public void onLike(Tweet tweet) {
        if (tweet != null) {
            tweetList.setClickable(false);
            List<String> likes = tweet.getLikes();
            if (likes.contains(userId)) {
                likes.remove(userId);
            } else {
                likes.add(userId);
            }
            firebaseDB.collection(Constants.DATA_TWEETS).document(tweet.getTweetId()).update(Constants.DATA_TWEET_LIKES, likes)
                    .addOnSuccessListener((it) -> {
                        tweetList.setClickable(true);
                        homeCallback.onRefresh();
                    })
                    .addOnFailureListener((it) -> {
                        tweetList.setClickable(true);
                    });
        }
    }

    @Override
    public void onRetweet(Tweet tweet) {
        if (tweet != null) {
            tweetList.setClickable(false);
            List<String> userIds = tweet.getUserIds();
            if (userIds.contains(userId)) {
                if (!userIds.get(0).equals(userId)) {
                    userIds.remove(userId);
                }
            } else {
                userIds.add(userId);
            }
            firebaseDB.collection(Constants.DATA_TWEETS).document(tweet.getTweetId()).update(Constants.DATA_TWEET_USER_IDS, userIds)
                    .addOnSuccessListener((it) -> {
                        tweetList.setClickable(true);
                        homeCallback.onRefresh();
                    })
                    .addOnFailureListener((it) -> {
                        tweetList.setClickable(true);
                    });
        }
    }
}
