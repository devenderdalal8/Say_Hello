package com.devender.whatsapp.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.devender.whatsapp.Contacts;
import com.devender.whatsapp.ChatActivity;
import com.devender.whatsapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment {
    private RecyclerView chatList;

    private DatabaseReference ChatRef, UsersRef, ContactsRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    public ChatsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View chatView= inflater.inflate(R.layout.fragment_chats, container, false);

        chatList = chatView.findViewById(R.id.chat_list);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        return chatView ;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(ChatRef,Contacts.class).build();
        FirebaseRecyclerAdapter <Contacts,ChatViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ChatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatViewHolder chatViewHolder, final int i, @NonNull Contacts contacts) {
                final String userIDs = getRef(i).getKey();
                final String[] retimage = {"default_image"};
                UsersRef.child(userIDs).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.exists())
                        {
                            if (dataSnapshot.hasChild("image")) {
                                retimage[0] = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(retimage[0]).placeholder(R.drawable.person_image).into(chatViewHolder.profileImage);

                            }
                            final String retUserName = dataSnapshot.child("name").getValue().toString();

                            chatViewHolder.userName.setText(retUserName);
                            if (dataSnapshot.child("userstate").hasChild("state")) {

                                String state = dataSnapshot.child("userstate").child("state").getValue().toString();
                                String date = dataSnapshot.child("userstate").child("date").getValue().toString();
                                String time = dataSnapshot.child("userstate").child("time").getValue().toString();

                                if (state.equals("online")) {
                                    chatViewHolder.userStatus.setText("Online");
                                }
                                if (state.equals("offline")) {
                                    chatViewHolder.userStatus.setText("Last Seen : "  + date + " " + time);
                                }
                            } else {
                                chatViewHolder.userStatus.setText("Offline");
                            }
                            chatViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getContext(), ChatActivity.class);
                                    intent.putExtra("visit_user_ids", userIDs);
                                    intent.putExtra("visit_user_name", retUserName);
                                    intent.putExtra("visit_image", retimage[0]);
                                    startActivity(intent);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
            @NonNull
            @Override
            public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout, parent, false);
                return new ChatViewHolder(view);

            }
        };
        chatList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, userStatus;
        CircleImageView profileImage;
        Button AcceptButton, CancelButton;

        public ChatViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_profile_status);
            profileImage = itemView.findViewById(R.id.user_person_image);
            AcceptButton = itemView.findViewById(R.id.request_accept_button);
            CancelButton = itemView.findViewById(R.id.request_cancel_button);
        }
    }
}