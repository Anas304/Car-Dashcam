package com.abc.clone_chatb.Notifications;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        FirebaseUser  firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String refeshToken = FirebaseInstanceId.getInstance().getToken();

        if (firebaseUser != null){
            updateToken(refeshToken);
        }
    }

    private void updateToken(String refreshToken){
        FirebaseUser  firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");

        Token token = new Token(refreshToken);
        assert firebaseUser != null;
        reference.child(firebaseUser.getUid()).setValue(token);
    }
}
