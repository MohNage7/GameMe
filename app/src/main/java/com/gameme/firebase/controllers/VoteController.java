package com.gameme.firebase.controllers;

import android.support.annotation.NonNull;
import android.util.Log;

import com.gameme.login.model.User;
import com.gameme.utils.SharedPreferencesManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import static com.gameme.firebase.controllers.UserController.setVoteCount;


public class VoteController {
    private String userId;
    private User currentUser;
    private ValueEventListener postValueEventListener;
    private IVoteFinishListener mIVoteFinishListener;

    public VoteController(IVoteFinishListener IVoteFinishListener) {
        currentUser = SharedPreferencesManager.getLoggedUserObject();
        mIVoteFinishListener = IVoteFinishListener;
    }

    public void vote(final DatabaseReference votesReference, final DatabaseReference singleReference, final boolean newVoteType, String userId, final boolean isPost) {
        this.userId = userId;
        postValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final DatabaseReference counterDatabaseReference = singleReference.child("votesCount");

                if (!dataSnapshot.hasChild(currentUser.getUserId())) {
                    firstTimeVoting(counterDatabaseReference, votesReference, newVoteType, isPost);

                } else {
                    ValueEventListener voteTypeValueEventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // get old vote value (up/down)
                            String oldVoteType = dataSnapshot.getValue(String.class);
                            votesReference.child(currentUser.getUserId()).removeEventListener(this);
                            // call reVote method
                            reVote(counterDatabaseReference, votesReference, oldVoteType, newVoteType, isPost);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    };

                    votesReference.child(currentUser.getUserId()).addListenerForSingleValueEvent(voteTypeValueEventListener);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        votesReference.addValueEventListener(postValueEventListener);
    }

    private void firstTimeVoting(final DatabaseReference counterDatabaseReference, final DatabaseReference questionVotes, final boolean voteType, final boolean isPost) {
        counterDatabaseReference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                if (currentData.getValue() == null) {
                    currentData.setValue(0);
                } else {
                    if (voteType)
                        currentData.setValue((Long) currentData.getValue() + 1);
                    else
                        currentData.setValue((Long) currentData.getValue() - 1);

                }
                return Transaction.success(currentData);
            }


            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (committed) {
                    questionVotes.removeEventListener(postValueEventListener);
                    if (voteType) {
                        questionVotes.child(currentUser.getUserId()).setValue("up");
                        setVoteCount(userId, 1);
                    } else {
                        questionVotes.child(currentUser.getUserId()).setValue("down");
                        setVoteCount(userId, -1);
                    }
                    mIVoteFinishListener.onVoteFinish(dataSnapshot, voteType, isPost);


                } else {
                    mIVoteFinishListener.onVoteFailed(databaseError);
                    Log.e("Error", databaseError.getMessage());
                }
            }
        });
    }

    /**
     * this method for revote process
     *
     * @param counterDatabaseReference reference for counter value fireBase
     * @param questionVotes            reference for question votes in fireBase
     * @param oldVoteType              represent user's old vote for the question up/down
     * @param upButton                 true for upButton / false for downDownButton
     */
    private void reVote(DatabaseReference counterDatabaseReference, final DatabaseReference questionVotes, final String oldVoteType, final boolean upButton, final boolean isPost) {
        if ((oldVoteType.equals("up") && upButton) || (oldVoteType.equals("down") && !upButton)) {
            counterDatabaseReference.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    if (currentData.getValue() == null) {
                        currentData.setValue(0);
                    } else {
                        if (upButton)
                            currentData.setValue((Long) currentData.getValue() - 1);
                        else
                            currentData.setValue((Long) currentData.getValue() + 1);

                    }
                    return Transaction.success(currentData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                    if (committed) {
                        questionVotes.removeEventListener(postValueEventListener);
                        questionVotes.child(currentUser.getUserId()).setValue(null);
                        mIVoteFinishListener.onRevoteFinish(dataSnapshot, upButton, true, isPost);
                        if (upButton)
                            setVoteCount(userId, -1);
                        else
                            setVoteCount(userId, 1);

                    } else {
                        mIVoteFinishListener.onVoteFailed(databaseError);
                        Log.e("Error", databaseError.getMessage());
                    }
                }
            });

        } else {
            counterDatabaseReference.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData currentData) {
                    if (currentData.getValue() == null) {
                        currentData.setValue(0);
                    } else {
                        if (upButton)
                            currentData.setValue((Long) currentData.getValue() + 2);
                        else
                            currentData.setValue((Long) currentData.getValue() - 2);
                    }
                    return Transaction.success(currentData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                    if (committed) {
                        questionVotes.removeEventListener(postValueEventListener);
                        if (upButton) {
                            questionVotes.child(currentUser.getUserId()).setValue("up");
                            setVoteCount(userId, 2);
                        } else {
                            questionVotes.child(currentUser.getUserId()).setValue("down");
                            setVoteCount(userId, -2);
                        }
                        mIVoteFinishListener.onRevoteFinish(dataSnapshot, upButton, false, isPost);
                    } else {
                        mIVoteFinishListener.onVoteFailed(databaseError);
                        Log.e("Error", databaseError.getMessage());
                    }
                }
            });


        }


    }
}