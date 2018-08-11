package com.gameme;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.gameme.base.OnMainFragmentInteractionListener;
import com.gameme.login.model.User;
import com.gameme.posts.addpost.AddPostActivity;
import com.gameme.posts.displayposts.view.HomeFragment;
import com.gameme.posts.model.Post;
import com.gameme.profile.ProfileFragment;
import com.gameme.utils.SharedPreferencesManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;

import static com.gameme.posts.addpost.AddPostActivity.POST_TYPE;
import static com.gameme.widget.JobShedulerUtils.scheduleJob;

public class HomeActivity extends AppCompatActivity implements OnMainFragmentInteractionListener {
    private static final int NEW_POST_RESULT = 1;
    @BindView(R.id.navigation)
    BottomNavigationView navigation;
    private boolean doubleBackToExitPressedOnce;
    private SimpleTooltip tooltip;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    setCurrentFragment(HomeFragment.newInstance(), getString(R.string.title_home));
                    return true;
                case R.id.navigation_new_post:
                    showToolTip(navigation);
                    return true;
                case R.id.navigation_profile:
                    User user = SharedPreferencesManager.getLoggedUserObject();
                    setCurrentFragment(ProfileFragment.newInstance(user.getUsername()), getString(R.string.title_profile));
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        if (savedInstanceState == null)
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        setSelectedBottomTab(R.id.navigation_home);
        // schedule job to sync posts in the widget
        scheduleJob(this);
    }

    void showToolTip(View v) {
        tooltip = new SimpleTooltip.Builder(this)
                .anchorView(v)
                .gravity(Gravity.TOP)
                .arrowColor(getResources().getColor(R.color.colorAccent))
                .transparentOverlay(true)
                .onDismissListener(new SimpleTooltip.OnDismissListener() {
                    @Override
                    public void onDismiss(SimpleTooltip tooltip) {
                        //
                        //     setSelectedBottomTab(R.id.navigation_home);
                    }
                })
                .contentView(R.layout.choose_post_layout)
                .build();
        tooltip.findViewById(R.id.questionButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v2) {
                if (tooltip.isShowing()) {
                    tooltip.dismiss();
                    Intent questionIntent = new Intent(HomeActivity.this, AddPostActivity.class);
                    questionIntent.putExtra(POST_TYPE, Post.QUESTION);
                    startActivityForResult(questionIntent, NEW_POST_RESULT);
                }
            }
        });

        tooltip.findViewById(R.id.normalPostButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v2) {
                if (tooltip.isShowing()) {
                    tooltip.dismiss();
                    Intent postIntent = new Intent(HomeActivity.this, AddPostActivity.class);
                    postIntent.putExtra(POST_TYPE, Post.POST);
                    startActivityForResult(postIntent, NEW_POST_RESULT);
                }
            }
        });

        tooltip.show();
    }


    @Override
    public void setSelectedBottomTab(int id) {
        navigation.setSelectedItemId(id);
    }

    @Override
    public void setCurrentFragment(Fragment newFragment, String tag) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.home_frame, newFragment, tag);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.home_frame);
        // handle back from home fragment
        if (fragment == fragmentManager.findFragmentByTag(getString(R.string.title_home))) {
            doubleClickToExitTheApplication();
            // handle back from profile fragment
        } else if (fragment == fragmentManager.findFragmentByTag(getString(R.string.title_profile))) {
            fragmentManager.popBackStack();
            setSelectedBottomTab(R.id.navigation_home);
        }
        // hide tooltip if it's showing
        if (tooltip != null && tooltip.isShowing())
            tooltip.dismiss();

        super.onBackPressed();
    }

    private void doubleClickToExitTheApplication() {
        // double click to exit app
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        setSelectedBottomTab(R.id.navigation_home);
        doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.click_back_again, Toast.LENGTH_LONG).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setSelectedBottomTab(R.id.navigation_home);
    }


}
