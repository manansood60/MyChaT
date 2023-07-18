package com.example.mychat.adapters;

import android.content.Context;
import android.content.Intent;
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

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder> {

    private List<User> users;
    private Context parentContext;

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
        View listItemView = holder.itemView;
        TextView username = listItemView.findViewById(R.id.user_name);
        username.setText(user.getName());
        ImageView userImage = listItemView.findViewById(R.id.user_image);
        Glide.with(parentContext)
                .load(user.getProfilePicture())
                .placeholder(R.drawable.avatar)
                .into(userImage);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(parentContext, ChatActivity.class);
                intent.putExtra("name",user.getName());
                intent.putExtra("uid",user.getUid());
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

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        public MyViewHolder(View itemView) {
            super(itemView);
        }
    }
}
