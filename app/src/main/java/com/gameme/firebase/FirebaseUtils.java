package com.gameme.firebase;

import android.support.annotation.StringDef;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.annotation.Retention;

import static com.gameme.firebase.FirebaseConstants.Childs.ANSWERS_LIKE_REF;
import static com.gameme.firebase.FirebaseConstants.Childs.ANSWER_VOTES;
import static com.gameme.firebase.FirebaseConstants.Childs.POSTS;
import static com.gameme.firebase.FirebaseConstants.Childs.POSTS_LIKES_REF;
import static com.gameme.firebase.FirebaseConstants.Childs.QUESTIONS_VOTES;
import static com.gameme.firebase.FirebaseConstants.Childs.REPORTS;
import static com.gameme.firebase.FirebaseConstants.Childs.USERS_REF;
import static java.lang.annotation.RetentionPolicy.SOURCE;

public class FirebaseUtils {

    public static DatabaseReference getReference(@ChildReferenceName String referenceName) {
        return FirebaseDatabase.getInstance().getReference().child(referenceName);
    }

    @Retention(SOURCE)
    @StringDef({QUESTIONS_VOTES, ANSWER_VOTES, POSTS_LIKES_REF, USERS_REF, POSTS, REPORTS, ANSWERS_LIKE_REF})
    public @interface ChildReferenceName {
    }
}
