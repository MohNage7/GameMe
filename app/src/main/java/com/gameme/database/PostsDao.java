package com.gameme.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.gameme.posts.model.Post;

import java.util.List;

@Dao
public interface PostsDao {

    @Query("SELECT * FROM post ORDER BY postId DESC limit 5")
    List<Post> getPosts();

//    @Query("SELECT * FROM post ORDER BY postId")
//    List<Post> getPosts();


    @Insert
    void insertPost(Post post);

    @Query("DELETE  FROM post WHERE postId= :id")
    int removePost(String id);

    @Query("DELETE FROM post")
    void removeAllPosts();

    @Insert
    void insertAllPosts(Post... posts);
}
