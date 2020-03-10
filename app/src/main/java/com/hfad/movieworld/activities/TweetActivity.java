package com.hfad.movieworld.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hfad.movieworld.R;
import com.hfad.movieworld.broadcast_receivers.BatteryLevelReceiver;
import com.hfad.movieworld.services.TimerService;
import com.hfad.movieworld.utils.Constants;
import com.hfad.movieworld.utils.Tweet;
import com.hfad.movieworld.utils.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TweetActivity extends AppCompatActivity {

    public static String PARAM_USER_ID = "UserId";
    public static String PARAM_USER_NAME = "UserName";

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firebaseDB = FirebaseFirestore.getInstance();
    private final StorageReference firebaseStorage = FirebaseStorage.getInstance().getReference();
    private String imageUrl;
    private String userId;
    private String userName;
    private final BatteryLevelReceiver batteryLevelReceiver = new BatteryLevelReceiver();
    private TimerService timerService;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerService.TimerBinder timerBinder = (TimerService.TimerBinder) service;
            timerService = timerBinder.getTimer();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public static Intent newIntent(Context context, String userId, String userName) {
        Intent intent = new Intent(context, TweetActivity.class);
        intent.putExtra(PARAM_USER_ID, userId);
        intent.putExtra(PARAM_USER_NAME, userName);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet);

        Intent intent = getIntent();
        if (intent.hasExtra(PARAM_USER_ID) && intent.hasExtra(PARAM_USER_NAME)) {
            userId = intent.getStringExtra(PARAM_USER_ID);
            userName = intent.getStringExtra(PARAM_USER_NAME);
        } else {
            Toast.makeText(this, "Error creating tweet", Toast.LENGTH_SHORT).show();
            finish();
        }

        LinearLayout tweetProgressLayout = findViewById(R.id.tweetProgressLayout);
        tweetProgressLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        registerReceiver(batteryLevelReceiver, new IntentFilter(Intent.ACTION_BATTERY_LOW));
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent serviceIntent = new Intent(this, TimerService.class);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        unbindService(connection);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.REQUEST_CODE_PHOTO) {
            storeImage(intent.getData());
        }
    }

    public void addImage(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, Constants.REQUEST_CODE_PHOTO);
    }

    private void storeImage(Uri uri) {
        if (uri != null) {
            Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();
            ImageView tweetImage = findViewById(R.id.tweetImage);
            LinearLayout tweetProgressLayout = findViewById(R.id.tweetProgressLayout);
            tweetProgressLayout.setVisibility(View.VISIBLE);
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                StorageReference filePath = firebaseStorage.child(Constants.DATA_TWEET_IMAGES).child(user.getUid());
                filePath.putFile(uri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                filePath.getDownloadUrl()
                                        .addOnSuccessListener((uri) -> {
                                            imageUrl = uri.toString();
                                            Util.loadUrl(tweetImage, imageUrl, R.drawable.logo);
                                            tweetProgressLayout.setVisibility(View.GONE);
                                        })
                                        .addOnFailureListener((e) -> {
                                            onUploadFailure(tweetProgressLayout);
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                onUploadFailure(tweetProgressLayout);
                            }
                        });
            }
        }
    }

    private void onUploadFailure(LinearLayout profileProgressLayout) {
        Toast.makeText(TweetActivity.this, "Image upload failed. Please try again later", Toast.LENGTH_SHORT).show();
        profileProgressLayout.setVisibility(View.GONE);
    }

    public void postTweet(View view) {
        LinearLayout tweetProgressLayout = findViewById(R.id.tweetProgressLayout);
        tweetProgressLayout.setVisibility(View.VISIBLE);

        EditText textET = findViewById(R.id.tweetText);
        String text = textET.getText().toString();
        List<String> hashtags = getHashtags(text);
        String[] userIds = {userId};
        DocumentReference tweetId = firebaseDB.collection(Constants.DATA_TWEETS).document();
        Tweet tweet = new Tweet(tweetId.getId(), Arrays.asList(userIds), userName, text, imageUrl, System.currentTimeMillis(),
                hashtags, new ArrayList<>());
        tweetId.set(tweet)
                .addOnCompleteListener(task -> {
                    this.finish();
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    tweetProgressLayout.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to post the tweet.", Toast.LENGTH_SHORT).show();
                });
        this.finish();
    }

    private List<String> getHashtags(String source) {
        ArrayList<String> hashtags = new ArrayList<>();
        String text = source;
        String hashtag;

        while (text.contains("#")) {
            int hash = text.indexOf("#");
            text = text.substring(hash + 1);

            int firstSpace = text.indexOf(" ");
            int firstHash = text.indexOf("#");

            if (firstSpace == -1 && firstHash == -1) {
                hashtag = text.substring(0);
            } else if (firstSpace != -1 && firstSpace < firstHash) {
                hashtag = text.substring(0, firstSpace);
                text = text.substring(firstSpace + 1);
            } else {
                if (firstSpace > -1) {
                    hashtag = text.substring(0, firstSpace);
                    text = text.substring(firstSpace);
                }
                else hashtag = "";
            }

            if (!hashtag.isEmpty()) {
                hashtags.add(hashtag);
            }
        }
        return hashtags;
    }
}
