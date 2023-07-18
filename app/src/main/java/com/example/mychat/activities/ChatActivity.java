package com.example.mychat.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mychat.models.Message;
import com.example.mychat.adapters.MessageAdapter;
import com.example.mychat.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageBox;
    private ImageView sendButton;
    private MessageAdapter messageAdapter;
    private DatabaseReference mDBRef;
    private FirebaseAuth mAuth;
    private List<Message> messageList;
    private String senderRoom;              // Separate room for all the chat of Sender
    private String receiverRoom;            // Separate room for all the chat of Receiver

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        String name = getIntent().getStringExtra("name");
        String receiverUid = getIntent().getStringExtra("uid");
        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        messageBox = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_image_view);
        mDBRef = FirebaseDatabase.getInstance("https://my-chat-202a1-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        mAuth = FirebaseAuth.getInstance();
        String senderUid = mAuth.getCurrentUser().getUid();
        senderRoom = receiverUid + senderUid;
        receiverRoom = senderUid + receiverUid;
        messageList = new ArrayList<Message>();
        messageAdapter = new MessageAdapter(messageList, this, senderRoom, receiverRoom);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(messageAdapter);

        // Displaying Messages by fetching them from Database
        mDBRef.child("chats").child(senderRoom).child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                messageList.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Message message = dataSnapshot.getValue(Message.class);
                    message.setMessageId(dataSnapshot.getKey());
                    messageList.add(message);
                }
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });


        // Sending the message to database on clicking send button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = messageBox.getText().toString();
                if(messageText.length() != 0) {
                    Date date = new Date();
                    Message messageObject = new Message(messageText, senderUid, date.getTime());
                    // Getting a random key to store both sender side and receiver side message with same key.
                    String randomKey = mDBRef.push().getKey();
                    mDBRef.child("chats").child(senderRoom).child("messages").child(randomKey).setValue(messageObject).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mDBRef.child("chats").child(receiverRoom).child("messages").child(randomKey).setValue(messageObject);
                        }
                    });
                    messageBox.setText("");
                }else{
                    Toast.makeText(ChatActivity.this, "Message is empty!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    // overriding the method to go back to previous activity when clicked on back button
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}