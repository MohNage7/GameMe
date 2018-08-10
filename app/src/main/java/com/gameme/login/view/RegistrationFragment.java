package com.gameme.login.view;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.gameme.R;
import com.gameme.base.BaseFragment;
import com.gameme.firebase.FirebaseConstants;
import com.gameme.firebase.FirebaseUtils;
import com.gameme.login.model.User;
import com.gameme.utils.Constants;
import com.gameme.utils.SharedPreferencesManager;
import com.gameme.utils.StringUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.gameme.utils.StringUtils.isPasswordValid;


public class RegistrationFragment extends BaseFragment {
    // UI references.
    @BindView(R.id.register_email)
    AutoCompleteTextView mEmailView;
    @BindView(R.id.register_password)
    EditText mPasswordView;
    @BindView(R.id.register_fullname)
    EditText mFullNameView;
    boolean cancel;
    View focusView;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;
    private Unbinder unbinder;

    public RegistrationFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseUtils.getReference(FirebaseConstants.Childs.USERS_REF);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View registerView = inflater.inflate(R.layout.fragment_registeration, container, false);
        unbinder = ButterKnife.bind(this, registerView);

        return registerView;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().getSupportFragmentManager().popBackStack();

        }
        return true;
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */

    @OnClick(R.id.email_register_button)
    void attemptRegistration() {

        // Reset errors.
        mPasswordView.setError(null);
        mEmailView.setError(null);

        // Store values at the time of the login attempt.
        final String fullName = mFullNameView.getText().toString();
        String password = mPasswordView.getText().toString();
        final String email = mEmailView.getText().toString();

        cancel = false;
        focusView = null;

        if (TextUtils.isEmpty(fullName)) {
            mFullNameView.setError(getString(R.string.error_field_required));
            focusView = mFullNameView;
            cancel = true;

        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!StringUtils.validateEmail(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            showLoadingDialog(getString(R.string.sign_in));
            // perform the user Registration attempt.
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        // create new child called userId into user child
                        DatabaseReference currentUserInfo = mDatabaseReference.child(userId);
                        User currentUser = new User();
                        currentUser.setUserId(userId);
                        currentUser.setFullName(fullName);
                        currentUser.setEmail(email);
                        currentUser.setProfilePicture("");
                        currentUser.setAccountType(User.EMAIL);
                        currentUser.setPrivate(false);
                        currentUser.setPostsCount(0);
                        currentUser.setVoteCount(0);
                        currentUserInfo.setValue(currentUser);
                        // getCurrentUserInfo();
                        hideLoadingDialog();
                        SharedPreferencesManager.saveString(Constants.QuickSharedPref.LOGIN_METHOD, User.EMAIL);
                        onFragmentInteractionListener.setCurrentFragment(UserNameFragment.newInstance(), null);


                    } else {
                        task.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                hideLoadingDialog();
                                if (e.getMessage().equals(getString(R.string.email_taken_before))) {
                                    focusView = mEmailView;
                                    mEmailView.setError(getString(R.string.email_taken_before));
                                } else if (e.getMessage().equals(getString(R.string.email_badly_formatted))) {
                                    mEmailView.setError(getString(R.string.error_invalid_email));
                                    focusView = mEmailView;
                                } else {
                                    Toast.makeText(getActivity(), "Registration Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
