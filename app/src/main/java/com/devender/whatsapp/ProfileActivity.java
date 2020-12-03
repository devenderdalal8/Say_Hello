package com.devender.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roger.catloadinglibrary.CatLoadingView;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String mRecivingUserId,mCurrentUserId , mSenderUserId, currentState;
    private CircleImageView mProfileImage;
    private TextView mUserName , mStatus;
    private Button mSendRequestButton , mCancelRequestButton;
    private CatLoadingView mView;
    private DatabaseReference mDatabaseReference , mChatRequestDatabaseReference ,mContactDatabaseReference , mNotificationReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        mSenderUserId = mAuth.getCurrentUser().getUid();
        mRecivingUserId = getIntent().getExtras().get("visit_user_id").toString();
        mProfileImage = findViewById(R.id.set_profile_image);
        mUserName = findViewById(R.id.set_user_name);
        mStatus = findViewById(R.id.set_profile_status);
        mSendRequestButton = findViewById(R.id.update_settings_button);
        mCancelRequestButton = findViewById(R.id.visit_cancel_button);
        currentState = "new";

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mChatRequestDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        mContactDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Contacts");
        mNotificationReference = FirebaseDatabase.getInstance().getReference().child("Notifications");
        retriveUserInfo();
    }

    private void retriveUserInfo() {
        mDatabaseReference.child(mRecivingUserId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && (snapshot.hasChild("image"))) {

                    String userImage = snapshot.child("image").getValue().toString();
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();
                    Picasso.get().load(userImage).placeholder(R.drawable.person_image).into(mProfileImage);

                    mUserName.setText(userName);

                    mStatus.setText(userStatus);

                    manageChatRequest();
                }
                else {
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();
                    mUserName.setText(userName);
                    mStatus.setText(userStatus);
                    manageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }) ;
    }

    private void manageChatRequest(){

        mChatRequestDatabaseReference.child(mSenderUserId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild(mRecivingUserId)) {

                    String request_type = dataSnapshot.child(mRecivingUserId).child("request_type").getValue().toString();
                    if (request_type.equals("sent")) {
                        currentState = "request_type";
                        mSendRequestButton.setText("Cancel Chat Request");
                    }
                    else if (request_type.equals("received")) {
                        currentState = "request_received";
                        mSendRequestButton.setText("Accept Chat Request");
                        mCancelRequestButton.setVisibility(View.VISIBLE);
                        mCancelRequestButton.setEnabled(true);
                        mCancelRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelChatRequest();
                            }
                        });
                    }

                }
                else
                {
                    mContactDatabaseReference.child(mSenderUserId).addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(mRecivingUserId)) {
                                currentState = "friends";
                                mSendRequestButton.setText("Remove Friend");
                                
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if (!(mSenderUserId.equals(mRecivingUserId))) {
            mSendRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSendRequestButton.setEnabled(false);
                    if (currentState.equals("new")) {
                        sendChatRequest();
                    }
                    if (currentState.equals("request_type")) {
                        cancelChatRequest();
                    }
                    if (currentState.equals("request_received")) {
                        acceptChatRequest();
                    }
                    if (currentState.equals("friends")) {
                        removeSpecificContacts();
                        
                    }
                }
            });

        } else {
            mSendRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void removeSpecificContacts() {
        mContactDatabaseReference.child(mSenderUserId).child(mRecivingUserId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mContactDatabaseReference.child(mRecivingUserId).child(mSenderUserId)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mSendRequestButton.setEnabled(true);
                                currentState = "new";
                                mSendRequestButton.setText("Send Messages");
                            }
                        }
                    });
                }
            }
        });

        
    }

    private void acceptChatRequest() {
        mContactDatabaseReference.child(mSenderUserId).child(mRecivingUserId).child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mChatRequestDatabaseReference.child(mSenderUserId).child(mRecivingUserId)
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mChatRequestDatabaseReference.child(mRecivingUserId).child(mSenderUserId)
                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                    mSendRequestButton.setEnabled(true);
                                                    currentState = "friends";
                                                    mSendRequestButton.setText("Remove Friend");
                                                    mCancelRequestButton.setVisibility(View.INVISIBLE);
                                                    mCancelRequestButton.setEnabled(false);
                                                }
                                            }
                                        });
                                    }
                                }
                            });

                        }
                    }
                });
    }

    private void cancelChatRequest() {
        mChatRequestDatabaseReference.child(mSenderUserId).child(mRecivingUserId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mChatRequestDatabaseReference.child(mRecivingUserId).child(mSenderUserId)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mSendRequestButton.setEnabled(true);
                                currentState = "new";
                                mSendRequestButton.setText("Send Messages");
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendChatRequest() {
        mChatRequestDatabaseReference.child(mSenderUserId).child(mRecivingUserId).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            mChatRequestDatabaseReference.child(mRecivingUserId).child(mSenderUserId).child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Map<String, String> chatNotification = new HashMap<>();
                                                chatNotification.put("from", mSenderUserId );
                                                chatNotification.put("type", "request");

                                                mNotificationReference.child(mRecivingUserId).push().setValue(chatNotification).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            mSendRequestButton.setEnabled(true);
                                                            currentState = "request_sent";
                                                            mSendRequestButton.setText("Cancel Chat Request");
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                        }

                    }
                });
    }
}