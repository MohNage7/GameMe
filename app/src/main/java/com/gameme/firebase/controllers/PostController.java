package com.gameme.firebase.controllers;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;

import com.gameme.firebase.FirebaseConstants;
import com.gameme.login.model.User;
import com.gameme.utils.SharedPreferencesManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;


public class PostController {
    private ILikeFinishListener.IPostLikeFinishListener iPostLikeFinishListener;
    private User currentUser;
    private ValueEventListener answersValueEventListener;

    public PostController(ILikeFinishListener.IPostLikeFinishListener iPostLikeFinishListener) {
        this.iPostLikeFinishListener = iPostLikeFinishListener;
        currentUser = SharedPreferencesManager.getLoggedUserObject();

    }

    //  implement like / unlike logic
    public void likePost(final TextView countTxtView, final DatabaseReference postLikes, final DatabaseReference counterDatabaseReference) {
        answersValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(currentUser.getUserId()))
                    reaction(countTxtView, counterDatabaseReference, postLikes, FirebaseConstants.Childs.LIKE);
                else
                    reaction(countTxtView, counterDatabaseReference, postLikes, FirebaseConstants.Childs.DISLIKE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        postLikes.addValueEventListener(answersValueEventListener);
    }


    private void reaction(final TextView countTxtView, final DatabaseReference counterDatabaseReference, final DatabaseReference answerReactions, final String reaction) {
        counterDatabaseReference.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                if (reaction.equals(FirebaseConstants.Childs.LIKE))
                    currentData.setValue((Long) currentData.getValue() + 1);
                else
                    currentData.setValue((Long) currentData.getValue() - 1);

                return Transaction.success(currentData);
            }


            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (committed) {
                    Long likesCount = (long) dataSnapshot.getValue();
                    countTxtView.setText(String.valueOf(likesCount));

                    if (reaction.equals(FirebaseConstants.Childs.LIKE)) {
                        answerReactions.removeEventListener(answersValueEventListener);
                        answerReactions.child(currentUser.getUserId()).setValue(reaction);
                        iPostLikeFinishListener.onPostLikeSuccess(likesCount, true);

                    } else {
                        answerReactions.removeEventListener(answersValueEventListener);
                        answerReactions.child(currentUser.getUserId()).setValue(null);
                        iPostLikeFinishListener.onPostLikeSuccess(likesCount, false);

                    }
                } else {
                    iPostLikeFinishListener.onPostLikeFailed(databaseError);
                    Log.e("Error", databaseError.getMessage());
                }
            }
        });
    }
}
