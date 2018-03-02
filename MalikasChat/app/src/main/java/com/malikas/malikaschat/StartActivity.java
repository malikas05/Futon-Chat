package com.malikas.malikaschat;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;

import com.malikas.malikaschat.fragment.LoginFragment;
import com.malikas.malikaschat.fragment.SignupFragment;
import com.malikas.malikaschat.fragment.StartupFragment;

public class StartActivity extends SingleFragmentActivity implements StartupFragment.Callbacks{

    // method for replacing fragments
    @Override
    public void changeFragment(int fragmentNum) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (fragmentNum == 1) {
            ft.replace(R.id.fragmentContainerMain, new StartupFragment());
        }
        else if (fragmentNum == 2) {
            ft.replace(R.id.fragmentContainerMain, new LoginFragment());
        }
        else if (fragmentNum == 3) {
            ft.replace(R.id.fragmentContainerMain, new SignupFragment());
        }
        ft.addToBackStack(null);
        ft.setTransition(android.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    protected Fragment createFragment(){
        return new StartupFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
