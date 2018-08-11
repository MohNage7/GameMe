package com.gameme.posts.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.lang.annotation.Retention;
import java.util.HashMap;
import java.util.Map;

import static java.lang.annotation.RetentionPolicy.SOURCE;


@Entity
public class Post implements Parcelable {

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };
    public static final int QUESTION = 0;
    public static final int POST = 1;
    private String content;
    @PrimaryKey
    @NonNull
    private String postId;
    private String category;
    private String image;
    private String imageName;
    private String userId;
    private String username;
    private String fullName;
    private String userPicture;
    private long time, answersCount, votesCount, likesCount;
    private int postType;

    @Ignore
    private Post(Parcel in) {
        content = in.readString();
        postId = in.readString();
        category = in.readString();
        image = in.readString();
        imageName = in.readString();
        userId = in.readString();
        username = in.readString();
        fullName = in.readString();
        userPicture = in.readString();
        time = in.readLong();
        votesCount = in.readLong();
        answersCount = in.readLong();
        likesCount = in.readLong();
        postType = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(content);
        parcel.writeString(postId);
        parcel.writeString(category);
        parcel.writeString(image);
        parcel.writeString(imageName);
        parcel.writeString(userId);
        parcel.writeString(username);
        parcel.writeString(fullName);
        parcel.writeString(userPicture);
        parcel.writeLong(time);
        parcel.writeLong(votesCount);
        parcel.writeLong(answersCount);
        parcel.writeLong(likesCount);
        parcel.writeInt(postType);
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("content", content);
        result.put("postId", postId);
        result.put("category", category);
        result.put("image", image);
        result.put("imageName", imageName);
        result.put("userId", userId);
        result.put("username", username);
        result.put("fullName", fullName);
        result.put("userPicture", userPicture);
        result.put("votesCount", votesCount);
        result.put("answersCount", answersCount);
        result.put("likesCount", likesCount);
        result.put("time", ServerValue.TIMESTAMP);
        result.put("postType", postType);
        return result;
    }

    public Post() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getPostType() {
        return postType;
    }

    public void setPostType(@PostType int postType) {
        this.postType = postType;
    }

    public String getUserPicture() {
        return userPicture;
    }

    public void setUserPicture(String userPicture) {
        this.userPicture = userPicture;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public long getAnswersCount() {
        return answersCount;
    }

    public void setAnswersCount(long answersCount) {
        this.answersCount = answersCount;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getVotesCount() {
        return votesCount;
    }

    public void setVotesCount(long votesCount) {
        this.votesCount = votesCount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(long likesCount) {
        this.likesCount = likesCount;
    }

    @Override
    public int describeContents() {
        return 0;
    }


    @Retention(SOURCE)
    @IntDef({QUESTION, POST})
    public @interface PostType {
    }
}
