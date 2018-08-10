package com.gameme.posts.model;


public class Comment {
    private String id;
    private String userName;
    private String userId;
    private int type;
    private String userImage;
    private long time, likesCount, votesCount;
    private boolean anAnswer;
    private String content;


    public long getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(long likesCount) {
        this.likesCount = likesCount;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isAnAnswer() {
        return anAnswer;
    }

    public void setAnAnswer(boolean anAnswer) {
        this.anAnswer = anAnswer;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public long getVotesCount() {
        return votesCount;
    }

    public void setVoteCount(long voteCount) {
        this.votesCount = voteCount;
    }
}
