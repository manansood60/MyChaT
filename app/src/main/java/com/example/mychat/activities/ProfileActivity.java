package com.example.mychat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mychat.R;
import com.example.mychat.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileName;
    private ImageView profileImage;
    private Button saveButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDBRef;
    private StorageReference mSRef;
    private String userId;
    private Uri selectedImage;

    private ProgressDialog dialog;          // Progress dialog to show processing animation till image is uploaded

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setSupportActionBar(findViewById(R.id.profile_toolbar));
        setupBackButton();

        // Setting up progress dialog
        dialog = new ProgressDialog(this);
        dialog.setMessage("Updating Profile...");
        dialog.setCancelable(false);

        profileImage = findViewById(R.id.profile_picture);
        profileName = findViewById(R.id.profile_name);
        saveButton = findViewById(R.id.save_btn);
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        mDBRef = FirebaseDatabase.getInstance("https://my-chat-202a1-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        mSRef = FirebaseStorage.getInstance().getReference();

        // Fetching user data from database and displaying it.
        loadAndDisplayUserData();

        // Handling onclick on save button to save new user information in database.
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = profileName.getText().toString();
                if(selectedImage != null){
                    StorageReference reference = mSRef.child("profiles").child(userId);
                    dialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String imageUrl = uri.toString();
                                        mDBRef.child("users").child(userId).child("name").setValue(name);
                                        mDBRef.child("users").child(userId).child("profilePicture").setValue(imageUrl);
                                    }
                                });
                                dialog.dismiss();
                                goBack();
                            }
                        }
                    });
                }else{
                    mDBRef.child("users").child(userId).child("name").setValue(name);
                    goBack();
                }
            }
        });

        // Handling on click on image icon to select new image from gallery
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, 1);
                    // after this the result will be sent to onActivityResult() method overriden below.
                }
            }
        });

    }
    private void loadAndDisplayUserData(){
        mDBRef.child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                profileName.setText(user.getName());
                Glide.with(ProfileActivity.this)
                        .load(user.getProfilePicture())
                        .placeholder(R.drawable.avatar)
                        .into(profileImage);
            }
            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    // This method gets the result of the selected image for Profile Image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                selectedImage = data.getData();
                profileImage.setImageURI(selectedImage);
            }
        }
    }

    // Setting the user status online on loading the activity
    @Override
    protected void onResume() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance("https://my-chat-202a1-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference().child("presence").child(userId).setValue("Online");
        super.onResume();
    }
    // Setting the user status offline when user leaves the activity
    @Override
    protected void onPause() {
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance("https://my-chat-202a1-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference().child("presence").child(userId).setValue("Offline");
        }
        super.onPause();
    }
    // This method is used to setup back button functionality to go to previous activity on click
    private void setupBackButton() {
        ImageView backButton = findViewById(R.id.profile_toolbar_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
    // Ths method simply takes user to main activity after updating their information
    private void goBack(){
        Toast.makeText(ProfileActivity.this, "Profile Updated",
                Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);
    }

}