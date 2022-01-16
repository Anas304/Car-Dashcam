package com.abc.clone_chatb.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.abc.clone_chatb.MessageActivity;
import com.abc.clone_chatb.Object.Chat;
import com.abc.clone_chatb.Object.Users;
import com.abc.clone_chatb.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<Users> mUsers;
    private boolean ischat;
    private String theLastMessage;

    public UserAdapter(){}

    public UserAdapter(Context mContext, List<Users> mUsers, boolean ischat) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.ischat = ischat;
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_user,parent,false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Users users = mUsers.get(position);
        holder.username.setText(users.getUsername());
        if (users.getImageURL().equals("default")){
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        }
        else{
            Glide.with(mContext).load(users.getImageURL()).into(holder.profile_image);
        }

        // User Online/Offline status visibility based on different conditions
        if (ischat){
            if (users.getStatus() != null && users.getStatus().equals("online")){
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.INVISIBLE);
            }
            else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
        }
        else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }

        //Check for last_Message
        if (ischat){
            lastMessage(users.getId(),holder.last_message);
        }
        else{
            holder.last_message.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userid",users.getId());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }
    @Override
    public long getItemId(int position) {
        return position;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{

        private TextView username,last_message;
        private ImageView profile_image, img_on ,img_off;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username_list);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            last_message = itemView.findViewById(R.id.last_message);
        }
    }

    //Show Last message below username
    private void lastMessage(final String userid , final TextView last_message){
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Chat chat = dataSnapshot.getValue(Chat.class);

                    //Big brain Stuff
                    assert chat != null;
                    assert firebaseUser != null;
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid)
                    || chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())){
                        //And here the last Message
                        theLastMessage = chat.getMessage();
                    }

                }
                switch (theLastMessage){
                    case "default":
                        last_message.setText("No Message");
                        break;
                    default:
                        last_message.setText(theLastMessage);
                        break;
                }
                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
