package com.abc.clone_chatb;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abc.clone_chatb.Adapters.MessageAdapter;
import com.abc.clone_chatb.Fragments.APIService;
import com.abc.clone_chatb.Notifications.Client;
import com.abc.clone_chatb.Notifications.Data;
import com.abc.clone_chatb.Notifications.MyResponse;
import com.abc.clone_chatb.Notifications.Sender;
import com.abc.clone_chatb.Notifications.Token;
import com.abc.clone_chatb.Object.Chat;
import com.abc.clone_chatb.Object.Users;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    private CircularImageView profile_image;
    private TextView username;
    private Context context;

    //Firebase
    FirebaseUser fuser;
    DatabaseReference reference;
    StorageReference storageReference;

    Intent intent;
    ImageButton btn_send,image_message;
    EditText text_send;
    MessageAdapter messageAdapter;
    List<Chat> mchat;
    RecyclerView recyclerView;

    ValueEventListener seenListener;
    String userid;
    APIService apiService;
    boolean notify = false;
    private int GALLERY_PICK = 1;
    private Uri imageUri;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MessageActivity.this,MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
        //Calling ApiService for Notifications
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        profile_image = findViewById(R.id.profile_image);
        image_message = findViewById(R.id.btn_img);
        username  = findViewById(R.id.username);
        btn_send  = findViewById(R.id.btn_send);
        text_send  = findViewById(R.id.text_send);

        //Messages Recyclerview init
        recyclerView = findViewById(R.id.recyclerview_messages_MessageActivity);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        intent = getIntent();
        final String userid  = intent.getStringExtra("userid");
        //Firebase User and Reference
        fuser  = FirebaseAuth.getInstance().getCurrentUser();
        assert userid != null;

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                String message = text_send.getText().toString();
                if (!message.equals("")){
                    //sender,receiver and message 3 things
                    sendMessage(fuser.getUid(), userid,message);
                }
                else{
                    Toast.makeText(MessageActivity.this, "You can't send an empty", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });

        storageReference = FirebaseStorage.getInstance().getReference("Images");

        image_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Accessing photos from Gallery
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent,"Select Image"),GALLERY_PICK);

            }
        });

        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users users = snapshot.getValue(Users.class);
                assert users != null;
                username.setText(users.getUsername());
                if (users.getImageURL().equals("default")){
                    //Put a check if compiler gives a nullPointerException
                    if (profile_image != null){
                        profile_image.setImageResource(R.mipmap.ic_launcher);
                    }
                }
                else{
                    Glide.with(getApplicationContext()).load(users.getImageURL()).into(profile_image);
                }
                //Calling readMessage with its parameters
                readMessage(fuser.getUid(),userid,users.getImageURL());

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        seenMessage(userid);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                //imageView.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        }
    }


    private void seenMessage(final String userid){

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    assert chat != null;
                    if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid)){
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("isseen",true);
                        dataSnapshot.getRef().updateChildren(hashMap);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    //Sending Messages
    private void sendMessage(final String sender , final String receiver, String message){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String , Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("isseen",false);

        //Making a new Firebase RealtimeDatabse object Named Chat and pushing its keys.
        reference.child("Chats").push().setValue(hashMap);
        // scope nhi thi is liye yahan dobara copy paste kiya.
        final String userid  = intent.getStringExtra("userid");
        // add user to chatFragement

        assert userid != null;
        final DatabaseReference chatref = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fuser.getUid())
                .child(userid);

        chatref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    chatref.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        final String msg =  message;
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users users  = snapshot.getValue(Users.class);
                if (notify) {
                    sendNotification(receiver, users.getUsername(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendNotification(final String receiver, final String username, final String msg) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data =  new Data(fuser.getUid(),
                            R.mipmap.ic_launcher,username+": "+msg,"New Message",userid);
                    Sender sender = new Sender(data,token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200){
                                        if (response.body().success != 1){
                                            Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //reading messages from Firebase
    private void readMessage(final String myid, final String userid , final String imageURL){
        mchat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference().child("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mchat.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Chat chat = dataSnapshot.getValue(Chat.class);

                    assert chat != null;
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(myid)){
                        mchat.add(chat);
                    }
                }
                messageAdapter = new MessageAdapter(MessageActivity.this,mchat,imageURL);
                recyclerView.setAdapter(messageAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // User status Online/Offline
    private void Status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        //Adding status object in FirebaseDatabase
        HashMap<String , Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
        reference.updateChildren(hashMap);
    }



    @Override
    protected void onResume() {
        super.onResume();
        //calling Status()
        Status("online");
    }
    @Override
    protected void onPause() {
        super.onPause();
        //same as onResume
        if (seenListener!=null){
            reference.removeEventListener(seenListener);
        }
        Status("offline");
    }

}