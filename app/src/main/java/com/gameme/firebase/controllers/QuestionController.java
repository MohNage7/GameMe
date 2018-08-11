package com.gameme.firebase.controllers;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.gameme.R;
import com.gameme.firebase.FirebaseConstants;
import com.gameme.firebase.FirebaseUtils;
import com.gameme.login.model.User;
import com.gameme.posts.model.Comment;
import com.gameme.posts.model.Post;
import com.gameme.utils.SharedPreferencesManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import static com.gameme.firebase.controllers.UserController.setPostsCount;
import static com.gameme.firebase.controllers.UserController.setVoteCount;


public class QuestionController {
    private String userId;
    private ValueEventListener questionValueEventListener;
    private Context mContext;
    private User currentUser;
    private IVoteFinishListener mIVoteFinishListener;
    private DatabaseReference mQuestionReference, mQuestionVotesReference;

    public QuestionController(Activity context, IVoteFinishListener IVoteFinishListener) {
        mContext = context;
        currentUser = SharedPreferencesManager.getLoggedUserObject();
        mIVoteFinishListener = IVoteFinishListener;
        mQuestionReference = FirebaseUtils.getReference(FirebaseConstants.Childs.POSTS);
    }

    public QuestionController(Activity context) {
        mContext = context;
        currentUser = SharedPreferencesManager.getLoggedUserObject();
        mQuestionReference = FirebaseUtils.getReference(FirebaseConstants.Childs.POSTS);
        mQuestionVotesReference = FirebaseUtils.getReference(FirebaseConstants.Childs.QUESTIONS_VOTES);
    }

