package com.gameme.posts.displayposts.view;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.gameme.R;
import com.gameme.firebase.FirebaseConstants;
import com.gameme.firebase.FirebaseUtils;
import com.gameme.firebase.controllers.AnswerController;
import com.gameme.firebase.controllers.ILikeFinishListener;
import com.gameme.firebase.controllers.IVoteFinishListener;
import com.gameme.firebase.controllers.PostController;
import com.gameme.firebase.controllers.QuestionController;
import com.gameme.firebase.controllers.UserController;
import com.gameme.firebase.controllers.VoteController;
import com.gameme.login.model.User;
import com.gameme.posts.displayposts.viewholder.AnswersViewHolder;
import com.gameme.posts.model.Comment;
import com.gameme.posts.model.Post;
import com.gameme.utils.SharedPreferencesManager;
import com.gameme.utils.TimeCalculator;
import com.gameme.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.gameme.firebase.FirebaseConstants.Childs.ANSWERS_LIKE_REF;
import static com.gameme.firebase.FirebaseConstants.Childs.COMMENTS;
import static com.gameme.firebase.FirebaseConstants.Childs.IS_ANSWER;
import static com.gameme.firebase.FirebaseConstants.Childs.LIKES_COUNT;
import static com.gameme.firebase.FirebaseConstants.Childs.TIME;
import static com.gameme.firebase.FirebaseConstants.Childs.UP;
import static com.gameme.posts.displayposts.view.FullScreenImageActivity.FULL_IMAGE;


