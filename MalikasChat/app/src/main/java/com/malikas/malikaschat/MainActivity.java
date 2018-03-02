package com.malikas.malikaschat;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private CircleImageView circleImageView;
    private TabLayout mTabLayout;
    private FloatingActionMenu fabMenu;
    private FloatingActionButton main_users_fab, main_new_message_fab;

    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private DatabaseReference mUserRef;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mToolBar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        circleImageView = (CircleImageView) findViewById(R.id.custom_bar_image);
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });

        fabMenu = (FloatingActionMenu) findViewById(R.id.fabMenu);
        fabMenu.setClosedOnTouchOutside(true);
        main_users_fab = (FloatingActionButton) findViewById(R.id.main_users_fab);
        main_new_message_fab = (FloatingActionButton) findViewById(R.id.main_new_message_fab);
        main_users_fab.setOnClickListener(clickListener);
        main_new_message_fab.setOnClickListener(clickListener);

        currentUser = mAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());

            // Tabs
            mViewPager = (ViewPager) findViewById(R.id.mainTabPager);
            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
            mViewPager.setAdapter(mSectionsPagerAdapter);

            mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
            mTabLayout.setupWithViewPager(mViewPager);



            mUserRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    final String image = dataSnapshot.child("image").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();
                    String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

//                    mName.setText(name);
//                    mStatus.setText(status);
                    if (!image.equals("default"))
//                    Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.malik).into(mImage);
                        Picasso.with(MainActivity.this).load(image)
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.profile_image).into(circleImageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.profile_image).into(circleImageView);
                            }
                        });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewPager.setCurrentItem(1);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null){
            sendToStart();
        }
        else {
            mUserRef.child("online").setValue("true");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (currentUser != null) {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    private void sendToStart() {
        startActivity(new Intent(this, StartActivity.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.main_settings_btn){
            Intent settingsIntent = new Intent(MainActivity.this, SettingsMainActivity.class);
            startActivity(settingsIntent);
        }

        return true;
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    RequestsFragment requestsFragment = new RequestsFragment();
                    return requestsFragment;
                case 1:
                    ChatsFragment chatsFragment = new ChatsFragment();
                    return chatsFragment;
                case 2:
                    FriendsFragment friendsFragment = new FriendsFragment();
                    return friendsFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        // sets title text for tabs
        public CharSequence getPageTitle(int position){
            switch (position){
                case 0:
                    return "REQUESTS";
                case 1:
                    return "CHATS";
                case 2:
                    return "FRIENDS";
                default:
                    return null;
            }
        }
    }

    // click listener for fab buttons
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.main_users_fab:
                    Intent usersIntent = new Intent(MainActivity.this, UsersActivity.class);
                    startActivity(usersIntent);
                    break;
                case R.id.main_new_message_fab:
                    mViewPager.setCurrentItem(2);
                    break;
            }
        }
    };
}
