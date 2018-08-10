package com.gameme.login.view;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.internal.ImageDownloader;
import com.facebook.internal.ImageRequest;
import com.facebook.internal.ImageResponse;
import com.gameme.HomeActivity;
import com.gameme.R;
import com.gameme.base.BaseFragment;
import com.gameme.firebase.FirebaseConstants;
import com.gameme.firebase.FirebaseUtils;
import com.gameme.login.model.User;
import com.gameme.utils.Constants;
import com.gameme.utils.SharedPreferencesManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.gameme.utils.StringUtils.validateUsername;


public class UserNameFragment extends BaseFragment {

    @BindView(R.id.userName_EditTxt)
    EditText userNameEdtTxt;
    private AccessToken accessToken;
    private String loginMethod;
    private String userId;
    private Unbinder unbinder;
    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mAuth;
    private ValueEventListener valueEventListener;
    private JSONObject facebookUserData;
    private ValueEventListener mUserListener;
    private DatabaseReference mUserDatabase;
    private boolean continueBtnClicked;

    public UserNameFragment() {
        // Required empty public constructor
    }

    public static UserNameFragment newInstance() {
        return new UserNameFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabaseReference = FirebaseUtils.getReference(FirebaseConstants.Childs.USERS_REF);
        accessToken = AccessToken.getCurrentAccessToken();
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View usernameView = inflater.inflate(R.layout.fragment_user_name, container, false);
        loginMethod = SharedPreferencesManager.getString(Constants.QuickSharedPref.LOGIN_METHOD);
        unbinder = ButterKnife.bind(this, usernameView);
        userNameEdtTxt.setError(null);
        return usernameView;
    }

    @OnClick(R.id.continue_Btn)
    void continueToHome() {
        final String username = userNameEdtTxt.getText().toString();
        Query userNameReference = mDatabaseReference.orderByChild("username").equalTo(username);
        if (!TextUtils.isEmpty(username)) {
            if (validateUsername(username)) {
                ValueEventListener usernameValueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            userNameEdtTxt.setError(getString(R.string.username_taken_before));
                            continueBtnClicked = false;
                        } else {
                            showLoadingDialog(getString(R.string.creating_account));
                            switch (loginMethod) {
                                case User.FACEBOOK:
                                    setupFacebookAccount(username);
                                    break;
                                case User.EMAIL:
                                    setupEmailAccount(username);
                                    break;
                                case User.GOOGLE:
                                    setupGoogleAccount(username);
                                    break;
                            }
                            continueBtnClicked = true;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        hideLoadingDialog();

                    }
                };

                userNameReference.addListenerForSingleValueEvent(usernameValueEventListener);

            } else {
                userNameEdtTxt.setError(" # username can contain  a-z, 0-9, underscore, hyphen\n" +
                        "# user name Length at least 3 characters and maximum length of 10");
            }
        } else {
            userNameEdtTxt.setError(getString(R.string.error_field_required));
        }
    }


    private void setupGoogleAccount(final String username) {
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userId)) {
                    DatabaseReference currentUserInfo = mDatabaseReference.child(userId).child("username");
                    currentUserInfo.setValue(username);
                    getCurrentUserInfo();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                hideLoadingDialog();

            }
        };
        mDatabaseReference.addValueEventListener(valueEventListener);
    }


    private void setupEmailAccount(final String username) {

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userId)) {
                    DatabaseReference currentUserInfo = mDatabaseReference.child(userId).child("username");
                    currentUserInfo.setValue(username);
                    getCurrentUserInfo();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                hideLoadingDialog();

            }
        };

        mDatabaseReference.addValueEventListener(valueEventListener);

    }

    void setupFacebookAccount(final String username) {

        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        facebookUserData = object;
                        try {
                            String email = object.getString("email");
                            String name = object.getString("name");
                            // get image
                            ImageRequest request = getImageRequest();
                            Uri requestUri = request.getImageUri();
                            ImageDownloader.downloadAsync(request);
                            // save user info
                            DatabaseReference currentUserInfo = mDatabaseReference.child(userId);
                            User currentUser = new User();
                            currentUser.setUserId(userId);
                            currentUser.setFullName(name);
                            currentUser.setUsername(username);
                            currentUser.setEmail(email);
                            currentUser.setPostsCount(0);
                            currentUser.setVoteCount(0);
                            currentUser.setAccountType(User.FACEBOOK);
                            currentUser.setProfilePicture(requestUri.toString());
                            currentUserInfo.setValue(currentUser);

                            getCurrentUserInfo();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            //No value for email
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,gender");
        request.setParameters(parameters);
        request.executeAsync();


    }

    private void getCurrentUserInfo() {

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        mUserDatabase = mDatabaseReference.child(firebaseUser.getUid());

        // get data
        ValueEventListener mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (getActivity() != null)
                    getActivity().finish();
                // get user info from database
                User currentUser = dataSnapshot.getValue(User.class);
                // save current user data offline
                SharedPreferencesManager.saveLoggedUserObject(currentUser);
                hideLoadingDialog();
                Intent homeIntent = new Intent(mContext, HomeActivity.class);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(homeIntent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mUserDatabase.addListenerForSingleValueEvent(mValueEventListener);
        // Keep copy of post listener so we can remove it when app stops
        mUserListener = mValueEventListener;

    }

    private ImageRequest getImageRequest() {
        ImageRequest request;
        ImageRequest.Builder requestBuilder = new ImageRequest.Builder(
                mContext,
                ImageRequest.getProfilePictureUri(
                        facebookUserData.optString("id"),
                        getResources().getDimensionPixelSize(
                                R.dimen.image_size),
                        getResources().getDimensionPixelSize(
                                R.dimen.image_size)));

        request = requestBuilder.setCallerTag(this)
                .setCallback(
                        new ImageRequest.Callback() {
                            @Override
                            public void onCompleted(ImageResponse response) {

                            }
                        })
                .build();
        return request;
    }


    @Override
    public void onStop() {
        super.onStop();
        if (!continueBtnClicked)
            // Remove post value event listener
            if (mUserListener != null) {
                mUserDatabase.removeEventListener(mUserListener);
            }
        if (valueEventListener != null) {
            mDatabaseReference.removeEventListener(valueEventListener);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAuth.getCurrentUser() == null)
            onFragmentInteractionListener.setCurrentFragment(new SignInFragment(), null);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
