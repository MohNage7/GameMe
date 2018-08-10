package com.gameme.login.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.gameme.HomeActivity;
import com.gameme.R;
import com.gameme.base.BaseFragment;
import com.gameme.firebase.FirebaseConstants;
import com.gameme.firebase.FirebaseUtils;
import com.gameme.login.model.User;
import com.gameme.utils.Constants;
import com.gameme.utils.InternetConnection;
import com.gameme.utils.SharedPreferencesManager;
import com.gameme.utils.StringUtils;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class SignInFragment extends BaseFragment {

    public static final int RC_SIGN_IN = 9001;
    @BindView(R.id.password)
    EditText mPasswordView;
    @BindView(R.id.email)
    EditText mEmailView;
    @BindView(R.id.loginFacebookButton)
    LoginButton facebookLoginButton;
    @BindView(R.id.login_layout)
    LinearLayout focusLayout;
    DatabaseReference mUserDatabase;
    FirebaseUser firebaseUser;
    private FirebaseAuth mAuth;
    private CallbackManager callbackManager;
    private DatabaseReference mDatabaseReference;
    private ValueEventListener valueEventListener;
    private Unbinder unbinder;
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInAccount googleAccount;

    public SignInFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        callbackManager = CallbackManager.Factory.create();
        mDatabaseReference = FirebaseUtils.getReference(FirebaseConstants.Childs.USERS_REF);
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.WEB_CLIENT_ID))
                .build();

        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .enableAutoManage(getActivity(), new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        }
                    })
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View loginView = inflater.inflate(R.layout.fragment_login, container, false);

        unbinder = ButterKnife.bind(this, loginView);
        focusLayout.requestFocus();

        // loginByTwitter();
        facebookLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginByFacebook();
            }
        });
        return loginView;
    }

    @OnClick(R.id.facebook_sign_in_button)
    void onFacebookButtonClicked() {
        if (InternetConnection.isNetworkAvailable(mContext))
            facebookLoginButton.performClick();
        else
            Toast.makeText(mContext, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.google_sign_in_button)
    void onGoogleButtonClicked() {
        if (InternetConnection.isNetworkAvailable(mContext)) {
            showLoadingDialog(mContext.getString(R.string.wait));
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        } else
            Toast.makeText(mContext, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();


    }

    @OnClick(R.id.forget_TxtView)
    void forgetPassword() {
        if (InternetConnection.isNetworkAvailable(mContext))
            onFragmentInteractionListener.setCurrentFragment(new ResetPasswordFragment(), null);
        else
            Toast.makeText(mContext, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();

    }


    //  @OnClick(R.id.loginFacebookButton)
    void loginByFacebook() {
        List<String> permissionNeeds = Arrays.asList("public_profile", "email");
        facebookLoginButton.setReadPermissions(permissionNeeds);
        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // dialog
                showLoadingDialog(mContext.getString(R.string.wait));
                // handle facebook accessTokken
                signInWithCredential(loginResult.getAccessToken(), null, User.FACEBOOK);

            }

            @Override
            public void onCancel() {
                Toast.makeText(mContext, "Login Canceled", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(mContext, "Login Failed", Toast.LENGTH_LONG).show();

            }
        });


    }

    private void signInWithCredential(final AccessToken fbAccessToken, GoogleSignInAccount googleAccount, final @User.AccountType String accountType) {
        AuthCredential credential;
        if (accountType.equals(User.FACEBOOK)) {
            credential = FacebookAuthProvider.getCredential(fbAccessToken.getToken());

        } else {
            credential = GoogleAuthProvider.getCredential(googleAccount.getIdToken(), null);
        }

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // check if the user exist in firebase database or not
                            verifyAccount(accountType);
                        }
                        if (!task.isSuccessful()) {
                            hideLoadingDialog();
                            LoginManager.getInstance().logOut();
                        }

                    }
                });
    }


    /**
     * verify  if the user exist in firebase database or not
     * if not , jump to username fragment to complete registration
     */
    void verifyAccount(final String accountType) {
        final String userId = mAuth.getCurrentUser().getUid();
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userId)) {
                    getCurrentUserInfo();

                } else {
                    // save login method
                    SharedPreferencesManager.saveString(Constants.QuickSharedPref.LOGIN_METHOD, accountType);
                    if (accountType.equals(User.GOOGLE))
                        setupGoogleAccount();
                    else {
                        hideLoadingDialog();
                        onFragmentInteractionListener.setCurrentFragment(UserNameFragment.newInstance(), null);
                    }
                }
                mDatabaseReference.removeEventListener(this);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                hideLoadingDialog();
            }
        };

        mDatabaseReference.addValueEventListener(valueEventListener);

    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            googleAccount = result.getSignInAccount();
            signInWithCredential(null, googleAccount, User.GOOGLE);

        } else {
            // Signed out, show unauthenticated UI.
            Toast.makeText(getActivity(), "Login Failed", Toast.LENGTH_SHORT).show();
            hideLoadingDialog();

        }
    }


    void setupGoogleAccount() {
        DatabaseReference currentUserInfo = mDatabaseReference.child(mAuth.getCurrentUser().getUid());
        User currentUser = new User();
        currentUser.setProfilePicture(googleAccount.getPhotoUrl().toString());
        currentUser.setFullName(googleAccount.getDisplayName());
        currentUser.setEmail(googleAccount.getEmail());
        currentUser.setAccountType(User.GOOGLE);
        currentUser.setUserId(mAuth.getCurrentUser().getUid());
        currentUser.setVoteCount(0);
        currentUserInfo.setValue(currentUser).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                hideLoadingDialog();
                onFragmentInteractionListener.setCurrentFragment(UserNameFragment.newInstance(), null);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideLoadingDialog();
                Toast.makeText(getActivity(), "Failed to register your account", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R.id.register_tv)
    void startRegistrationActivity() {
        onFragmentInteractionListener.setCurrentFragment(new RegistrationFragment(), null);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    @OnClick(R.id.email_sign_in_button)
    void validateDataAndLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !StringUtils.isPasswordValid(password)) {
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
            // perform the user login attempt.
            showLoadingDialog(getString(R.string.sign_in));

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        getCurrentUserInfo();

                    } else {
                        task.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                hideLoadingDialog();
                                Toast.makeText(mContext, "Invalid email/password ,please try again!", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });

        }
    }

    private void getCurrentUserInfo() {
        firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            mUserDatabase = mDatabaseReference.child(firebaseUser.getUid());
            // get data
            final ValueEventListener mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // get user info from database
                    User currentUser = dataSnapshot.getValue(User.class);

                    mUserDatabase.removeEventListener(this);

                    // save current user data offline
                    SharedPreferencesManager.saveLoggedUserObject(currentUser);
                    hideLoadingDialog();

                    startHomeActivity();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    hideLoadingDialog();

                }
            };

            mUserDatabase.addListenerForSingleValueEvent(mValueEventListener);
            // Keep copy of post listener so we can remove it when app stops
        }
    }

    private void startHomeActivity() {
        Intent homeIntent = new Intent(mContext, HomeActivity.class);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
        if (getActivity() != null)
            getActivity().finish();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        // Pass the activity result to the login button.
        //twitterLoginButton.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
        mGoogleApiClient = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}



