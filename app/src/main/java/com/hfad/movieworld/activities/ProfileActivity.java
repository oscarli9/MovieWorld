package com.hfad.movieworld.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hfad.movieworld.R;
import com.hfad.movieworld.broadcast_receivers.BatteryLevelReceiver;
import com.hfad.movieworld.utils.Constants;
import com.hfad.movieworld.utils.User;
import com.hfad.movieworld.utils.Util;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firebaseDB = FirebaseFirestore.getInstance();
    private final StorageReference firebaseStorage = FirebaseStorage.getInstance().getReference();
    private String imageUrl;
    private final BatteryLevelReceiver batteryLevelReceiver = new BatteryLevelReceiver();

    public static Intent newIntent(Context context) {
        return new Intent(context, ProfileActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) finish();

        LinearLayout profileProgressLayout = findViewById(R.id.profileProgressLayout);
        profileProgressLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        ImageView photoIV = findViewById(R.id.photoIV);
        photoIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, Constants.REQUEST_CODE_PHOTO);
            }
        });

        populateInfo();

        registerReceiver(batteryLevelReceiver, new IntentFilter(Intent.ACTION_BATTERY_LOW));
    }


    private void populateInfo() {
        LinearLayout profileProgressLayout = findViewById(R.id.profileProgressLayout);
        TextInputEditText usernameET = findViewById(R.id.usernameET);
        TextInputEditText emailET = findViewById(R.id.emailET);
        ImageView photoIV = findViewById(R.id.photoIV);

        profileProgressLayout.setVisibility(View.VISIBLE);
        if (firebaseAuth.getCurrentUser() != null) {
            firebaseDB.collection(Constants.DATA_USERS).document(firebaseAuth.getCurrentUser().getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            usernameET.setText(user.getUsername(), TextView.BufferType.EDITABLE);
                            emailET.setText(user.getEmail(), TextView.BufferType.EDITABLE);

                            imageUrl = user.getImageUrl();
                            if (imageUrl != null) {
                                Util.loadUrl(photoIV, imageUrl, R.drawable.logo);
                            }
                        }
                        profileProgressLayout.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        finish();
                    });
        }
    }

    public void onApply(View view) {
        LinearLayout profileProgressLayout = findViewById(R.id.profileProgressLayout);
        TextInputEditText usernameET = findViewById(R.id.usernameET);
        TextInputEditText emailET = findViewById(R.id.emailET);
        HashMap<String, Object> map = new HashMap<>();

        profileProgressLayout.setVisibility(View.VISIBLE);
        String username = usernameET.getText().toString();
        String email = emailET.getText().toString();
        map.put(Constants.DATA_USER_USERNAME, username);
        map.put(Constants.DATA_USER_EMAIL, email);

        if (firebaseAuth.getUid() != null) {
            firebaseDB.collection(Constants.DATA_USERS).document(firebaseAuth.getUid()).update(map)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ProfileActivity.this, "Update successful", Toast.LENGTH_SHORT).show();
                            ProfileActivity.this.finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        Toast.makeText(ProfileActivity.this, "Update failed. Please try again", Toast.LENGTH_SHORT).show();
                        profileProgressLayout.setVisibility(View.GONE);
                    });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.REQUEST_CODE_PHOTO) {
            storeImage(intent.getData());
        }
    }

    private void storeImage(Uri uri) {
        if (uri != null) {
            Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();
            ImageView photoIV = findViewById(R.id.photoIV);
            LinearLayout profileProgressLayout = findViewById(R.id.profileProgressLayout);
            profileProgressLayout.setVisibility(View.VISIBLE);
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                StorageReference filePath = firebaseStorage.child(Constants.DATA_IMAGES).child(user.getUid());
                filePath.putFile(uri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                filePath.getDownloadUrl()
                                    .addOnSuccessListener((uri) -> {
                                        String url = uri.toString();
                                        firebaseDB.collection(Constants.DATA_USERS).document(user.getUid()).update(Constants.DATA_USER_IMAGE_URL, url)
                                                .addOnSuccessListener((it) -> {
                                                    imageUrl = url;
                                                    Util.loadUrl(photoIV, imageUrl, R.id.logo);
                                                });
                                        profileProgressLayout.setVisibility(View.GONE);
                                    })
                                    .addOnFailureListener((e) -> {
                                        onUploadFailure(profileProgressLayout);
                                    });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                onUploadFailure(profileProgressLayout);
                            }
                        });
            }
        }
    }

    private void onUploadFailure(LinearLayout profileProgressLayout) {
        Toast.makeText(ProfileActivity.this, "Image upload failed. Please try again later", Toast.LENGTH_SHORT).show();
        profileProgressLayout.setVisibility(View.GONE);
    }

    public void onSignout(View view) {
        firebaseAuth.signOut();
        this.finish();
    }
}
