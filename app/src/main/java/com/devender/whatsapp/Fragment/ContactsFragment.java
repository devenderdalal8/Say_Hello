package com.devender.whatsapp.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.devender.whatsapp.Contacts;
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

import java.util.zip.Inflater;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsFragment extends Fragment {
    private View mContactView;
    private RecyclerView mRecyclerView;

    private DatabaseReference mContactReference, mUserReference;
    private FirebaseAuth mAuth;
    private String mCurrentUID;

    public ContactsFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        mContactView= inflater.inflate(R.layout.fragment_contacts, container, false);
        mRecyclerView = mContactView.findViewById(R.id.contact_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        mCurrentUID = mAuth.getCurrentUser().getUid();
        mUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mContactReference = FirebaseDatabase.getInstance().getReference().child("Contacts").child(mCurrentUID);

        return mContactView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(mContactReference, Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts, ContactViewHolder > adapter = new FirebaseRecyclerAdapter<Contacts, ContactViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final ContactViewHolder holder, int i, @NonNull Contacts contacts)
            {
                String userIds = getRef(i).getKey();
                mUserReference.child(userIds).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            if (dataSnapshot.child("userstate").hasChild("state")) {

                                String state = dataSnapshot.child("userstate").child("state").getValue().toString();
                                String date = dataSnapshot.child("userstate").child("date").getValue().toString();
                                String time = dataSnapshot.child("userstate").child("time").getValue().toString();

                                if (state.equals("online")) {
                                    holder.onlineIcon.setVisibility(View.VISIBLE);
                                }
                                if (state.equals("offline")) {
                                    holder.onlineIcon.setVisibility(View.VISIBLE);
                                }
                            } else {
                                holder.onlineIcon.setVisibility(View.VISIBLE);
                            }
                            if (dataSnapshot.hasChild("image")) {
                                String userImage = dataSnapshot.child("image").getValue().toString();
                                String profileName = dataSnapshot.child("name").getValue().toString();
                                String profileStatus = dataSnapshot.child("status").getValue().toString();

                                holder.userName.setText(profileName);
                                holder.userStatus.setText(profileStatus);
                                Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.mImage);
                            } else {
                                String profileName = dataSnapshot.child("name").getValue().toString();
                                String profileStatus = dataSnapshot.child("status").getValue().toString();

                                holder.userName.setText(profileName);
                                holder.userStatus.setText(profileStatus);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout, parent, false);
                ContactViewHolder viewHolder = new ContactViewHolder(view);
                return viewHolder;
            }
        };

        mRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public  static class  ContactViewHolder extends  RecyclerView.ViewHolder {

        TextView userName , userStatus;
        CircleImageView mImage;
        ImageView onlineIcon;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_profile_status);
            mImage = itemView.findViewById(R.id.user_person_image);

            onlineIcon = itemView.findViewById(R.id.user_online_status);
        }
    }
}