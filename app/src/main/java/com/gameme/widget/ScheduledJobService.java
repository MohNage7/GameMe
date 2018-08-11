package com.gameme.widget;

import android.support.annotation.NonNull;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.gameme.database.AppDatabase;
import com.gameme.database.AppExecutor;
import com.gameme.firebase.FirebaseConstants;
import com.gameme.firebase.FirebaseUtils;
import com.gameme.login.model.User;
import com.gameme.posts.model.Post;
import com.gameme.utils.SharedPreferencesManager;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import static com.gameme.profile.ProfilePostsFragment.USER_ID;
import static com.gameme.widget.PostsService.startActionPosts;

public class ScheduledJobService extends JobService {

    private ChildEventListener mChildEventListener;
    private Query mPostQuery;
    private User user;
    private AppDatabase appDatabase;

    @Override
    public boolean onStartJob(final JobParameters job) {
        user = SharedPreferencesManager.getLoggedUserObject();
        appDatabase = AppDatabase.getsInstance();
        mPostQuery = FirebaseUtils.getReference(FirebaseConstants.Childs.POSTS).limitToFirst(5);

        //Offloading work to a new thread.
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWidgetData(job);
            }
        }).start();
        return true;
    }

    public void updateWidgetData(final JobParameters parameters) {
        try {
            deleteAllPosts();
            addNewsPosts();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //Tell the framework that the job has completed and doesnot needs to be reschedule
            jobFinished(parameters, true);
        }
    }

    private void deleteAllPosts() {
        AppExecutor.getsInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                appDatabase.getPostsDao().removeAllPosts();
            }
        });
    }


    private void addNewsPosts() {
        mPostQuery = FirebaseUtils.getReference(FirebaseConstants.Childs.POSTS).orderByChild(USER_ID).equalTo(user.getUserId()).limitToLast(5);
        ChildEventListener childEventListener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Post post = dataSnapshot.getValue(Post.class);
                // insert post into db
                insertIntoDb(post);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }

        };
        mPostQuery.addChildEventListener(childEventListener);
        mChildEventListener = childEventListener;
        updateCallback();
    }

    private void insertIntoDb(final Post post) {
        AppExecutor.getsInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                appDatabase.getPostsDao().insertPost(post);
            }
        });
    }

    private void updateCallback() {
        mPostQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // update widget
                startActionPosts(getApplicationContext());
                mPostQuery.removeEventListener(this);
                if (mChildEventListener != null)
                    mPostQuery.removeEventListener(mChildEventListener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onStopJob(com.firebase.jobdispatcher.JobParameters job) {
        if (mPostQuery != null && mChildEventListener != null)
            mPostQuery.removeEventListener(mChildEventListener);
        return false;
    }
}
