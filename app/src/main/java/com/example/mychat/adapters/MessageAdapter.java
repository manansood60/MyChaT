package com.example.mychat.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mychat.models.Message;
import com.example.mychat.R;
import com.github.pgreze.reactions.Reaction;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> messageList;
    private Context parentContext;
    private String senderRoom;
    private String receiverRoom;
    private DatabaseReference mDBRef;
    private int[] reactions;

    private final int SENT_TYPE = 1;
    private final int RECEIVED_TYPE = 2;

    public MessageAdapter(List<Message> messages, Context context, String senderRoom, String receiverRoom){
        messageList = messages;
        parentContext = context;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == SENT_TYPE){
            // inflating the senders layout
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_sent_layout,parent,false);
            return new SentViewHolder(view);
        }else{
            // inflating the received layout
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_receive_layout,parent,false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        // set up the reactions
        ReactionPopup popup = setupReactions(message);

        if(holder.getClass() == SentViewHolder.class){
            // logic for binding data to sent message layout
            SentViewHolder viewHolder = (SentViewHolder)holder;
            if(message.getImageUrl() != null){
                viewHolder.sentMessage.setVisibility(View.GONE);
                Glide.with(parentContext)
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(viewHolder.sentMessageImage);
                viewHolder.sentMessageImage.setVisibility(View.VISIBLE);
            }
            // setting the message to textview
            viewHolder.sentMessage.setText(message.getMessage());
            // if there is a reaction with the message then set it.
            if(message.getFeeling() >= 0){
                viewHolder.sentMessageFeeling.setImageResource(reactions[message.getFeeling()]);
                viewHolder.sentMessageFeeling.setVisibility(View.VISIBLE);
            }else{
                viewHolder.sentMessageFeeling.setVisibility(View.GONE);
            }
            // setting onTouch listener to open reactions on touching message
            GestureDetector gestureDetector = new GestureDetector(parentContext, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public void onLongPress(MotionEvent e) {
                    // Handle long press event here (e.g., show a dialog for message deletion)
                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    // Handle single tap event here (e.g., open reactions window)
                    popup.onTouch(viewHolder.itemView, e);
                    return true;
                }
            });
            if(!message.getMessage().equals("The message is deleted.")) {
                viewHolder.itemView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {

                        return gestureDetector.onTouchEvent(motionEvent);
                    }
                });
                // set up long click listener for message deletion
                viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        AlertDialog deleteDialog = createDeleteDialog(message, viewHolder);
                        deleteDialog.show();
                        return false;
                    }

                });

            }

        }else{
            // logic for data binding to received message layout
            ReceivedViewHolder viewHolder = (ReceivedViewHolder) holder;
            // setting the message to textview
            if(message.getImageUrl() != null){
                viewHolder.receivedMessage.setVisibility(View.GONE);
                Glide.with(parentContext)
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(viewHolder.receivedMessageImage);
                viewHolder.receivedMessageImage.setVisibility(View.VISIBLE);
            }
            viewHolder.receivedMessage.setText(message.getMessage());
            // if there is a reaction with the message then set it.
            if(message.getFeeling() >= 0){
                viewHolder.receivedMessageReaction.setImageResource(reactions[message.getFeeling()]);
                viewHolder.receivedMessageReaction.setVisibility(View.VISIBLE);
            }else{
                viewHolder.receivedMessageReaction.setVisibility(View.GONE);
            }

            // setting onTouch listener to open reactions on touching message
            GestureDetector gestureDetector = new GestureDetector(parentContext, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public void onLongPress(MotionEvent e) {
                    // Handle long press event here (e.g., show a dialog for message deletion)
                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    // Handle single tap event here (e.g., open reactions window)
                    popup.onTouch(viewHolder.itemView, e);
                    return true;
                }
            });
            if(!message.getMessage().equals("The message is deleted.")) {
                viewHolder.itemView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        return gestureDetector.onTouchEvent(motionEvent);
                    }
                });
                // set up long click listener for message deletion
                viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        AlertDialog deleteDialog = createDeleteDialog(message, viewHolder);
                        deleteDialog.show();
                        return false;
                    }

                });
            }
        }

    }

    @Override
    public int getItemCount() {
        if(messageList != null){
            return messageList.size();
        }else
            return 0;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(message.getSenderId())){
            // Current user is the sender of message
            return SENT_TYPE;
        }else{
            return RECEIVED_TYPE;
        }
    }

    public static class SentViewHolder extends RecyclerView.ViewHolder{
        ImageView sentMessageFeeling;
        ImageView sentMessageImage;
        TextView sentMessage;
        View sentMessageLayout;
        public SentViewHolder(View itemView) {
            super(itemView);
            sentMessage = itemView.findViewById(R.id.sent_message_text);
            sentMessageFeeling = itemView.findViewById(R.id.sent_message_feeling);
            sentMessageImage = itemView.findViewById(R.id.sent_message_image);
            sentMessageLayout = itemView.findViewById(R.id.sent_message_linearlayout);
        }
    }
    public static class ReceivedViewHolder extends RecyclerView.ViewHolder{
        ImageView receivedMessageImage;
        TextView receivedMessage;
        ImageView receivedMessageReaction;
        View receivedMessageLayout;
        public ReceivedViewHolder(View itemView) {
            super(itemView);
            receivedMessage = itemView.findViewById(R.id.received_message_text);
            receivedMessageReaction = itemView.findViewById(R.id.received_message_feeling);
            receivedMessageImage = itemView.findViewById(R.id.received_message_image);
            receivedMessageLayout = itemView.findViewById(R.id.received_message_linearlayout);
        }
    }
    // This method handles all the code of Related to Reactions
    public ReactionPopup setupReactions(Message message){
        // Setting up reactions on message
        reactions = new int[]{
                R.drawable.like,
                R.drawable.heart,
                R.drawable.laugh,
                R.drawable.shock,
                R.drawable.sad,
                R.drawable.angry
        };
        ReactionsConfig config = new ReactionsConfigBuilder(parentContext)
                .withReactions(reactions)
                .build();

        // This code will be called every time a reaction is selected.
        ReactionPopup popup = new ReactionPopup(parentContext, config, (reactionPosition) -> {


            // After the reaction has been selected we update it in the both user's database.
            message.setFeeling(reactionPosition);
            mDBRef = FirebaseDatabase.getInstance("https://my-chat-202a1-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
            mDBRef.child("chats").child(senderRoom).child("messages").child(message.getMessageId()).setValue(message);
            mDBRef.child("chats").child(receiverRoom).child("messages").child(message.getMessageId()).setValue(message);

            return true; // true is closing popup, false is requesting a new selection
        });
        return popup;
    }
    // This method creates and returns message deletion dialog
    public AlertDialog createDeleteDialog(Message message, RecyclerView.ViewHolder holder){
        mDBRef = FirebaseDatabase.getInstance("https://my-chat-202a1-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        AlertDialog.Builder builder = new AlertDialog.Builder(parentContext);
        ProgressDialog deleteDialog;          // Progress dialog to show processing animation till image is deleted.
        deleteDialog = new ProgressDialog(parentContext);
        deleteDialog.setMessage("Deleting image...");
        deleteDialog.setCancelable(false);


        // Chain together various setter methods to set the dialog characteristics
        builder.setTitle("Delete Message");

        // Adding the buttons
        builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User clicked Delete For Everyone button
                message.setMessage("The message is deleted.");
                message.setFeeling(-1);
                // if it is image message
                if(message.getImageUrl() != null){
                    StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(message.getImageUrl());
                    String imageName = storageReference.getName();
                    StorageReference deletionReference = FirebaseStorage.getInstance().getReference().child("chats/" + imageName);
                    Log.e("Delete " , deletionReference.toString());
                    deleteDialog.show();
                    deletionReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            deleteDialog.dismiss();
                            message.setImageUrl(null);
                            mDBRef.child("chats").child(senderRoom).child("messages").child(message.getMessageId()).setValue(message);
                            mDBRef.child("chats").child(receiverRoom).child("messages").child(message.getMessageId()).setValue(message);
                            if(holder.getClass() == ReceivedViewHolder.class){
                                ReceivedViewHolder viewHolder = (ReceivedViewHolder)(holder);
                                viewHolder.receivedMessageImage.setVisibility(View.GONE);
                                viewHolder.receivedMessageReaction.setVisibility(View.GONE);
                                viewHolder.receivedMessage.setVisibility(View.VISIBLE);
                            }else{
                                SentViewHolder viewHolder = (SentViewHolder) (holder);
                                viewHolder.sentMessageImage.setVisibility(View.GONE);
                                viewHolder.sentMessageFeeling.setVisibility(View.GONE);
                                viewHolder.sentMessage.setVisibility(View.VISIBLE);
                            }
                            holder.itemView.setOnTouchListener(null);
                            holder.itemView.setOnLongClickListener(null);
                        }
                    });
                }
                // if it is a text message.
                else{
                    mDBRef.child("chats").child(senderRoom).child("messages").child(message.getMessageId()).setValue(message);
                    mDBRef.child("chats").child(receiverRoom).child("messages").child(message.getMessageId()).setValue(message);
                    holder.itemView.setOnTouchListener(null);
                    holder.itemView.setOnLongClickListener(null);
                }
            }
        });

        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        AlertDialog dialog = builder.create();
        return dialog;
    }


}

