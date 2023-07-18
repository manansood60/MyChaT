package com.example.mychat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mychat.R;
import com.example.mychat.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class SignupActivity extends AppCompatActivity {

    public static final String LOG_TAG = SignupActivity.class.getName();
    static final int REQUEST_IMAGE_GET = 1;

    private EditText mNameText;
    private EditText mEmailText;
    private EditText mPasswordText;
    private EditText mConfirmPassword;
    private ImageView mProfilePicture;
    private Button mSignUp;
    private TextView mSignIn;

    private DatabaseReference mDBRef;
    private StorageReference mSRef;
    private FirebaseAuth mAuth;
    private Uri selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();

        mNameText = findViewById(R.id.name_edit_text);
        mEmailText = findViewById(R.id.email_edit_text);
        mPasswordText = findViewById(R.id.password_edit_text);
        mConfirmPassword = findViewById(R.id.confirm_password_edit_text);
        mProfilePicture = findViewById(R.id.profile_picture);
        mSignUp = findViewById(R.id.signup_btn);
        mSignIn = findViewById(R.id.login_btn);

        // Setting onClick Listener on Profile picture image to open the gallery of images
        mProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_IMAGE_GET);
                    // after this the result will be sent to onActivityResult() method overriden below.
                }
            }
        });

        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = mNameText.getText().toString();
                String email = mEmailText.getText().toString();
                String password = mPasswordText.getText().toString();
                String confirmPassword = mConfirmPassword.getText().toString();
                if(verify(name,email,password,confirmPassword)){
                    signUp(name,email,password);
                }
            }
        });

        mSignIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

    }// end of onCreate()

    // This method gets the result of the selected image for profile picture
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            if(data != null && data.getData() != null) {
                mProfilePicture.setImageURI(data.getData());
                selectedImage = data.getData();
            }
        }
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
                            addUserDataToDatabase(name, email, user.getUid());
                            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                            startActivity(intent);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(LOG_TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignupActivity.this, "Registration failed.",
                                    Toast.LENGTH_SHORT).show();
                        }


                    }
                });
    }
    private void addUserDataToDatabase(String name, String email, String uid){
        Log.e(LOG_TAG,"Adding data");
        // first adding image to Firebase Storage.
        if(selectedImage != null){
            mSRef = FirebaseStorage.getInstance().getReference().child("profiles").child(uid);
            mSRef.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        mSRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                // Adding User data in real-time database.
                                User user = new User(name,email,uid,imageUrl);
                                mDBRef = FirebaseDatabase.getInstance("https://my-chat-202a1-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
                                mDBRef.child("users").child(uid).setValue(user);
                            }
                        });
                    }
                }
            });
        }else{      // When no Image is selected add user data in database without image.
            User user = new User(name,email,uid,"No Image");
            mDBRef = FirebaseDatabase.getInstance("https://my-chat-202a1-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
            mDBRef.child("users").child(uid).setValue(user);
        }

    }


    private boolean verify(String name, String email, String password, String cPassword){
        String msg = null;
        if(name.isEmpty() || email.isEmpty() || password.isEmpty() || cPassword.isEmpty()){
            msg = "Please fill all the fields.";
        }else if(!password.equals(cPassword)){
            msg = "Passwords Don't Match.";
        }
        if(msg != null){
            Toast.makeText(SignupActivity.this, msg,
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}