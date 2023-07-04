package com.example.mychat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    public static final String LOG_TAG = SignupActivity.class.getName();

    private EditText mNameText;
    private EditText mEmailText;
    private EditText mPasswordText;
    private Button mSignUp;

    private DatabaseReference mDBRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();

        mNameText = findViewById(R.id.name_edit_text);
        mEmailText = findViewById(R.id.email_edit_text);
        mPasswordText = findViewById(R.id.password_edit_text);
        mSignUp = findViewById(R.id.signup_btn);

        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = mNameText.getText().toString();
                String email = mEmailText.getText().toString();
                String password = mPasswordText.getText().toString();
                signUp(name,email,password);
            }
        });
    }

    private void signUp(String name,String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(LOG_TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(SignupActivity.this, user.getEmail() +" User Created.",
                                    Toast.LENGTH_SHORT).show();
                            addUserToDatabase(name, email, mAuth.getCurrentUser().getUid());
                            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                            startActivity(intent);

                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(LOG_TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignupActivity.this, "Registration failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }
    private void addUserToDatabase(String name, String email, String uid){
        Log.e(LOG_TAG,"Adding data");
        mDBRef = FirebaseDatabase.getInstance("https://my-chat-202a1-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        mDBRef.child("users").child(uid).setValue(new User(name,email,uid));
    }
}