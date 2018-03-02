package com.malikas.malikaschat.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.malikas.malikaschat.MainActivity;
import com.malikas.malikaschat.R;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Malik on 2017-12-13.
 */

public class SignupFragment extends Fragment {

    public StartupFragment.Callbacks listener;

    @BindView(R.id.signup_username) TextInputLayout signup_username;
    @BindView(R.id.signup_display_email) TextInputLayout signup_display_email;
    @BindView(R.id.signup_display_password) TextInputLayout signup_display_password;
    @BindView(R.id.signup_signup_btn) Button signup_signup_btn;

    // Firebase Auth
    private FirebaseAuth mAuth;
    private DatabaseReference database;

    //ProgressDialog
    private ProgressDialog mSignupProgress;

    //lifecycle methods
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.listener = (StartupFragment.Callbacks)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.listener = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(getView() == null){
            return;
        }

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK){
                    // handle back button's click listener
                    listener.changeFragment(2);
                    return true;
                }
                return false;
            }
        });
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_signup, container, false);
        ButterKnife.bind(this, v);

        mSignupProgress = new ProgressDialog(getActivity());

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference().child("Users");

        return v;
    }
    //

    @OnClick(R.id.signup_signup_btn)
    public void signupBtnClick(){
        String username = signup_username.getEditText().getText().toString();
        String email = signup_display_email.getEditText().getText().toString();
        String password = signup_display_password.getEditText().getText().toString();

        if (!username.isEmpty() || !email.isEmpty() || !password.isEmpty()){
            mSignupProgress.setTitle("Registering User");
            mSignupProgress.setMessage("Please wait while we create your account!");
            mSignupProgress.setCanceledOnTouchOutside(false);
            mSignupProgress.show();
            registerUser(username, email, password);
        }
        else {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerUser(final String display_name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            String uId = currentUser.getUid();

                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            // saving data to database
                            database = FirebaseDatabase.getInstance().getReference().child("Users").child(uId);
                            HashMap<String, String> userMap = new HashMap<String, String>();
                            userMap.put("name", display_name);
                            userMap.put("status", "Hi there! I'm using Malikas Chat App.");
                            userMap.put("image", "default");
                            userMap.put("thumb_image", "default");
                            userMap.put("device_token", deviceToken);
                            database.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        mSignupProgress.dismiss();
                                        Intent mainIntent = new Intent(getContext(), MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        getActivity().finish();
                                    }
                                }
                            });
                        }
                        else {
                            mSignupProgress.hide();
                            Toast.makeText(getActivity(), "Cannot sign in. Please check the form and try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}
