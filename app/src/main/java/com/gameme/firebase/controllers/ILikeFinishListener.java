package com.gameme.firebase.controllers;

import com.gameme.posts.displayposts.viewholder.AnswersViewHolder;
import com.google.firebase.database.DatabaseError;


public interface ILikeFinishListener {
    interface IPostLikeFinishListener {
        void onPostLikeSuccess(Long likesCount, boolean isLike);

        void onPostLikeFailed(DatabaseError error);
    }

    interface IAnswersLikeFinishListener {
        void onAnswerLikeSuccess(AnswersViewHolder answersViewHolder, Long likesCount, boolean isLike);

        void onAnswerLikeFailed(DatabaseError error);

    }


}
