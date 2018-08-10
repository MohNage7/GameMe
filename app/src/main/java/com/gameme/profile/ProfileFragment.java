package com.gameme.profile;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gameme.R;
import com.gameme.base.BaseFragment;
import com.gameme.firebase.FirebaseConstants;
import com.gameme.firebase.FirebaseUtils;
import com.gameme.firebase.controllers.UserController;
import com.gameme.login.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends BaseFragment {


    private static final String USER_NAME = "user_name";
    @BindView(R.id.profile_toolbar)
    Toolbar toolbar;
    @BindView(R.id.profile_userImage)
    CircleImageView profileImageView;
    @BindView((R.id.profile_cover_layout))
    RelativeLayout coverLayout;
    @BindView(R.id.profile_UserName_txtView)
    TextView userNameTxtView;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.profile_Posts_TxtView)
    TextView postsCount;
    @BindView(R.id.profile_Posts_Layout)
    LinearLayout postsLayout;
    @BindView(R.id.answersCountTxtView)
    TextView answersCountTxtView;
    @BindView(R.id.answerCountLayout)
    LinearLayout answerCountLayout;
    @BindView(R.id.votesCountTxtView)
    TextView votesCountTxtView;
    @BindView(R.id.votesCountLayout)
    LinearLayout votesCountLayout;

    Context mContext;
    Query mUsersReference;
    private ValueEventListener userValueEventListener;
    private User mUser;
    private Menu mMenu;
    private String username;
    private AppCompatActivity activity;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String username) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(USER_NAME, username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        if (getArguments() != null) {
            username = getArguments().getString(USER_NAME);
        }

        mUsersReference = FirebaseUtils.getReference(FirebaseConstants.Childs.USERS_REF).orderByChild("username").equalTo(username);

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View profileView = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, profileView);

        activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
        }
        collapsingToolbar.setExpandedTitleColor(ContextCompat.getColor(mContext, android.R.color.transparent));

        getUserByUserName();

        setHasOptionsMenu(true);

        return profileView;
    }


    void getUserByUserName() {
        userValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userObject : dataSnapshot.getChildren()) {
                        mUser = userObject.getValue(User.class);
                        if (mUser != null) {
                            setUserInfo(mUser);
                            handleViewsVisibility();

                        } else {
                            Toast.makeText(mContext, R.string.profile_not_found, Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(mContext, R.string.profile_not_found, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mUsersReference.addListenerForSingleValueEvent(userValueEventListener);
    }

    private void handleViewsVisibility() {
        if (mUser.getUserId().equals(mCurrentUser.getUserId())) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            if (mMenu != null)
                mMenu.getItem(0).setVisible(true);
        } else {
            mMenu.getItem(0).setVisible(false);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void setCurrentFragment(Fragment newFragment) {
        if (getActivity() == null) return;
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.profile_frameLayout, newFragment, null);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public void setUserInfo(final User profileUser) {
        try {
            setCurrentFragment(ProfilePostsFragment.newInstance(profileUser.getUserId()));
            // set  user name on title
            if (collapsingToolbar != null) {
                collapsingToolbar.setTitle(profileUser.getFullName());
                // make title disappears when expanded
            }
            userNameTxtView.setText(String.format("@%s", profileUser.getUsername()));
            if (!profileUser.getProfilePicture().isEmpty()) {
                Glide.with(mContext).load(profileUser.getProfilePicture()).into(profileImageView);
            } else {
                Glide.with(mContext).load(R.drawable.default_profile_picture).into(profileImageView);
            }
            postsCount.setText(String.valueOf(profileUser.getPostsCount()));
            votesCountTxtView.setText(String.valueOf(profileUser.getVoteCount()));
            answersCountTxtView.setText(String.valueOf(profileUser.getAnswersCount()));
        } catch (NullPointerException e) {
            Log.e("Error", e.getMessage());
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        // Remove post value event listener
        if (userValueEventListener != null) {
            mUsersReference.removeEventListener(userValueEventListener);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.current_user_profile_menu, menu);
        mMenu = menu;
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                break;
            case R.id.profile_logout:
                UserController.logout(getActivity());
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}

