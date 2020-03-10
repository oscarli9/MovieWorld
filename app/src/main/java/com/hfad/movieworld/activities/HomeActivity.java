package com.hfad.movieworld.activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hfad.movieworld.R;
import com.hfad.movieworld.broadcast_receivers.BatteryLevelReceiver;
import com.hfad.movieworld.fragments.HomeFragment;
import com.hfad.movieworld.fragments.MyActivityFragment;
import com.hfad.movieworld.fragments.SearchFragment;
import com.hfad.movieworld.fragments.TwitterFragment;
import com.hfad.movieworld.listeners.HomeCallback;
import com.hfad.movieworld.utils.Constants;
import com.hfad.movieworld.utils.User;
import com.hfad.movieworld.utils.Util;

public class HomeActivity extends AppCompatActivity implements HomeCallback {

    private SectionPageAdapter sectionPageAdapter;
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firebaseDB = FirebaseFirestore.getInstance();
    private final HomeFragment homeFragment = new HomeFragment();
    private final SearchFragment searchFragment = new SearchFragment();
    private final MyActivityFragment myActivityFragment = new MyActivityFragment();
    private String imageUrl;
    private User user;
    private TwitterFragment currentFragment = homeFragment;
    private final BatteryLevelReceiver batteryLevelReceiver = new BatteryLevelReceiver();

    public static Intent newIntent(Context context) {
        return new Intent(context, HomeActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sectionPageAdapter = new SectionPageAdapter(getSupportFragmentManager());

        ViewPager container = findViewById(R.id.container);
        TabLayout tabs = findViewById(R.id.tabs);
        container.setAdapter(sectionPageAdapter);
        container.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
        tabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(container));
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                TextView titleBar = findViewById(R.id.titleBar);
                CardView searchBar = findViewById(R.id.searchBar);
                switch (tab.getPosition()) {
                    case 0: {
                        titleBar.setVisibility(View.VISIBLE);
                        titleBar.setText(R.string.title_home);
                        searchBar.setVisibility(View.GONE);
                        currentFragment = homeFragment;
                        break;
                    }
                    case 1: {
                        titleBar.setVisibility(View.GONE);
                        searchBar.setVisibility(View.VISIBLE);
                        currentFragment = searchFragment;
                        break;
                    }
                    case 2: {
                        titleBar.setVisibility(View.VISIBLE);
                        titleBar.setText(R.string.title_my_activity);
                        searchBar.setVisibility(View.GONE);
                        currentFragment = myActivityFragment;
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        ImageView logo = findViewById(R.id.logo);
        logo.setOnClickListener(v -> {
            startActivity(ProfileActivity.newIntent(this));
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener((it) -> {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (user != null && currentUser!= null) {
                startActivity(TweetActivity.newIntent(this, currentUser.getUid(), user.getUsername()));
            }
        });

        LinearLayout homeProgressLayout = findViewById(R.id.homeProgressLayout);
        homeProgressLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        EditText search = findViewById(R.id.search);
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchFragment.newHashtag(v.getText().toString());
                }
                return true;
            }
        });

        registerReceiver(batteryLevelReceiver, new IntentFilter(Intent.ACTION_BATTERY_LOW));
    }

    public void onResume() {
        super.onResume();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(LoginActivity.newIntent(this));
            this.finish();
        } else {
            populate();
        }
    }

    @Override
    public void onUserUpdated() {
        populate();
    }

    @Override
    public void onRefresh() {
        currentFragment.updateList();
    }

    public final class SectionPageAdapter extends FragmentPagerAdapter {

        public SectionPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        @NonNull
        public Fragment getItem(int position) {
            Fragment fragment;
            switch (position) {
                case 0: {
                    fragment = homeFragment;
                    break;
                }
                case 1: {
                    fragment = searchFragment;
                    break;
                }
                default: fragment = myActivityFragment;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    private void populate() {
        LinearLayout homeProgressLayout = findViewById(R.id.homeProgressLayout);
        ImageView logo = findViewById(R.id.logo);

        homeProgressLayout.setVisibility(View.VISIBLE);
        if (firebaseAuth.getCurrentUser() != null) {
            firebaseDB.collection(Constants.DATA_USERS).document(firebaseAuth.getCurrentUser().getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            imageUrl = user.getImageUrl();
                            if (imageUrl != null) {
                                Util.loadUrl(logo, imageUrl, R.drawable.logo);
                            }
                        }
                        homeProgressLayout.setVisibility(View.GONE);
                        updateFragmentUser();
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        finish();
                    });
        }
    }

    private void updateFragmentUser() {
        homeFragment.setUser(user);
        searchFragment.setUser(user);
        myActivityFragment.setUser(user);
        currentFragment.updateList();
    }

}
