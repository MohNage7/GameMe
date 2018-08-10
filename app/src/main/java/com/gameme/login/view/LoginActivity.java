package com.gameme.login.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.gameme.HomeActivity;
import com.gameme.R;
import com.gameme.base.OnFragmentInteractionListener;
import com.gameme.utils.SharedPreferencesManager;
import com.google.firebase.auth.FirebaseAuth;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import static com.gameme.utils.Constants.IS_COMPLETED;


public class LoginActivity extends AppCompatActivity implements OnFragmentInteractionListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (savedInstanceState == null)
            updateUI();
    }

    private void updateUI() {
        if (isUserProfileComplete()) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            if (mAuth.getCurrentUser() != null) {
                Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(homeIntent);
                finish();
            } else {
                setCurrentFragment(new SignInFragment(), null);
            }
        } else {
            if (SharedPreferencesManager.getLoggedUserObject() != null)
                setCurrentFragment(UserNameFragment.newInstance(), null);
            else {
                FirebaseAuth.getInstance().signOut();
                setCurrentFragment(new SignInFragment(), null);
            }
        }
    }

    private boolean isUserProfileComplete() {
        return getIntent().getBooleanExtra(IS_COMPLETED, true);
    }


    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().findFragmentById(R.id.login_frame) instanceof SignInFragment) {
            new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(getString(R.string.exit_app))
                    .setContentText(getString(R.string.are_u_sure_exit))
                    .setCancelText(getString(R.string.no))
                    .setConfirmText(getString(R.string.yes))
                    .showCancelButton(true)
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                        }
                    })
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {

                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            finish();
                        }
                    }).show();

        } else if (getSupportFragmentManager().findFragmentById(R.id.login_frame) instanceof UserNameFragment) {
            getSupportFragmentManager().popBackStack();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SignInFragment fragment = (SignInFragment) getSupportFragmentManager()
                .findFragmentById(R.id.login_frame);
        fragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void setCurrentFragment(Fragment newFragment, String tag) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.login_frame, newFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}

