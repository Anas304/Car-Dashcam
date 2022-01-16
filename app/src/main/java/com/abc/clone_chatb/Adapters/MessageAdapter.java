package com.abc.clone_chatb.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.abc.clone_chatb.Object.Chat;
import com.abc.clone_chatb.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private static final int MSG_TYPE_LEFT  = 0;
    private static final int MSG_TYPE_RIGHT  = 1;

    private Context mContext;
    private List<Chat> mChats;
    private String imageurl;
    FirebaseUser fuser;

    public MessageAdapter(Context mContext, List<Chat> mChats , String imageurl) {
        this.mContext = mContext;
        this.mChats = mChats;
        this.imageurl = imageurl;
    }

    //Big Brain stuff
    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_chat_right,parent,false);
            return new MessageAdapter.ViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_chat_left,parent,false);
            return new MessageAdapter.ViewHolder(view);
        }


    }
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        final Chat chats = mChats.get(position);
        String messageType = chats.getType();

        if (messageType != null && messageType.equals("text")){
            holder.show_message.setText(chats.getMessage());
        }
        else {
            Glide.with(mContext).load(messageType).into(holder.image_message);
        }

        if (imageurl.equals("default")){
            holder.profile_image.setImageResource(R.mipmap.bob);
        }
        else{
            Glide.with(mContext).load(imageurl).into(holder.profile_image);
        }
        // Check Last Message seen or not
        if ( mChats.size() > 0 ){
            if (chats.isIsseen()){
                holder.text_seen.setText("Seen");
            }
            else {
                holder.text_seen.setText("Delivered");
            }
        }
        else {
            holder.text_seen.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return mChats.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView show_message;
        public ImageView profile_image;
        public ImageView image_message;
        public TextView text_seen;

        //layout for paticular message aka image or text
        public RelativeLayout layout_text,layout_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
            text_seen = itemView.findViewById(R.id.txt_seen);

            //Image as message
            image_message = itemView.findViewById(R.id.image_message);
            //Layout to choose from
            layout_text = itemView.findViewById(R.id.layout_text);
            layout_image = itemView.findViewById(R.id.layout_image);
        }
    }

    //Return the view type of the item at position for the purposes of view recycling.
    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChats.get(position).getSender().equals(fuser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else
            return MSG_TYPE_LEFT;
    }
}
