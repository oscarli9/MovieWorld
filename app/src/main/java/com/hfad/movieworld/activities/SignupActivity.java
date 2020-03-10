package com.hfad.movieworld.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hfad.movieworld.R;
import com.hfad.movieworld.utils.User;
import com.hfad.movieworld.utils.Constants;

import java.util.ArrayList;

public class SignupActivity extends AppCompatActivity {

    private final FirebaseFirestore firebaseDB = FirebaseFirestore.getInstance();
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseAuth.AuthStateListener firebaseAuthListener = (FirebaseAuth.AuthStateListener)((it) -> {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String user = currentUser.getUid();
            if (!user.isEmpty()) {
                startActivity(HomeActivity.newIntent(SignupActivity.this));
                SignupActivity.this.finish();
            }
        }
    });

    public static Intent newIntent(Context context) {
        return new Intent(context, SignupActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        setTextChangeListener(findViewById(R.id.usernameET), findViewById(R.id.usernameTIL));
        setTextChangeListener(findViewById(R.id.emailET), findViewById(R.id.emailTIL));
        setTextChangeListener(findViewById(R.id.passwordET), findViewById(R.id.passwordTIL));

        LinearLayout signupProgressLayout = findViewById(R.id.signupProgressLayout);
        signupProgressLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    private void setTextChangeListener(TextInputEditText et, TextInputLayout til) {
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                til.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void onSignup(View view) {
        boolean proceed = true;
        LinearLayout signupProgressLayout = findViewById(R.id.signupProgressLayout);
        TextInputEditText usernameET = findViewById(R.id.usernameET);
        TextInputLayout usernameTIL = findViewById(R.id.usernameTIL);
        TextInputEditText emailET = findViewById(R.id.emailET);
        TextInputLayout emailTIL = findViewById(R.id.emailTIL);
        TextInputEditText passwordET = findViewById(R.id.passwordET);
        TextInputLayout passwordTIL = findViewById(R.id.passwordTIL);

        if (usernameET.getText() == null || usernameET.getText().toString().isEmpty()) {
            usernameTIL.setError("Username is required.");
            usernameTIL.setErrorEnabled(true);
            proceed = false;
        }

        if (emailET.getText() == null || emailET.getText().toString().isEmpty()) {
            emailTIL.setError("Email is required.");
            emailTIL.setErrorEnabled(true);
            proceed = false;
        }

        if (passwordET.getText() == null || passwordET.getText().toString().isEmpty()) {
            passwordTIL.setError("Password is required.");
            passwordTIL.setErrorEnabled(true);
            proceed = false;
        }

        if (proceed) {
            signupProgressLayout.setVisibility(View.VISIBLE);
            firebaseAuth.createUserWithEmailAndPassword(emailET.getText().toString(), passwordET.getText().toString())
                    .addOnCompleteListener((task) -> {
                        if (!task.isSuccessful()) {
                            Toast.makeText(this, "Signup error: " + task.getException().getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                        } else {
                            String email = emailET.getText().toString();
                            String username = usernameET.getText().toString();
                            User user = new User(email, username, "", new ArrayList<String>(), new ArrayList<String>());
                            if (firebaseAuth.getUid() != null) firebaseDB.collection(Constants.DATA_USERS).document(firebaseAuth.getUid()).set(user);
                        }
                        signupProgressLayout.setVisibility(View.GONE);
                    })
                    .addOnFailureListener((e) -> {
                        e.printStackTrace();
                        signupProgressLayout.setVisibility(View.GONE);
                    });
        }
    }

    public void goToLogin(View view) {
        startActivity(LoginActivity.newIntent(this));
        this.finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(firebaseAuthListener);
    }
}
