package com.malikas.malikaschat;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    @BindView(R.id.chats_list) RecyclerView mChatsList;

    private DatabaseReference mChatsDatabase;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView =  inflater.inflate(R.layout.fragment_chats, container, false);
        ButterKnife.bind(this, mMainView);

        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mChatsDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);
        mChatsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        rootRef = FirebaseDatabase.getInstance().getReference();

        mChatsList.setHasFixedSize(true);
        mChatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Friends, ChatsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, ChatsViewHolder>(
                Friends.class,
                R.layout.user_chat_single_layout,
                ChatsViewHolder.class,
                mChatsDatabase
        ) {
            @Override
            protected void populateViewHolder(final ChatsViewHolder viewHolder, Friends model, final int position) {

                final String user_id = getRef(position).getKey();
                mUsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String name = dataSnapshot.child("name").getValue().toString();
                        String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                        viewHolder.setName(name);
                        viewHolder.setUserImage(thumb_image, getContext());

                        if (dataSnapshot.hasChild("online")){
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(userOnline);
                        }

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user_id", user_id);
                                chatIntent.putExtra("user_name", name);
                                startActivity(chatIntent);
                            }
                        });

                        DatabaseReference messageRef = rootRef.child("messages").child(mCurrent_user_id).child(user_id);
                        Query messageQuery = messageRef.limitToLast(1);
                        messageQuery.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                Messages message = dataSnapshot.getValue(Messages.class);
                                if (message.getType().equals("text"))
                                    viewHolder.setMessage(message.getMessage());
                                else if (message.getType().equals("image"))
                                    viewHolder.setMessage("Photo");
                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        mChatsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ChatsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setMessage(String message) {
            TextView messageView = (TextView) mView.findViewById(R.id.user_chat_single_message);
            messageView.setText(message);
        }

        public void setName(String name){
            TextView nameView = (TextView) mView.findViewById(R.id.user_chat_single_name);
            nameView.setText(name);
        }

        public void setUserImage(String thumb_image, Context ctx) {
            CircleImageView userImage = (CircleImageView) mView.findViewById(R.id.user_chat_single_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.profile_image).into(userImage);
        }

        public void setUserOnline(String online_status){
            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.user_chat_single_online_icon);
            if (online_status.equals("true"))
                userOnlineView.setVisibility(View.VISIBLE);
            else
                userOnlineView.setVisibility(View.INVISIBLE);
        }
    }

}
