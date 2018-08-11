package com.gameme.posts.displayposts.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gameme.R;
import com.gameme.database.AppDatabase;
import com.gameme.database.AppExecutor;
import com.gameme.firebase.FirebaseConstants;
import com.gameme.firebase.FirebaseUtils;
import com.gameme.firebase.controllers.ILikeFinishListener;
import com.gameme.firebase.controllers.PostController;
import com.gameme.firebase.controllers.QuestionController;
import com.gameme.posts.displayposts.view.FullScreenImageActivity;
import com.gameme.posts.model.Post;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.gameme.firebase.FirebaseConstants.Childs.LIKES_COUNT;
import static com.gameme.firebase.FirebaseConstants.Childs.POSTS_LIKES_REF;
import static com.gameme.posts.displayposts.view.FullScreenImageActivity.FULL_IMAGE;
import static com.gameme.widget.PostsService.startActionPosts;


public class PostsViewHolder extends RecyclerView.ViewHolder {
    public View mView;
    @BindView(R.id.likeButton)
    public ImageButton mLikeButton;
    @BindView(R.id.question_image)
    public ImageView questionImage;
    @BindView(R.id.postHeaderLayout)
    public RelativeLayout postHeader;
    @BindView(R.id.postDescriptionTxtView)
    public TextView postDescriptionTxtView;
    @BindView(R.id.moreOption)
    public ImageView ownerOptionsButton;
    @BindView(R.id.like_layout)
    public LinearLayout likeLayout;
    @BindView(R.id.vote_layout)
    public LinearLayout voteLayout;
    @BindView(R.id.up_vote_button)
    public ImageButton upVoteButton;
    @BindView(R.id.down_vote_button)
    public ImageButton downVoteButton;
    @BindView(R.id.userPicture)
    public CircleImageView userImageView;
    @BindView(R.id.username_Tv)
    public TextView userNameView;
    @BindView(R.id.question_time)
    TextView createdTimeView;
    @BindView(R.id.react_count_tv)
    TextView likesCounterTxtView;
    @BindView(R.id.vote_count_tv)
    TextView voteView;
    @BindView(R.id.comments_counter_tv)
    TextView commentsCountView;
    @BindView(R.id.question_progressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.questionImage_container)
    FrameLayout questionImageLayout;
    @BindView(R.id.answer_layout)
    LinearLayout commentsLayout;
    private Context mContext;

    public PostsViewHolder(View view) {
        super(view);
        // init views
        mView = view;
        mContext = mView.getContext();
        ButterKnife.bind(this, view);

    }

    public void setImageClickListener(final String image) {
        questionImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, FullScreenImageActivity.class);
                intent.putExtra(FULL_IMAGE, image);
                Pair<View, String> p1 = Pair.create((View) questionImage, mContext.getString(R.string.details_image));
                ActivityOptionsCompat imageTransitionOptions = ActivityOptionsCompat.
                        makeSceneTransitionAnimation((Activity) mContext, p1);
                mContext.startActivity(intent, imageTransitionOptions.toBundle());
            }
        });
    }

    public void moreOptionMenuVisibility(String currentUserId, String userId) {
        if (currentUserId.equals(userId)) {
            ownerOptionsButton.setVisibility(View.VISIBLE);
        } else {
            ownerOptionsButton.setVisibility(View.GONE);
        }
    }


    public void setUsername(String username) {
        userNameView.setText(username);

    }


    public void postLikesListeners(String postId, ILikeFinishListener.IPostLikeFinishListener likeFinishListener) {
        final DatabaseReference postLikesCountReference = FirebaseUtils.getReference(FirebaseConstants.Childs.POSTS).child(postId).child(LIKES_COUNT);
        final DatabaseReference mPostLikesReference = FirebaseDatabase.getInstance().getReference().child(POSTS_LIKES_REF).child(postId);
        PostController postController = new PostController(likeFinishListener);
        postController.likePost(likesCounterTxtView, mPostLikesReference, postLikesCountReference);
    }

    public void setVoteButtonColor(final DatabaseReference questionVotes, final String userId) {
        questionVotes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userId)) {
                    HashMap<String, String> yourData = (HashMap<String, String>) dataSnapshot.getValue();
                    String voteType = yourData.get(userId);
                    if (voteType.equals(FirebaseConstants.Childs.UP)) {
                        upVoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.up_vote_color));
                        downVoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.icons_gray));
                    } else {
                        downVoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.down_vote_color));
                        upVoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.icons_gray));
                    }


                } else {
                    upVoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.icons_gray));
                    downVoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.icons_gray));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setLikeButtonColor(final DatabaseReference questionVotes, final String userId) {
        questionVotes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userId)) {
                    mLikeButton.setColorFilter(ContextCompat.getColor(mContext, R.color.up_vote_color));
                } else {
                    mLikeButton.setColorFilter(ContextCompat.getColor(mContext, R.color.icons_gray));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setSmallDescription(String smallDesc) {
        postDescriptionTxtView.setText(smallDesc);
    }

    public void setCreatedTimeView(String createdTime) {
        createdTimeView.setText(createdTime);
    }

    public void setLikesCount(String likes) {
        this.likesCounterTxtView.setText(likes);
    }

    public void setVoteCount(String likes) {
        this.voteView.setText(likes);
    }

    public void setProfilePicture(String profilePicture, Context mContext) {
        if (profilePicture.isEmpty()) {
            userImageView.setImageResource(R.drawable.default_profile_picture);
        } else {
            Glide.with(mContext).load(profilePicture).into(userImageView);

        }
    }

    public void setQuestionImage(String image) {
        if (image.isEmpty()) {
            questionImageLayout.setVisibility(View.GONE);
        } else {
            questionImageLayout.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(image).into(questionImage);
            mProgressBar.setVisibility(View.GONE);
        }
    }

    public void setCommentsCount(String commentsCount) {
        commentsCountView.setText(commentsCount);
    }


    public void setActionButtonVisibility(int postType) {
        if (postType == Post.POST) {
            likeLayout.setVisibility(View.VISIBLE);
            voteLayout.setVisibility(View.GONE);
        } else {
            likeLayout.setVisibility(View.GONE);
            voteLayout.setVisibility(View.VISIBLE);
        }

    }


    public void setMenuClickListener(final DatabaseReference questionVotesRef, final Post post) {
        ownerOptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PopupMenu popupMenu = new PopupMenu(
                        mContext,
                        ownerOptionsButton);

                popupMenu.inflate(R.menu.owner_option_menu);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.delete) {
                            QuestionController questionController = new QuestionController((Activity) mContext);
                            questionController.deleteQuestion(questionVotesRef, post);
                            removePostFromLocalDb(post.getPostId());
                        }
                        return false;
                    }
                });

                popupMenu.show();
            }
        });
    }

    private void removePostFromLocalDb(final String postId) {
        final AppDatabase appDatabase = AppDatabase.getsInstance();
        AppExecutor.getsInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                int isDeleted = appDatabase.getPostsDao().removePost(postId);
                if (isDeleted == 1)
                    startActionPosts(mContext);
            }
        });

    }
}

