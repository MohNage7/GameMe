package com.gameme.firebase.controllers;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.gameme.firebase.FirebaseConstants;
import com.gameme.login.model.User;
import com.gameme.posts.displayposts.viewholder.AnswersViewHolder;
import com.gameme.utils.SharedPreferencesManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

public class AnswerController {
    private ILikeFinishListener.IAnswersLikeFinishListener iAnswersLikeFinishListener;
    private User currentUser;
    private ValueEventListener answersValueEventListener;

    public AnswerController(Activity context, ILikeFinishListener.IAnswersLikeFinishListener iAnswersLikeFinishListener) {
        this.iAnswersLikeFinishListener = iAnswersLikeFinishListener;
        currentUser = SharedPreferencesManager.getLoggedUserObject();

    }

    //  implement like / unlike logic
    public void likeAnswer(final AnswersViewHolder viewHolder, final DatabaseReference answersLikes, final DatabaseReference counterDatabaseReference) {
        answersValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(currentUser.getUserId()))
                    reaction(viewHolder, counterDatabaseReference, answersLikes, FirebaseConstants.Childs.LIKE);
                else
                    reaction(viewHolder, counterDatabaseReference, answersLikes, FirebaseConstants.Childs.DISLIKE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        answersLikes.addValueEventListener(answersValueEventListener);
    }


    private void reaction(final AnswersViewHolder viewHolder, final DatabaseReference counterDatabaseReference, final DatabaseReference answerReactions, final String reaction) {
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
                    viewHolder.setLikesCount(String.valueOf(likesCount));

                    if (reaction.equals(FirebaseConstants.Childs.LIKE)) {
                        answerReactions.removeEventListener(answersValueEventListener);
                        answerReactions.child(currentUser.getUserId()).setValue(reaction);
                        iAnswersLikeFinishListener.onAnswerLikeSuccess(viewHolder, likesCount, true);

                    } else {
                        answerReactions.removeEventListener(answersValueEventListener);
                        answerReactions.child(currentUser.getUserId()).setValue(null);
                        iAnswersLikeFinishListener.onAnswerLikeSuccess(viewHolder, likesCount, false);

                    }
                } else {
                    iAnswersLikeFinishListener.onAnswerLikeFailed(databaseError);
                    Log.e("Error", databaseError.getMessage());
                }
            }
        });
    }


}
