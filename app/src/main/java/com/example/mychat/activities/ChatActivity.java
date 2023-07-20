package com.example.mychat.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mychat.models.Message;
import com.example.mychat.adapters.MessageAdapter;
import com.example.mychat.R;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageBox;
    private ImageView sendButton;
    private ImageView attachImage;
    private MessageAdapter messageAdapter;
    private DatabaseReference mDBRef;
    private FirebaseAuth mAuth;
    private List<Message> messageList;
    private String senderUid;
    private String senderRoom;              // Separate room for all the chat of Sender
    private String receiverRoom;            // Separate room for all the chat of Receiver

    private ProgressDialog dialog;          // Progress dialog to show processing animation till image is uploaded

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        String name = getIntent().getStringExtra("name");
        String receiverUid = getIntent().getStringExtra("uid");
        String profilePicture = getIntent().getStringExtra("image");
        // setting up our custome toolbar as actionbar for activity
        setSupportActionBar(findViewById(R.id.chat_toolbar));
        TextView userName = findViewById(R.id.chat_toolbar_name);
        ImageView userImage = findViewById(R.id.chat_toolbar_image);
        userName.setText(name);
        Glide.with(ChatActivity.this).load(profilePicture).placeholder(R.drawable.avatar).into(userImage);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending image...");
        dialog.setCancelable(false);

        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        messageBox = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_image_view);
        attachImage = findViewById(R.id.attach_image);
        mDBRef = FirebaseDatabase.getInstance("https://my-chat-202a1-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        mAuth = FirebaseAuth.getInstance();
        senderUid = mAuth.getCurrentUser().getUid();
        senderRoom = receiverUid + senderUid;
        receiverRoom = senderUid + receiverUid;
        messageList = new ArrayList<Message>();
        messageAdapter = new MessageAdapter(messageList, this, senderRoom, receiverRoom);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(messageAdapter);

        // Displaying the status of receiving user in toolbar
        mDBRef.child("presence").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String status = snapshot.getValue(String.class);
                    TextView userStatus = findViewById(R.id.chat_toolbar_status);
                    userStatus.setText(status);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

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
                findViewById(R.id.chat_progress_bar).setVisibility(View.GONE);
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
                    // Adding the message as the last sent message and it's time in database.
                    HashMap<String, Object> lastMsgObj = new HashMap<>();
                    lastMsgObj.put("lastMsg" , messageObject.getMessage());
                    lastMsgObj.put("lastMsgTime", date.getTime());
                    mDBRef.child("chats").child(senderRoom).updateChildren(lastMsgObj);
                    mDBRef.child("chats").child(receiverRoom).updateChildren(lastMsgObj);
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

        // setting up onclick listener on attach image icon to send image from gallery
        attachImage.setOnClickListener(new View.OnClickListener() {
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
        // setting listener on message box to change the user status to typing when typing.
        final Handler handler = new Handler();
        messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mDBRef.child("presence").child(senderUid).setValue("Typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping, 1000);
            }
            Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    mDBRef.child("presence").child(senderUid).setValue("Online");
                }
            };
        });


    }

    // This method gets the result of the selected image for attach image icon
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if(data != null && data.getData() != null) {
                Uri selectedImage = data.getData();
                Calendar calendar = Calendar.getInstance();
                StorageReference reference = FirebaseStorage.getInstance().getReference()
                        .child("chats").child(String.valueOf(calendar.getTimeInMillis()));
                dialog.show();
                reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(Task<UploadTask.TaskSnapshot> task) {
                        dialog.dismiss();
                        if(task.isSuccessful()){
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String filePath = uri.toString();
                                    // adding the image url as a message in chats database.
                                    Date date = new Date();
                                    Message messageObject = new Message("IMAGE", senderUid, date.getTime());
                                    messageObject.setImageUrl(filePath);
                                    // Adding the message as the last sent message and it's time in database.
                                    HashMap<String, Object> lastMsgObj = new HashMap<>();
                                    lastMsgObj.put("lastMsg" , messageObject.getMessage());
                                    lastMsgObj.put("lastMsgTime", date.getTime());
                                    mDBRef.child("chats").child(senderRoom).updateChildren(lastMsgObj);
                                    mDBRef.child("chats").child(receiverRoom).updateChildren(lastMsgObj);
                                    // Getting a random key to store both sender side and receiver side message with same key.
                                    String randomKey = mDBRef.push().getKey();
                                    mDBRef.child("chats").child(senderRoom).child("messages").child(randomKey).setValue(messageObject).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mDBRef.child("chats").child(receiverRoom).child("messages").child(randomKey).setValue(messageObject);

                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    // overriding the method to go back to previous activity when clicked on back button
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
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
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance("https://my-chat-202a1-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference().child("presence").child(userId).setValue("Offline");
        super.onPause();
    }

}