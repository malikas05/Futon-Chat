package com.malikas.malikaschat.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.malikas.malikaschat.MainActivity;
import com.malikas.malikaschat.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Malik on 2017-12-12.
 */

public class LoginFragment extends Fragment {

    public StartupFragment.Callbacks listener;

    @BindView(R.id.log_display_email) TextInputLayout mEmail;
    @BindView(R.id.log_display_password) TextInputLayout mPassword;
    @BindView(R.id.log_textView1) TextView log_textView1;
    @BindView(R.id.log_signup_btn) Button log_signup_btn;
    @BindView(R.id.log_login_btn) Button log_login_btn;

    // Firebase Auth
    private FirebaseAuth mAuth;
    private DatabaseReference database;

    //ProgressDialog
    private ProgressDialog mLogProgress;

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
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this, v);

        mLogProgress = new ProgressDialog(getActivity(), R.style.MyAlertDialogStyle);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference().child("Users");

        return v;
    }
    //

    @OnClick(R.id.log_signup_btn)
    public void signupBtnClick(){
        listener.changeFragment(3);
    }

    @OnClick(R.id.log_login_btn)
    public void loginBtnClick(){
        String email = mEmail.getEditText().getText().toString();
        String password = mPassword.getEditText().getText().toString();

        if (!email.isEmpty() || !password.isEmpty()){
            mLogProgress.setTitle("Logging User");
            mLogProgress.setMessage("Please wait while we check your credentials!");
            mLogProgress.setCanceledOnTouchOutside(false);
            mLogProgress.show();
            loginUser(email, password);
        }
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            database.child(mAuth.getCurrentUser().getUid()).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Intent mainIntent = new Intent(getContext(), MainActivity.class);
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainIntent);
                                    getActivity().finish();
                                }


                            });
                        }
                        else {
                            mLogProgress.hide();
                            Toast.makeText(getActivity(), "Cannot sign in. Please check the form and try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
