package com.example.mychat.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mychat.R;
import com.example.mychat.models.User;
import com.example.mychat.activities.ChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder> {

    private List<User> users;
    private Context parentContext;
    private DatabaseReference mDBRef;

    public UserAdapter(List<User> users, Context context){
        this.users = users;
        parentContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_layout,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        User user = users.get(position);
        holder.userName.setText(user.getName());
        Glide.with(parentContext)
                .load(user.getProfilePicture())
                .placeholder(R.drawable.avatar)
                .into(holder.userImage);
        // Fetching and displaying last msg data.
        String senderId = user.getUid();
        String senderRoom = FirebaseAuth.getInstance().getCurrentUser().getUid() + senderId;
        mDBRef = FirebaseDatabase.getInstance("https://my-chat-202a1-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        mDBRef.child("chats").child(senderRoom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String lastMsg = snapshot.child("lastMsg").getValue(String.class);
                    long lastMsgTime = snapshot.child("lastMsgTime").getValue(Long.class);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                    holder.lastMsg.setText(lastMsg);
                    holder.lastMsgTime.setText(dateFormat.format(new Date(lastMsgTime)));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(parentContext, ChatActivity.class);
                intent.putExtra("name",user.getName());
                intent.putExtra("uid",user.getUid());
                intent.putExtra("image",user.getProfilePicture());
                parentContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        if(users != null)
            return users.size();
        else
            return 0;
    }

    public String[] fetchLastMsgData(User user){
        final String[] lastMsgData = new String[2];

        return lastMsgData;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView userName;
        ImageView userImage;
        TextView lastMsg;
        TextView lastMsgTime;

        public MyViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userImage = itemView.findViewById(R.id.user_image);
            lastMsg = itemView.findViewById(R.id.last_message);
            lastMsgTime = itemView.findViewById(R.id.last_message_time);
        }
    }
}
