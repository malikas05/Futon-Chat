package com.malikas.malikaschat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Malik on 2018-01-14.
 */

public class SettingsMainActivity extends AppCompatActivity {

    @BindView(R.id.main_page_toolbar) Toolbar mToolBar;

    @BindView(R.id.recyclerSettings)
    RecyclerView recyclerSettings;
    MyRecyclerViewAdapter adapter;

    private DatabaseReference mUserRef;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_settings);
        ButterKnife.bind(this);

        mToolBar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());
        }

        ArrayList<String> options = new ArrayList<>();
        options.add("Profile");
        options.add("Notifications");
        options.add("Reports");
        options.add("Appearance");
        options.add("Log out");

        // set up the RecyclerView
        recyclerSettings.setHasFixedSize(true);
        recyclerSettings.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRecyclerViewAdapter(this, options);
        recyclerSettings.setAdapter(adapter);
    }

    public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

        private ArrayList<String> mData;
        private Context context;

        // data is passed into the constructor
        public MyRecyclerViewAdapter(Context context, ArrayList<String> data) {
            this.mData = data;
            this.context = context;
        }

        // inflates the row layout from xml when needed
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item_settings, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            final String option = mData.get(position);
            holder.myTextView.setText(option);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (position == 4){
                        logOut();
                    }
                }
            });
        }

        public void logOut(){
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
            mAuth.signOut();
            sendToStart();
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView myTextView;

            public ViewHolder(View itemView) {
                super(itemView);
                myTextView = (TextView) itemView.findViewById(R.id.textViewOption);
            }
        }
    }

    private void sendToStart() {
        Intent startIntent = new Intent(this, StartActivity.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startIntent);
        finish();
    }
}
