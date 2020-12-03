package com.devender.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendActivity extends AppCompatActivity
{
    private Toolbar mToolBar;
    private RecyclerView mFindFriendRecyclerView;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        mToolBar = (Toolbar)findViewById(R.id.find_friend_tool);
        mToolBar.setTitleTextColor(getResources().getColor(R.color.colorAccent));
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        mFindFriendRecyclerView = findViewById(R.id.find_friend_recylcer_list);
        mFindFriendRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(mDatabaseReference,Contacts.class).build();

        final FirebaseRecyclerAdapter<Contacts,FindFriendViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options)
                {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull Contacts model)
            {
                holder.mUserName.setText(model.getName());
                holder.mUserStatus.setText(model.getStatus());
                System.out.println(model.getImage());
                Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.mImageView);

                holder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        String visit_user_id = getRef(position).getKey();
                        Intent intent = new Intent(FindFriendActivity.this, ProfileActivity.class);
                        intent.putExtra("visit_user_id", visit_user_id);
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout , parent,false);
                FindFriendViewHolder findFriendViewHolder = new FindFriendViewHolder(view);
                return findFriendViewHolder;
            }

        }  ;
        mFindFriendRecyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder
    {

        TextView mUserName , mUserStatus ;
        CircleImageView mImageView;

        public FindFriendViewHolder(@NonNull View itemView)
        {
            super(itemView);

            mImageView = itemView.findViewById(R.id.user_person_image);
            mUserName = itemView.findViewById(R.id.user_profile_name);
            mUserStatus = itemView.findViewById(R.id.user_profile_status);
        }
    }
}
