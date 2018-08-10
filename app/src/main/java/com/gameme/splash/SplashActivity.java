package com.gameme.splash;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import com.gameme.HomeActivity;
import com.gameme.R;
import com.gameme.login.model.User;
import com.gameme.login.view.LoginActivity;
import com.gameme.utils.SharedPreferencesManager;
import com.google.firebase.auth.FirebaseAuth;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.gameme.utils.Constants.IS_COMPLETED;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE); //Removing ActionBar
        setContentView(R.layout.splash);
        startActivity();
    }


    private void startActivity() {
        //  Initialize SharedPreferences
        SharedPreferences getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());

        //  Create a new boolean and preference and set it to true
        boolean isFirstStart = getPrefs.getBoolean("firstStart", true);

        //  If the activity has never started before...
        if (isFirstStart) {

            //  Launch app intro
            Intent i = new Intent(SplashActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);

            //  Make a new preferences editor
            SharedPreferences.Editor e = getPrefs.edit();

            //  Edit preference to make it false because we don't want this to run again
            e.putBoolean("firstStart", false);

            //  Apply changes
            e.apply();
        } else {
            // get current user
            User currentUser = SharedPreferencesManager.getLoggedUserObject();
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            if (mAuth.getCurrentUser() != null) {
                if (currentUser != null) {
                    Intent homeIntent = new Intent(SplashActivity.this, HomeActivity.class);
                    homeIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(homeIntent);
                } else
                    launchLoginActivity(true);
            } else {
                launchLoginActivity(false);
            }
            finish();
        }

    }

    private void launchLoginActivity(boolean isRegisteredButNotCompleted) {
        Intent homeIntent = new Intent(SplashActivity.this, LoginActivity.class);
        if (isRegisteredButNotCompleted)
            homeIntent.putExtra(IS_COMPLETED, false);
        homeIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
    }

}
