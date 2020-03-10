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
import com.hfad.movieworld.R;

public class LoginActivity extends AppCompatActivity {

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseAuth.AuthStateListener firebaseAuthListener = (FirebaseAuth.AuthStateListener)((it) -> {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String user = currentUser.getUid();
            if (!user.isEmpty()) {
                startActivity(HomeActivity.newIntent(LoginActivity.this));
                LoginActivity.this.finish();
            }
        }
    });

    public static Intent newIntent(Context context) {
        return new Intent(context, LoginActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setTextChangeListener(findViewById(R.id.emailET), findViewById(R.id.emailTIL));
        setTextChangeListener(findViewById(R.id.passwordET), findViewById(R.id.passwordTIL));

        LinearLayout loginProgressLayout = findViewById(R.id.loginProgressLayout);
        loginProgressLayout.setOnTouchListener(new View.OnTouchListener() {
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

    public void onLogin(View view) {
        boolean proceed = true;
        LinearLayout loginProgressLayout = findViewById(R.id.loginProgressLayout);
        TextInputEditText emailET = findViewById(R.id.emailET);
        TextInputLayout emailTIL = findViewById(R.id.emailTIL);
        TextInputEditText passwordET = findViewById(R.id.passwordET);
        TextInputLayout passwordTIL = findViewById(R.id.passwordTIL);

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
            loginProgressLayout.setVisibility(View.VISIBLE);
            firebaseAuth.signInWithEmailAndPassword(emailET.getText().toString(), passwordET.getText().toString())
            .addOnCompleteListener((task) -> {
                if (!task.isSuccessful()) {
                    loginProgressLayout.setVisibility(View.GONE);
                    Toast.makeText(this, "Login error: " + task.getException().getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener((e) -> {
                e.printStackTrace();
                loginProgressLayout.setVisibility(View.GONE);
            });
        }
    }

    public void goToSignup(View view) {
        startActivity(SignupActivity.newIntent(this));
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
