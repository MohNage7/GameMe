package com.gameme.firebase.controllers;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;


public interface IVoteFinishListener {
    void onVoteFinish(DataSnapshot dataSnapshot, boolean voteType, boolean isPost);

    void onRevoteFinish(DataSnapshot dataSnapshot, boolean isUpButton, boolean isRevot, boolean isPost);

    void onVoteFailed(DatabaseError error);
}
