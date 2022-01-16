package com.abc.clone_chatb.Fragments;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.abc.clone_chatb.Object.Users;
import com.abc.clone_chatb.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.HashMap;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    CircularImageView profile_image;
    TextView username;
    DatabaseReference reference;
    FirebaseUser fuser;

    StorageReference storageReference;
    public static final int  IMAGE_REQUEST = 1;
    private Uri imageUri;
    //StorageTask :():An controllable task that uploads and fires events for success, progress and failure.
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profile_image = view.findViewById(R.id.profile_image);
        username = view.findViewById(R.id.username);

        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users users = snapshot.getValue(Users.class);
                assert users != null;
                username.setText(users.getUsername());
                if (users.getImageURL().equals("default")){
                    profile_image.setImageResource(R.mipmap.bob);
                }
                else {
                    Glide.with(getContext()).load(users.getImageURL()).into(profile_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImages();
            }
        });
        return view;
    }

    //Accessing photos from Gallery
    private void openImages() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Image"),IMAGE_REQUEST);
    }
    private String GetFileExtension(Uri uri) {
        ContentResolver contentResolver = Objects.requireNonNull(getContext()).getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    // Uploading Image to Firebase Storage
    private void uploadImage(){
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Uploading...");
        progressDialog.show();


        if (imageUri != null){
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis() +
                    "."+GetFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);

            //WTF is going on..... :(
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot , Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){
                        throw Objects.requireNonNull(task.getException());
                    }

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = ( Uri ) task.getResult();
                        assert downloadUri != null;
                        String mUri = downloadUri.toString();

                        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(fuser.getUid());

                        HashMap<String , Object> map = new HashMap<>();
                        map.put("imageURL",mUri);
                        reference.updateChildren(map);

                        progressDialog.dismiss();

                    }
                    else
                        Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        }
        else {
            Toast.makeText(getContext(), "No image Selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null){

            imageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(getContext(), "Upload in Progress", Toast.LENGTH_SHORT).show();
            }
            else {
                uploadImage();
            }
        }

    }
}