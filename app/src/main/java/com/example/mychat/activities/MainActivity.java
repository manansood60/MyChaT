package com.example.mychat.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.mychat.R;
import com.example.mychat.models.User;
import com.example.mychat.adapters.UserAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private List<User> users;
    private UserAdapter mAdapter;
    private RecyclerView mRecyclerView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.main_toolbar));


        mAuth = FirebaseAuth.getInstance();

        users = new ArrayList<>();
        mAdapter = new UserAdapter(users,this);
        mRecyclerView = findViewById(R.id.users_recyler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRecyclerView.setAdapter(mAdapter);
        DatabaseReference mDBRef = FirebaseDatabase.getInstance("https://my-chat-202a1-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        mDBRef.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                users.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);
                    // Checking so that logged in user won't get displayed in the list
                    if(!mAuth.getCurrentUser().getUid().equals(user.getUid())) {
                        users.add(user);
                    }
                }
                mAdapter.notifyDataSetChanged();
                findViewById(R.id.users_progress_bar).setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.logout){
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance("https://my-chat-202a1-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference().child("presence").child(userId).setValue("Offline");
            mAuth.signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            finish();
            startActivity(intent);
        }else if(item.getItemId() == R.id.profile){
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

}