package com.gameme.firebase;

public interface FirebaseConstants {
    interface Childs {
        String USERS_REF = "users";
        String QUESTIONS_REF = "questions";
        String POSTS = "posts";
        String QUESTIONS_VOTES = "questionVotes";
        String ANSWER_VOTES = "answerVotes";
        String COMMENTS = "comments";
        String VOTE_COUNT = "voteCount";
        String ANSWERS_COUNT = "answersCount";
        String POSTS_COUNT = "postsCount";
        String DISLIKE = "dislike";
        String LIKE = "like";
        String POSTS_LIKES_REF = "postsLikes";
        String ANSWERS_LIKE_REF = "answerLikes";
        String LIKES_COUNT = "likesCount";
        String TIME = "time";
        String UP = "up";
        String IS_ANSWER = "anAnswer";
        String REPORTS = "reports";
        String ANNOYING = "Annoying";
        String INAPPROPRIATE = "Inappropriate";
        String FAKE = "Fake";
    }

    interface References {
        String FIREBASE_STORAGE = "gs://gamers-field-4966e.appspot.com";
    }

}
