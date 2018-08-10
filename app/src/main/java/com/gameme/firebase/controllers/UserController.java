package com.gameme.firebase.controllers;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.facebook.login.LoginManager;
import com.gameme.firebase.FirebaseConstants;
import com.gameme.firebase.FirebaseUtils;
import com.gameme.login.model.User;
import com.gameme.login.view.LoginActivity;
import com.gameme.utils.SharedPreferencesManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;


public class UserController {


    public static void setPostsCount(String userId, final long count) {
        final DatabaseReference userDatabaseRef = FirebaseUtils.getReference(FirebaseConstants.Childs.USERS_REF).child(userId);
        userDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    long postsCount = user.getPostsCount();
                    userDatabaseRef.child(FirebaseConstants.Childs.POSTS_COUNT).setValue(postsCount + count);
                    userDatabaseRef.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    public static void setVoteCount(String userId, final long count) {
        final DatabaseReference userDatabaseRef = FirebaseUtils.getReference(FirebaseConstants.Childs.USERS_REF).child(userId);
        userDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    long answersCount = user.getVoteCount();
                    userDatabaseRef.child(FirebaseConstants.Childs.VOTE_COUNT).setValue(answersCount + count);
                    userDatabaseRef.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    public static void setAnswersCount(String userId, final long count) {
        final DatabaseReference userDatabaseRef = FirebaseUtils.getReference(FirebaseConstants.Childs.USERS_REF).child(userId);
        userDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    long answersCount = user.getAnswersCount();
                    userDatabaseRef.child(FirebaseConstants.Childs.ANSWERS_COUNT).setValue(answersCount + count);
                    userDatabaseRef.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    public static void logout(Activity mContext) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        LoginManager.getInstance().logOut();
        SharedPreferencesManager.removeLoggedUserObject();
        mContext.finish();
        mContext.startActivity(new Intent(mContext, LoginActivity.class));
    }

}