public class PostDetailsActivity extends AppCompatActivity implements ILikeFinishListener.IAnswersLikeFinishListener, ILikeFinishListener.IPostLikeFinishListener, IVoteFinishListener {
    public static final String POST_DETAILS = "post_details";
    public static final String LIST_STATE = "state";
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.likeButton)
    ImageButton mPostLikeButton;
    @BindView(R.id.loading_layout)
    FrameLayout mLoadingLayout;
    @BindView(R.id.progress_layout)
    LinearLayout mProgressLayout;
    @BindView(R.id.noViews_Layout)
    LinearLayout mNoAnswersView;
    @BindView(R.id.userPicture)
    CircleImageView mUserImageView;
    @BindView(R.id.question_image)
    ImageView mQuestionImage;
    @BindView(R.id.username_Tv)
    TextView mUserNameView;
    @BindView(R.id.postDescriptionTxtView)
    TextView mSmallDescriptionView;
    @BindView(R.id.question_time)
    TextView mCreatedTimeView;
    @BindView(R.id.react_count_tv)
    TextView mLikesCounterTxtView;
    @BindView(R.id.answer_editText)
    EditText mAnswerEditText;
    @BindView(R.id.noViewsImage)
    ImageView mNoViewsImage;
    @BindView(R.id.noViewsText)
    TextView mNoViewsText;
    @BindView(R.id.moreOption)
    ImageView mMoreOptionMenu;
    @BindView(R.id.questionDivider)
    View mQuestionDivider;
    @BindView(R.id.answers_recycler)
    RecyclerView mRecyclerView;
    @BindView(R.id.voteLayout)
    LinearLayout voteLayout;
    @BindView(R.id.like_layout)
    LinearLayout likeLayout;
    @BindView(R.id.up_vote_button)
    ImageButton mUpVoteButton;
    @BindView(R.id.down_vote_button)
    ImageButton mDownVoteButton;
    @BindView(R.id.voteCounter)
    TextView mVoteCounterView;
    Post mPost;
    User mCurrentUser;
    Query mCommentsQuery;
    private DatabaseReference mPostLikesReference, mCurrentPostReference, mCommentsVoteReference, mPostVotesReference, mCommentssDatabaseRef, mAnswersLikesReference;
    private boolean hasClicked;
    private ArrayList<String> mAnswersIdList;
    private String mention;
    private long mVoteCounter;
    private AnswerController answerController;
    private VoteController voteController;
    private AnswersViewHolder answerViewHolder;
    private Parcelable mListState;
    private LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        ButterKnife.bind(this);
        // get user object
        mCurrentUser = SharedPreferencesManager.getLoggedUserObject();
        // set toolbars
        setToolbar();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(POST_DETAILS))
            mPost = getIntent().getParcelableExtra(POST_DETAILS);
        else
            finish();

        setupFirebaseReferences();
        fillViews();
        questionVoteListeners();
        setProgressBar();
        loadComments();

        setLikeButtonColor(mPostLikesReference, mCurrentUser.getUserId());
        setVoteButtonColor(mPostVotesReference, mCurrentUser.getUserId());
        // init controllers
        answerController = new AnswerController(PostDetailsActivity.this, PostDetailsActivity.this);
        voteController = new VoteController(PostDetailsActivity.this);
    }

    void setToolbar() {
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }


    private void setLikeButtonColor(@NonNull DatabaseReference mPostVotesReference, @NonNull final String userId) {
        mPostVotesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userId)) {
                    mPostLikeButton.setColorFilter(ContextCompat.getColor(PostDetailsActivity.this, R.color.up_vote_color));
                } else {
                    mPostLikeButton.setColorFilter(ContextCompat.getColor(PostDetailsActivity.this, R.color.icons_gray));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setVoteButtonColor(@NonNull final DatabaseReference questionVotes, @NonNull final String userId) {
        questionVotes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userId)) {
                    HashMap<String, String> yourData = (HashMap<String, String>) dataSnapshot.getValue();
                    String voteType = yourData.get(userId);
                    if (voteType.equals(UP)) {
                        mUpVoteButton.setColorFilter(ContextCompat.getColor(PostDetailsActivity.this, R.color.up_vote_color));
                        mDownVoteButton.setColorFilter(ContextCompat.getColor(PostDetailsActivity.this, R.color.icons_gray));
                    } else {
                        mDownVoteButton.setColorFilter(ContextCompat.getColor(PostDetailsActivity.this, R.color.down_vote_color));
                        mUpVoteButton.setColorFilter(ContextCompat.getColor(PostDetailsActivity.this, R.color.icons_gray));
                    }


                } else {
                    mUpVoteButton.setColorFilter(ContextCompat.getColor(PostDetailsActivity.this, R.color.icons_gray));
                    mDownVoteButton.setColorFilter(ContextCompat.getColor(PostDetailsActivity.this, R.color.icons_gray));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @OnClick(R.id.send_comment_Btn)
    public void sendComment() {
        if (!TextUtils.isEmpty(mAnswerEditText.getText())) {
            mAnswerEditText.setError(null);
            final DatabaseReference questionReference = mCurrentPostReference.child(COMMENTS).push();
            Comment comment = new Comment();
            comment.setType(mPost.getPostType());
            comment.setAnAnswer(false);
            comment.setUserId(mCurrentUser.getUserId());
            comment.setUserName(mCurrentUser.getFullName());
            if (TextUtils.isEmpty(mention))
                comment.setContent(mAnswerEditText.getText().toString());
            else
                comment.setContent("\u200E" + mAnswerEditText.getText().toString());
            comment.setUserImage(mCurrentUser.getProfilePicture());
            comment.setLikesCount(0);
            comment.setVoteCount(0);
            comment.setId(questionReference.getKey());
            questionReference.setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        mAnswerEditText.setText("");
                        Utils.hideSoftKeyBoard(PostDetailsActivity.this, mAnswerEditText);
                        questionReference.child(TIME).setValue(ServerValue.TIMESTAMP);
                        QuestionController.setAnswersCount(mCurrentPostReference, 1);
                    } else
                        Toast.makeText(PostDetailsActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            mAnswerEditText.setError(getString(R.string.error_field_required));
        }
    }

    @OnClick(R.id.question_image)
    public void openImageInFullScreenMode() {
        Intent intent = new Intent(this, FullScreenImageActivity.class);
        intent.putExtra(FULL_IMAGE, mPost.getImage());
        ActivityOptionsCompat imageTransitionOptions = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this, mQuestionImage, getString(R.string.details_image));
        // set animation for 16+ and higher and disable at below
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            startActivity(intent, imageTransitionOptions.toBundle());
        else
            startActivity(intent);
    }

    private void setupFirebaseReferences() {
        mCurrentPostReference = FirebaseUtils.getReference(FirebaseConstants.Childs.POSTS).child(mPost.getPostId());
        mPostVotesReference = FirebaseUtils.getReference(FirebaseConstants.Childs.QUESTIONS_VOTES).child(mPost.getPostId());
        mCommentsQuery = mCurrentPostReference.child(COMMENTS);
        mCommentssDatabaseRef = mCurrentPostReference.child(COMMENTS);
        mPostLikesReference = FirebaseUtils.getReference(FirebaseConstants.Childs.POSTS_LIKES_REF).child(mPost.getPostId());
        mCommentsVoteReference = FirebaseUtils.getReference(FirebaseConstants.Childs.ANSWER_VOTES).child(mPost.getPostId());
    }

    private void loadComments() {
        mAnswersLikesReference = FirebaseDatabase.getInstance().getReference().child(ANSWERS_LIKE_REF);
        FirebaseRecyclerAdapter<Comment, AnswersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comment, AnswersViewHolder>
                (Comment.class, R.layout.answer_item, AnswersViewHolder.class, mCommentsQuery) {
            @Override
            protected void populateViewHolder(final AnswersViewHolder viewHolder, final Comment comment, int position) {
                answerViewHolder = viewHolder;
                final DatabaseReference answerLikesCountReference = mCommentssDatabaseRef.child(comment.getId()).child(LIKES_COUNT);
                final DatabaseReference answerLikesRef = mAnswersLikesReference.child(comment.getId());
                final DatabaseReference commentVoteReference = mCommentsVoteReference.child(comment.getId());
                final DatabaseReference singleCommentReference = mCommentssDatabaseRef.child(comment.getId());

                viewHolder.setContent(comment.getContent());
                viewHolder.setUserName(comment.getUserName());
                viewHolder.setUserImage(comment.getUserImage());
                viewHolder.setAnswerTime(TimeCalculator.getTimeAgo(comment.getTime(), PostDetailsActivity.this));
                viewHolder.setLikesCount(String.valueOf(comment.getLikesCount()));
                viewHolder.optionMenuForAnswersVisibility(mCurrentUser.getUserId(), comment.getUserId(), mPost.getUserId());
                viewHolder.setActionButtonVisibility(mPost.getPostType());

                if (mPost.getPostType() == Post.POST) {
                    viewHolder.setLikesCount(String.valueOf(comment.getLikesCount()));
                    // set like button color
                    viewHolder.setLikesButtonColor(answerLikesRef, mCurrentUser.getUserId());
                    // set like listener
                    viewHolder.mCommentsLikeButton.setOnClickListener(new OneShotClickListener() {
                        @Override
                        public void onClicked(View v) {
                            answerController.likeAnswer(viewHolder, answerLikesRef, answerLikesCountReference);
                        }
                    });
                } else {
                    // set as answer
                    viewHolder.setAnswerMarkButton(comment.isAnAnswer());
                    // set answer mark visibility
                    viewHolder.setMarkVisibility(isPostOwner(), comment.isAnAnswer());
                    // set vote count
                    viewHolder.setVoteCount(String.valueOf(comment.getVotesCount()));
                    // set up vote button color
                    viewHolder.setVoteButtonColor(commentVoteReference, mCurrentUser.getUserId());
                    // set vote listeners
                    viewHolder.upVoteButton.setOnClickListener(new OneShotClickListener() {
                        @Override
                        public void onClicked(View v) {
                            voteController.vote(commentVoteReference, singleCommentReference, true, mPost.getUserId(), false);
                        }
                    });

                    viewHolder.downVoteButton.setOnClickListener(new OneShotClickListener() {
                        @Override
                        public void onClicked(View v) {
                            voteController.vote(commentVoteReference, singleCommentReference, false, mPost.getUserId(), false);
                        }
                    });
                }

                viewHolder.answerMarkButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isPostOwner()) {
                            if (comment.isAnAnswer()) {
                                comment.setAnAnswer(false);
                                viewHolder.setAnswerMarkButton(false);
                                mCommentssDatabaseRef.child(comment.getId()).child(IS_ANSWER).setValue(false);
                                UserController.setAnswersCount(comment.getUserId(), -1);
                            } else {
                                setAsAnswer(viewHolder, comment);
                            }
                        }
                    }
                });

                viewHolder.replayTxtView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mention = "@" + "\u200E" +
                                comment.getUserName().trim();
                        mAnswerEditText.setText(mention);
                        Utils.showSoftKeyBoard(PostDetailsActivity.this, mAnswerEditText);

                    }
                });
                viewHolder.ownerOptionsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final PopupMenu popupMenu = new PopupMenu(
                                PostDetailsActivity.this,
                                viewHolder.ownerOptionsButton);

                        popupMenu.inflate(R.menu.owner_option_menu);

                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                if (item.getItemId() == R.id.delete) {
                                    QuestionController questionController = new QuestionController(PostDetailsActivity.this);
                                    questionController.deleteComment(answerLikesRef, comment, mPost.getPostId());
                                }
                                return false;
                            }
                        });

                        popupMenu.show();
                    }
                });


            }
        };

        linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
        // stop blinking animation when data changes
        RecyclerView.ItemAnimator animator = mRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // restore list state ( position )
                if (mListState != null) {
                    linearLayoutManager.onRestoreInstanceState(mListState);
                }

            }
        }, 100);

    }

    /**
     * first we set the comment as chosen comment and then get all other answers and pass them to setOtherAnswerAsFalse
     *
     * @param viewHolder view holder for the post to update comment icon
     * @param comment    object for the comment
     */
    private void setAsAnswer(final AnswersViewHolder viewHolder, final Comment comment) {
        mCommentssDatabaseRef.child(comment.getId()).child(IS_ANSWER).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mAnswersIdList = new ArrayList<>();
                mCommentsQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            Comment comment = userSnapshot.getValue(Comment.class);
                            mAnswersIdList.add(comment.getId());
                        }
                        // remove listening for changes
                        mAnswersIdList.remove(comment.getId());
                        mCommentsQuery.removeEventListener(this);
                        setOtherAnswerAsFalse(viewHolder, comment);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

            }
        });

    }

    /**
     * mContext method loops through answers and make all false but chosen comment
     */
    private void setOtherAnswerAsFalse(AnswersViewHolder viewHolder, final Comment comment) {
        for (String answerId : mAnswersIdList) {
            mCommentssDatabaseRef.child(answerId).child(IS_ANSWER).setValue(false);
        }
        comment.setAnAnswer(true);
        viewHolder.setAnswerMarkButton(true);
        UserController.setAnswersCount(comment.getUserId(), 1);
    }

    boolean isPostOwner() {
        return mCurrentUser.getUserId().equals(mPost.getUserId());
    }


    void fillViews() {
        mAnswerEditText.setError(null);
//        setVoteButtonColor();
        mMoreOptionMenu.setVisibility(View.GONE);

        if (mPost.getImage().isEmpty()) {
            mQuestionImage.setVisibility(View.GONE);
        } else {
            mQuestionImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(mPost.getImage()).into(mQuestionImage);

        }

        if (mPost.getUserPicture().isEmpty())
            mUserImageView.setImageResource(R.drawable.default_profile_picture);
        else
            Glide.with(this).load(mPost.getUserPicture()).into(mUserImageView);

        mSmallDescriptionView.setText(mPost.getContent());
        mUserNameView.setText(mPost.getUsername());
        mCreatedTimeView.setText(TimeCalculator.getTimeAgo(mPost.getTime(), this));

        showActionControllers();

    }

    private void showActionControllers() {
        switch (mPost.getPostType()) {
            case Post.POST:
                likeLayout.setVisibility(View.VISIBLE);
                voteLayout.setVisibility(View.GONE);
                mLikesCounterTxtView.setText(String.valueOf(mPost.getLikesCount()));
                break;
            case Post.QUESTION:
                likeLayout.setVisibility(View.GONE);
                voteLayout.setVisibility(View.VISIBLE);
                mVoteCounterView.setText(String.valueOf(mPost.getVotesCount()));
                break;
        }
    }

    @OnClick(R.id.likeButton)
    public void postLikesListeners() {
        final DatabaseReference postLikesCountReference = FirebaseUtils.getReference(FirebaseConstants.Childs.POSTS).child(mPost.getPostId()).child(LIKES_COUNT);
        PostController postController = new PostController(this);
        postController.likePost(mLikesCounterTxtView, mPostLikesReference, postLikesCountReference);

    }

    public void questionVoteListeners() {
        mUpVoteButton.setOnClickListener(new OneShotClickListener() {
            @Override
            public void onClicked(View v) {
                QuestionController questionController = new QuestionController(PostDetailsActivity.this, PostDetailsActivity.this);
                questionController.vote(mPostVotesReference, mCurrentPostReference, true, mPost.getUserId());
            }
        });
        mDownVoteButton.setOnClickListener(new OneShotClickListener() {
            @Override
            public void onClicked(View v) {
                QuestionController questionController = new QuestionController(PostDetailsActivity.this, PostDetailsActivity.this);
                questionController.vote(mPostVotesReference, mCurrentPostReference, false, mPost.getUserId());
            }
        });
    }

    void setProgressBar() {
        mNoViewsText.setText(getString(R.string.no_comments));
        mCommentsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mProgressLayout.setVisibility(View.GONE);
                boolean hasChildren = dataSnapshot.hasChildren();
                if (!hasChildren) {
                    mNoAnswersView.setVisibility(View.VISIBLE);
                    mQuestionDivider.setVisibility(View.GONE);
                    newCommentsObserver();
                } else {
                    mQuestionDivider.setVisibility(View.VISIBLE);
                    mLoadingLayout.setVisibility(View.GONE);
                }
                mCommentsQuery.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void newCommentsObserver() {
        ValueEventListener mNewCommentsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean hasChildren = dataSnapshot.hasChildren();
                if (hasChildren)
                    mNoAnswersView.setVisibility(View.GONE);
                else
                    mNoAnswersView.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mCommentsQuery.addValueEventListener(mNewCommentsEventListener);
    }


    @Override
    public void onAnswerLikeSuccess(AnswersViewHolder answersViewHolder, Long likesCount, boolean isLike) {
        hasClicked = false;
        if (isLike) {
            answersViewHolder.mCommentsLikeButton.setColorFilter(ContextCompat.getColor(this, R.color.up_vote_color));
        } else {
            answersViewHolder.mCommentsLikeButton.setColorFilter(ContextCompat.getColor(this, R.color.icons_gray));
        }
    }

    @Override
    public void onAnswerLikeFailed(DatabaseError error) {
        Log.e("Like Failed", error.getMessage());
    }

    @Override
    public void onPostLikeFailed(DatabaseError error) {
        Log.e("Vote Failed", error.getMessage());
    }

    @Override
    public void onPostLikeSuccess(Long likesCount, boolean isLike) {
        hasClicked = false;
        if (isLike) {
            mPostLikeButton.setColorFilter(ContextCompat.getColor(this, R.color.up_vote_color));
        } else {
            mPostLikeButton.setColorFilter(ContextCompat.getColor(this, R.color.icons_gray));
        }
    }


    @Override
    public void onVoteFinish(DataSnapshot dataSnapshot, boolean voteType, boolean isPost) {
        hasClicked = false;
        mVoteCounter = (long) dataSnapshot.getValue();
        if (isPost) {
            mVoteCounterView.setText(String.valueOf(mVoteCounter));
            if (voteType) {
                //   mSmallBangUpVote.bang(mUpVoteButton);
                mUpVoteButton.setColorFilter(ContextCompat.getColor(this, R.color.up_vote_color));
            } else {
                // mSmallBangDownVote.bang(mDownVoteButton);
                mDownVoteButton.setColorFilter(ContextCompat.getColor(this, R.color.down_vote_color));
            }
        } else {
            mVoteCounter = (long) dataSnapshot.getValue();
            answerViewHolder.voteCountTxtView.setText(String.valueOf(mVoteCounter));
            if (voteType) {
                //   mSmallBangUpVote.bang(mUpVoteButton);
                answerViewHolder.upVoteButton.setColorFilter(ContextCompat.getColor(this, R.color.up_vote_color));
            } else {
                // mSmallBangDownVote.bang(mDownVoteButton);
                answerViewHolder.downVoteButton.setColorFilter(ContextCompat.getColor(this, R.color.down_vote_color));
            }
        }

    }

    @Override
    public void onRevoteFinish(DataSnapshot dataSnapshot, boolean isUpButton, boolean isRevot, boolean isPost) {
        hasClicked = false;
        mVoteCounter = (long) dataSnapshot.getValue();
        if (isPost) {
            mVoteCounterView.setText(String.valueOf(mVoteCounter));
            if (isRevot) {
                if (isUpButton) {
                    //   mSmallBangNormal.bang(mUpVoteButton);
                    mUpVoteButton.setColorFilter(ContextCompat.getColor(this, R.color.icons_gray));
                } else {
                    // mSmallBangNormal.bang(mDownVoteButton);
                    mDownVoteButton.setColorFilter(ContextCompat.getColor(this, R.color.icons_gray));
                }
            }
        } else {
            answerViewHolder.voteCountTxtView.setText(String.valueOf(mVoteCounter));
            if (isRevot) {
                if (isUpButton) {
                    //   mSmallBangNormal.bang(mUpVoteButton);
                    answerViewHolder.upVoteButton.setColorFilter(ContextCompat.getColor(this, R.color.icons_gray));
                } else {
                    // mSmallBangNormal.bang(mDownVoteButton);
                    answerViewHolder.downVoteButton.setColorFilter(ContextCompat.getColor(this, R.color.icons_gray));
                }
            }
        }

    }

    @Override
    public void onVoteFailed(DatabaseError error) {
        Toast.makeText(this, R.string.vote_failed, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // save recycler state
        if (linearLayoutManager != null) {
            mListState = linearLayoutManager.onSaveInstanceState();
            outState.putParcelable(LIST_STATE, mListState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
            mListState = savedInstanceState.getParcelable(LIST_STATE);
    }

    /**
     * prevent multiple click
     */
    public abstract class OneShotClickListener implements View.OnClickListener {

        @Override
        public final void onClick(View v) {
            if (!hasClicked) {
                onClicked(v);
                hasClicked = true;
            }
        }

        public abstract void onClicked(View v);
    }
}
