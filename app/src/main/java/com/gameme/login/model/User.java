package com.gameme.login.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.StringDef;

import com.google.firebase.database.IgnoreExtraProperties;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

@IgnoreExtraProperties
public class User implements Parcelable {
    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
    public static final String FACEBOOK = "Facebook";
    public static final String EMAIL = "EmailAndPassword";
    public static final String GOOGLE = "Google";
    private boolean isPrivate;
    private String userId, username, email, profileImageName, profilePicture, accountType, fullName;
    private long postsCount, voteCount, answersCount;

    public User() {
    }

    protected User(Parcel in) {
        isPrivate = in.readByte() != 0;
        userId = in.readString();
        username = in.readString();
        email = in.readString();
        profileImageName = in.readString();
        profilePicture = in.readString();
        accountType = in.readString();
        fullName = in.readString();
        postsCount = in.readLong();
        voteCount = in.readLong();
        answersCount = in.readLong();
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageName() {
        return profileImageName;
    }

    public void setProfileImageName(String profileImageName) {
        this.profileImageName = profileImageName;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public long getAnswersCount() {
        return answersCount;
    }

    public void setAnswersCount(long answersCount) {
        this.answersCount = answersCount;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(@AccountType String accountType) {
        this.accountType = accountType;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public long getPostsCount() {
        return postsCount;
    }

    public void setPostsCount(long postsCount) {
        this.postsCount = postsCount;
    }

    public long getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(long voteCount) {
        this.voteCount = voteCount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isPrivate ? 1 : 0));
        dest.writeString(userId);
        dest.writeString(username);
        dest.writeString(email);
        dest.writeString(profileImageName);
        dest.writeString(profilePicture);
        dest.writeString(accountType);
        dest.writeString(fullName);
        dest.writeLong(postsCount);

        dest.writeLong(voteCount);
        dest.writeLong(answersCount);
    }

    @Retention(SOURCE)
    @StringDef({
            FACEBOOK,
            EMAIL,
            GOOGLE
    })
    public @interface AccountType {
    }


}
