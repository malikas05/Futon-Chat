package com.malikas.malikaschat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriends;
    private Button mProfileRequestBtn, mProfileDeclineReqBtn;

    private DatabaseReference database;
    private DatabaseReference friendRequestDatabase;
    private DatabaseReference friendDatabase;
    private DatabaseReference notificationDatabase;
    private DatabaseReference roofRefDatabase;
    private FirebaseUser currentUser;

    private ProgressDialog mProgressDialog;

    private String mCurrent_state;

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser != null)
            roofRefDatabase.child("Users").child(currentUser.getUid()).child("online").setValue("true");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (currentUser != null)
            roofRefDatabase.child("Users").child(currentUser.getUid()).child("online").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");
        roofRefDatabase = FirebaseDatabase.getInstance().getReference();
        database = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        friendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        friendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        notificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage = (ImageView) findViewById(R.id.profile_image);
        mProfileName = (TextView) findViewById(R.id.profile_name);
        mProfileStatus = (TextView) findViewById(R.id.profile_status);
        mProfileFriends = (TextView) findViewById(R.id.profile_total_friends);
        mProfileRequestBtn = (Button) findViewById(R.id.profile_request_btn);
        mProfileDeclineReqBtn = (Button) findViewById(R.id.profile_decline_btn);

        mCurrent_state = "not_friends";

        mProfileDeclineReqBtn.setVisibility(View.INVISIBLE);
        mProfileDeclineReqBtn.setEnabled(false);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while we load user data.");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mProfileName.setText(dataSnapshot.child("name").getValue().toString());
                mProfileStatus.setText(dataSnapshot.child("status").getValue().toString());

                Picasso.with(ProfileActivity.this)
                        .load(dataSnapshot.child("image").getValue().toString())
                        .placeholder(R.drawable.profile_image)
                        .into(mProfileImage);

                if (currentUser.getUid().equals(user_id)){
                    mProfileRequestBtn.setEnabled(false);
                    mProfileRequestBtn.setVisibility(View.INVISIBLE);

                    mProfileDeclineReqBtn.setEnabled(false);
                    mProfileDeclineReqBtn.setVisibility(View.INVISIBLE);
                }

                // ---------------- FRIENDS LIST / REQUEST FEATURE
                friendRequestDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_id)){
                            String request_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            Toast.makeText(ProfileActivity.this, request_type, Toast.LENGTH_SHORT).show();
                            if (request_type.equals("received")){
                                mCurrent_state = "req_received";
                                mProfileRequestBtn.setText("Accept Friend Request");

                                mProfileDeclineReqBtn.setVisibility(View.VISIBLE);
                                mProfileDeclineReqBtn.setEnabled(true);
                            }
                            else if (request_type.equals("sent")){
                                mCurrent_state = "req_sent";
                                mProfileRequestBtn.setText("Cancel Friend Request");

                                mProfileDeclineReqBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineReqBtn.setEnabled(false);
                            }

                            mProgressDialog.dismiss();
                        }
                        else {
                            mCurrent_state = "not_friends";
                            mProfileRequestBtn.setText("Send Friend Request");

                            mProfileDeclineReqBtn.setVisibility(View.INVISIBLE);
                            mProfileDeclineReqBtn.setEnabled(false);
                            friendDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)){
                                        mCurrent_state = "friends";
                                        mProfileRequestBtn.setText("Unfriend this person");

                                    }
                                    else {
                                        mCurrent_state = "not_friends";
                                        mProfileRequestBtn.setText("Send Friend Request");
                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });
                        }
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

        mProfileRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProfileRequestBtn.setEnabled(false);

                // Not friends
                if (mCurrent_state.equals("not_friends")){
                    DatabaseReference newNotificationref = roofRefDatabase.child("notifications").child(user_id).push();
                    String newNotificationId = newNotificationref.getKey();

                    HashMap<String, String> notificationData = new HashMap<String, String>();
                    notificationData.put("from", currentUser.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + currentUser.getUid() + "/" + user_id + "/request_type", "sent"); // "/" is the same as .child() method
                    requestMap.put("Friend_req/" + user_id + "/" + currentUser.getUid() + "/request_type", "received");
                    requestMap.put("notifications/" + user_id + "/" + newNotificationId, notificationData);
                    roofRefDatabase.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null){
                                Toast.makeText(ProfileActivity.this, "There was some error in sending request", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                mCurrent_state = "req_sent";
                                mProfileRequestBtn.setText("Cancel Friend Request");
                            }
                            mProfileRequestBtn.setEnabled(true);
                        }
                    });
                }

                // cancel request state
                if (mCurrent_state.equals("req_sent")){
                    friendRequestDatabase
                            .child(currentUser.getUid())
                            .child(user_id)
                            .removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    friendRequestDatabase
                                            .child(user_id)
                                            .child(currentUser.getUid())
                                            .removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mProfileRequestBtn.setEnabled(true);
                                                    mCurrent_state = "not_friends";
                                                    mProfileRequestBtn.setText("Send Friend Request");

                                                    mProfileDeclineReqBtn.setVisibility(View.INVISIBLE);
                                                    mProfileDeclineReqBtn.setEnabled(false);
                                                }
                                            });
                                }
                            });
                }

                // request received state
                if (mCurrent_state.equals("req_received")){
                    final String currentDate = java.text.DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + currentUser.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + user_id + "/" + currentUser.getUid() + "/date", currentDate);

                    friendsMap.put("Friend_req/" + currentUser.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend_req/" + user_id + "/" + currentUser.getUid(), null);

                    roofRefDatabase.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null){
                                mProfileRequestBtn.setEnabled(true);
                                mCurrent_state = "friends";
                                mProfileRequestBtn.setText("Unfriend this person");

                                mProfileDeclineReqBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineReqBtn.setEnabled(false);
                            }
                            else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                // unfriend friend
                if (mCurrent_state.equals("friends")){
                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + currentUser.getUid() + "/" + user_id, null);
                    unfriendMap.put("Friends/" + user_id + "/" + currentUser.getUid(), null);

                    roofRefDatabase.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null){
                                mCurrent_state = "not_friends";
                                mProfileRequestBtn.setText("Send Friend Request");

                                mProfileDeclineReqBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineReqBtn.setEnabled(false);
                            }
                            else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                            mProfileRequestBtn.setEnabled(true);
                        }
                    });
                }
            }
        });
    }
}
