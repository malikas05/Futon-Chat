package com.malikas.malikaschat.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.malikas.malikaschat.MainActivity;
import com.malikas.malikaschat.ProfileActivity;
import com.malikas.malikaschat.R;
import com.malikas.malikaschat.StartActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Malik on 2017-12-12.
 */

public class StartupFragment extends Fragment {

    private FirebaseUser currentUser;

    public interface Callbacks{
        void changeFragment(int fragmentNum);
    }

    public Callbacks listener;
    Animation myanim;

    @BindView(R.id.startup_logo) ImageView startup_logo;

    //lifecycle methods
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.listener = (Callbacks)context;
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
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_startup, container, false);
        ButterKnife.bind(this, v);

        myanim = AnimationUtils.loadAnimation(getActivity(), R.anim.mytransition);
        startup_logo.startAnimation(myanim);
        checkIfAnimFinished();

//        Thread thread = new Thread(){
//            @Override
//            public void run() {
//                try {
//                    sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                finally {
//                    if (currentUser == null) {
//                        if (listener != null)
//                            listener.changeFragment(2);
//                    }
//                    else {
//                        sendToMain();
//                    }
//                }
//            }
//        };
//        thread.start();
        return v;
    }
    //

    private void checkIfAnimFinished(){
        myanim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (currentUser == null) {
                    if (listener != null)
                        listener.changeFragment(2);
                }
                else {
                    sendToMain();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void sendToMain() {
        Log.d("check", "here");
        startActivity(new Intent(getContext(), MainActivity.class));
        getActivity().finish();
    }
}
