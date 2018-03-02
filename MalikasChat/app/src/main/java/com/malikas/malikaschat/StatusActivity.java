package com.malikas.malikaschat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout mStatus;
    private Button mSaveBtn;
    private ProgressDialog mProgress;

    //Firebase
    private DatabaseReference database;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onStart() {
        super.onStart();
        if (mCurrentUser != null)
            database.child("online").setValue("true");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCurrentUser != null)
            database.child("online").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        String status_value = getIntent().getStringExtra("status_value");

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        mToolbar = (Toolbar) findViewById(R.id.status_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mStatus = (TextInputLayout) findViewById(R.id.status_input);
        mStatus.getEditText().setText(status_value);
        mSaveBtn = (Button) findViewById(R.id.status_save_btn);

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Progress
                mProgress = new ProgressDialog(StatusActivity.this);
                mProgress.setTitle("Saving Changes");
                mProgress.setMessage("Please wait while we save the changes");
                mProgress.show();

                String status = mStatus.getEditText().getText().toString();
                database.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mProgress.dismiss();
                        }
                        else {
                            Toast.makeText(StatusActivity.this, "There was some error in saving changes.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }
}
