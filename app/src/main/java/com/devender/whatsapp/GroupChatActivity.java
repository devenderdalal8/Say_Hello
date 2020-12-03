package com.devender.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.icu.text.Edits;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;


public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private ImageButton mSendButton;
    private EditText mUserMessage;
    private ScrollView mScrollView;
    private TextView mDisplayMessage;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference,mGroupNameRef , mGroupMessageKeyRef;
    private String mCurrentGroupName,mCurrentUserId,mCurrentUserName ,mCurrentDate,mCurrentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        mCurrentGroupName = getIntent().getExtras().get("groupName").toString();

        mToolBar = findViewById(R.id.group_chat_bar_layout);
        mToolBar.setTitleTextColor(getResources().getColor(R.color.colorAccent));
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle(mCurrentGroupName);

        mSendButton = findViewById(R.id.send_message_button);
        mUserMessage = findViewById(R.id.input_group_message);
        mDisplayMessage = findViewById(R.id.group_chat_view);
        mScrollView = findViewById(R.id.scroll_view);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mGroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(mCurrentGroupName);
        
        getUserInfo();

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMessageIntoDatabase();
                mUserMessage.setText("");
            }
        });
        }

    private void getUserInfo() {
        mDatabaseReference.child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (snapshot.exists()) {
                    mCurrentUserName = snapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void saveMessageIntoDatabase() {
        String message = mUserMessage.getText().toString();
        String messageKey = mGroupNameRef.push().getKey();
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please Write some Messages..", Toast.LENGTH_SHORT).show();
        } else {
            Calendar currentDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd,yyyy");
            mCurrentDate = currentDateFormat.format(currentDate.getTime());

            Calendar currentTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            mCurrentTime = currentTimeFormat.format(currentTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            mGroupNameRef.updateChildren(groupMessageKey);

            mGroupMessageKeyRef = mGroupNameRef.child(messageKey);

            HashMap<String,Object> messageInfoMap = new HashMap<>();
                messageInfoMap.put("name", mCurrentUserName);
                messageInfoMap.put("message", message);
                messageInfoMap.put("date", mCurrentDate);
                messageInfoMap.put("time",mCurrentTime);
            mGroupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mGroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                if (snapshot.exists()) {
                    displayMessage(snapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    displayMessage(snapshot);
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void displayMessage(DataSnapshot snapshot) {
        Iterator iterator = snapshot.getChildren().iterator();
        while (iterator.hasNext()) {
            String chatDate = (String)((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String)((DataSnapshot)iterator.next()).getValue();
            String chatName = (String)((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String)((DataSnapshot)iterator.next()).getValue();

            mDisplayMessage.append(chatName + ":\n"+ chatMessage+" \n" +chatTime+"   "+ chatDate+"\n\n\n" );
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
}