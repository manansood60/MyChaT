package com.example.mychat.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mychat.models.Message;
import com.example.mychat.R;
import com.github.pgreze.reactions.Reaction;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
            // setting the message to textview
            viewHolder.sentMessage.setText(message.getMessage());
            // if there is a reaction with the message then set it.
            if(message.getFeeling() >= 0){
                viewHolder.sentMessageFeeling.setImageResource(reactions[message.getFeeling()]);
                viewHolder.sentMessageFeeling.setVisibility(View.VISIBLE);
            }
            // setting onTouch listener to open reactions on touching message
            viewHolder.sentMessage.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popup.onTouch(view, motionEvent);
                    return false;
                }
            });

        }else{
            // logic for data binding to received message layout
            ReceivedViewHolder viewHolder = (ReceivedViewHolder) holder;
            // setting the message to textview
            viewHolder.receivedMessage.setText(message.getMessage());
            // if there is a reaction with the message then set it.
            if(message.getFeeling() >= 0){
                viewHolder.receivedMessageReaction.setImageResource(reactions[message.getFeeling()]);
                viewHolder.receivedMessageReaction.setVisibility(View.VISIBLE);
            }
            // setting onTouch listener to open reactions on touching message
            viewHolder.receivedMessage.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popup.onTouch(view, motionEvent);
                    return false;
                }
            });
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
        TextView sentMessage;
        public SentViewHolder(View itemView) {
            super(itemView);
            sentMessage = itemView.findViewById(R.id.sent_message_text);
            sentMessageFeeling = itemView.findViewById(R.id.sent_message_feeling);
        }
    }
    public static class ReceivedViewHolder extends RecyclerView.ViewHolder{
        TextView receivedMessage;
        ImageView receivedMessageReaction;
        public ReceivedViewHolder(View itemView) {
            super(itemView);
            receivedMessage = itemView.findViewById(R.id.received_message_text);
            receivedMessageReaction = itemView.findViewById(R.id.received_message_feeling);
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


}

