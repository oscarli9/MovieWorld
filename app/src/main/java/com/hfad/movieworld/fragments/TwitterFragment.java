package com.hfad.movieworld.fragments;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hfad.movieworld.adapters.TweetListAdapter;
import com.hfad.movieworld.listeners.HomeCallback;
import com.hfad.movieworld.listeners.TwitterListenerImpl;
import com.hfad.movieworld.utils.User;

public abstract class TwitterFragment extends Fragment {

    TweetListAdapter tweetsAdapter;
    TwitterListenerImpl tweetListener;
    User currentUser;
    final FirebaseFirestore firebaseDB = FirebaseFirestore.getInstance();
    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    HomeCallback callback;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof HomeCallback) {
            this.callback = (HomeCallback) context;
        } else {
            throw new RuntimeException(context + " must implement HomeCallback");
        }
    }

    public void setUser(@NonNull User currentUser) {
        this.currentUser = currentUser;
        if (tweetListener != null) {
            this.tweetListener.setUser(currentUser);
        }
    }

    abstract public void updateList();
}
