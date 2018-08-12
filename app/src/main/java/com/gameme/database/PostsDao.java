package com.gameme.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.gameme.posts.model.Post;

import java.util.List;

@Dao
public interface PostsDao {

    /**
     * this method  returns first five posts sorted by DESC
     *
     * @return list of posts
     */
    @Query("SELECT * FROM post ORDER BY postId DESC limit 5")
    List<Post> getPosts();

    /**
     * @param post object to be inserted
     */
    @Insert
    void insertPost(Post post);

    /**
     * this method deletes post
     *
     * @param id of the post the we want to delete it
     * @return num of deleted posts
     */
    @Query("DELETE  FROM post WHERE postId= :id")
    int removePost(String id);

    /**
     * this method deletes all posts
     */
    @Query("DELETE FROM post")
    void removeAllPosts();


}
