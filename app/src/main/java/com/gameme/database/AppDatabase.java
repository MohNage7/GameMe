package com.gameme.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;

import com.gameme.GameMe;
import com.gameme.posts.model.Post;

@Database(entities = {Post.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "gammme";
    private static final Object LOCK = new Object();
    private static AppDatabase sInstance;

    /**
     * @return new or original instance from db
     */
    public static AppDatabase getsInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = Room.databaseBuilder(GameMe.get().getApplicationContext(),
                        AppDatabase.class,
                        AppDatabase.DATABASE_NAME).build();

            }
        }
        return sInstance;
    }

    public abstract PostsDao getPostsDao();

}