    public static void setAnswersCount(final DatabaseReference mQuestionDataBaseRef, final long count) {
        mQuestionDataBaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);
                if (post != null) {
                    long postsCount = post.getAnswersCount();
                    mQuestionDataBaseRef.child(FirebaseConstants.Childs.ANSWERS_COUNT).setValue(postsCount + count);
                    mQuestionDataBaseRef.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void vote(final DatabaseReference questionVotes, final DatabaseReference singleQuestion, final boolean newVoteType, String userId) {
        this.userId = userId;
        questionValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final DatabaseReference counterDatabaseReference = singleQuestion.child(FirebaseConstants.Childs.VOTES_COUNT);

                if (!dataSnapshot.hasChild(currentUser.getUserId())) {
                    firstTimeVoting(counterDatabaseReference, questionVotes, newVoteType);

                } else {
                    ValueEventListener voteTypeValueEventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // get old vote value (up/down)
                            String oldVoteType = dataSnapshot.getValue(String.class);
                            questionVotes.child(currentUser.getUserId()).removeEventListener(this);
                            // call reVote method
                            reVote(counterDatabaseReference, questionVotes, oldVoteType, newVoteType);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    };

                    questionVotes.child(currentUser.getUserId()).addListenerForSingleValueEvent(voteTypeValueEventListener);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        questionVotes.addValueEventListener(questionValueEventListener);
    }

    private void firstTimeVoting(final DatabaseReference counterDatabaseReference, final DatabaseReference questionVotes, final boolean voteType) {
        counterDatabaseReference.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
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
                    questionVotes.removeEventListener(questionValueEventListener);
                    if (voteType) {
                        questionVotes.child(currentUser.getUserId()).setValue(FirebaseConstants.Childs.UP);
                        setVoteCount(userId, 1);
                    } else {
                        questionVotes.child(currentUser.getUserId()).setValue(FirebaseConstants.Childs.DOWN);
                        setVoteCount(userId, -1);
                    }
                    mIVoteFinishListener.onVoteFinish(dataSnapshot, voteType, true);


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
    private void reVote(DatabaseReference counterDatabaseReference, final DatabaseReference questionVotes, final String oldVoteType, final boolean upButton) {
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
                        questionVotes.removeEventListener(questionValueEventListener);
                        questionVotes.child(currentUser.getUserId()).setValue(null);
                        mIVoteFinishListener.onRevoteFinish(dataSnapshot, upButton, true, true);
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
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
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
                        questionVotes.removeEventListener(questionValueEventListener);
                        if (upButton) {
                            questionVotes.child(currentUser.getUserId()).setValue(FirebaseConstants.Childs.UP);
                            setVoteCount(userId, 2);
                        } else {
                            questionVotes.child(currentUser.getUserId()).setValue(FirebaseConstants.Childs.DOWN);
                            setVoteCount(userId, -2);
                        }
                        mIVoteFinishListener.onRevoteFinish(dataSnapshot, upButton, false, true);
                    } else {
                        mIVoteFinishListener.onVoteFailed(databaseError);
                        Log.e("Error", databaseError.getMessage());
                    }
                }
            });


        }


    }

    public void deleteQuestion(final DatabaseReference postVotesRef, final Post post) {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE);
        sweetAlertDialog
                .setTitleText(mContext.getString(R.string.are_u_sure))
                .setContentText(mContext.getString(R.string.delete_post))
                .setCancelText(mContext.getString(R.string.cancel))
                .setConfirmText(mContext.getString(R.string.delete))
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        mQuestionReference = null;
                        mQuestionVotesReference = null;
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {

                    @Override
                    public void onClick(final SweetAlertDialog sweetAlertDialog) {
                        mQuestionReference.child(post.getPostId()).
                                runTransaction(new Transaction.Handler() {
                                    @NonNull
                                    @Override
                                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                        currentData.setValue(null);
                                        postVotesRef.setValue(null);
                                        mQuestionVotesReference.child(post.getPostId()).setValue(null);
                                        FirebaseStorage.getInstance().getReference().child("posts_images/" + post.getImageName()).delete();
                                        return Transaction.success(currentData);
                                    }

                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, boolean isCommited,
                                                           @Nullable DataSnapshot dataSnapshot) {
                                        if (isCommited) {
                                            // decrement questions count
                                            setPostsCount(currentUser.getUserId(), -1);
                                            // update loading dialog with success message
                                            updateDeleteDialog(sweetAlertDialog, true);
                                        } else {
                                            // update loading dialog with failure message
                                            updateDeleteDialog(sweetAlertDialog, false);
                                        }
                                    }
                                });

                    }
                }).
                show();
    }


    private void updateDeleteDialog(SweetAlertDialog sweetAlertDialog, boolean isDeleted) {
        if (isDeleted) {
            sweetAlertDialog.setTitleText("")
                    .setContentText(mContext.getString(R.string.delete_success))
                    .setConfirmText(mContext.getString(R.string.ok))
                    .showCancelButton(false)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();

                        }
                    })
                    .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
        } else {
            sweetAlertDialog.setTitleText("")
                    .setContentText(mContext.getString(R.string.delete_failed))
                    .setConfirmText(mContext.getString(R.string.ok))
                    .showCancelButton(false)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    })
                    .changeAlertType(SweetAlertDialog.ERROR_TYPE);
        }
    }


    public void deleteComment(final DatabaseReference answerLikesRef, final Comment comment,
                              final String questionId) {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE);
        sweetAlertDialog
                .setTitleText(mContext.getString(R.string.are_u_sure))
                .setContentText(mContext.getString(R.string.delete_answer))
                .setCancelText(mContext.getString(R.string.cancel))
                .setConfirmText(mContext.getString(R.string.delete))
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        mQuestionReference = null;
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {

                    @Override
                    public void onClick(final SweetAlertDialog sweetAlertDialog) {
                        mQuestionReference.child(questionId).child(FirebaseConstants.Childs.COMMENTS).child(comment.getId()).
                                runTransaction(new Transaction.Handler() {
                                    @NonNull
                                    @Override
                                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                        currentData.setValue(null);
                                        QuestionController.setAnswersCount(mQuestionReference.child(questionId), -1);
                                        answerLikesRef.setValue(null);
                                        return Transaction.success(currentData);
                                    }

                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, boolean isCommited,
                                                           @Nullable DataSnapshot dataSnapshot) {
                                        if (isCommited) {
                                            // decrement answers count
                                            UserController.setAnswersCount(comment.getUserId(), -1);
                                            // update loading dialog with success message
                                            updateDeleteDialog(sweetAlertDialog, true);
                                        } else {
                                            // update loading dialog with failure message
                                            updateDeleteDialog(sweetAlertDialog, false);
                                        }
                                    }
                                });

                    }
                }).
                show();
    }


}
