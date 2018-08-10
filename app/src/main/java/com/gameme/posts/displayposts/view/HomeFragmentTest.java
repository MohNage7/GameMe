package com.gameme.posts.displayposts.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.gameme.R;
import com.gameme.base.BaseFragment;
import com.gameme.base.OnMainFragmentInteractionListener;
import com.gameme.firebase.FirebaseConstants;
import com.gameme.firebase.FirebaseUtils;
import com.gameme.firebase.controllers.ILikeFinishListener;
import com.gameme.firebase.controllers.IVoteFinishListener;
import com.gameme.firebase.controllers.QuestionController;
import com.gameme.login.model.User;
import com.gameme.posts.displayposts.viewholder.PostsViewHolder;
import com.gameme.posts.model.Post;
import com.gameme.profile.ProfileFragment;
import com.gameme.utils.InternetConnection;
import com.gameme.utils.SharedPreferencesManager;
import com.gameme.utils.TimeCalculator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;


public class HomeFragmentTest extends BaseFragment implements IVoteFinishListener, ILikeFinishListener.IPostLikeFinishListener {

    public static final String LIST_STATE = "state";
    User currentUser;
    long voteCounter;
    @BindView(R.id.loading_layout)
    FrameLayout loadingLayout;
    @BindView(R.id.progress_layout)
    LinearLayout progressLayout;
    @BindView(R.id.noViews_Layout)
    LinearLayout noPostsView;
    @BindView(R.id.noViewsImage)
    ImageView noViewsImage;
    @BindView(R.id.noViewsText)
    TextView noViewsText;
    @BindView(R.id.home_recycle)
    RecyclerView mRecyclerView;
    Activity mContext;
    private Query mPostQuery;
    private DatabaseReference mQuestionVotesReference, mPostsReference, mPostLikesReference;
    private boolean hasClicked;
    private OnMainFragmentInteractionListener onMainFragmentInteractionListener;
    private PostsViewHolder mPostsViewHolder;
    private Parcelable mListState;
    private LinearLayoutManager linearLayoutManager;

    public HomeFragmentTest() {
    }

    public static HomeFragmentTest newInstance() {
        return new HomeFragmentTest();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        currentUser = SharedPreferencesManager.getLoggedUserObject();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts, container, false);
        ButterKnife.bind(this, view);
        setupFirebase();
        return view;
    }

    private void setupFirebase() {
        mPostQuery = FirebaseUtils.getReference(FirebaseConstants.Childs.POSTS);
        mPostsReference = FirebaseUtils.getReference(FirebaseConstants.Childs.POSTS);
        mQuestionVotesReference = FirebaseUtils.getReference(FirebaseConstants.Childs.QUESTIONS_VOTES);
        mPostLikesReference = FirebaseUtils.getReference(FirebaseConstants.Childs.POSTS_LIKES_REF);
    }


    void fetchPostsFromServer() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        final QuestionController questionController = new QuestionController(mContext, this);
        FirebaseRecyclerAdapter<Post, PostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Post, PostsViewHolder>
                (Post.class, R.layout.item_post, PostsViewHolder.class, mPostQuery) {

            @Override
            protected void populateViewHolder(final PostsViewHolder viewHolder, final Post post, int position) {
                mPostsViewHolder = viewHolder;
                final DatabaseReference questionVotesRef = mQuestionVotesReference.child(post.getPostId());
                final DatabaseReference postLikesRef = mPostLikesReference.child(post.getPostId());
                final DatabaseReference singlePostReference = mPostsReference.child(post.getPostId());
                viewHolder.setActionButtonVisibility(post.getPostType());
                viewHolder.setUsername(post.getFullName());
                viewHolder.setCommentsCount(String.valueOf(post.getAnswersCount()));
                if (post.getPostType() == Post.POST) {
                    viewHolder.setLikesCount(String.valueOf(post.getLikesCount()));
                    // set like button color
                    viewHolder.setLikeButtonColor(postLikesRef, currentUser.getUserId());
                    // set like listener
                    viewHolder.mLikeButton.setOnClickListener(new OneShotClickListener() {
                        @Override
                        public void onClicked(View v) {
                            viewHolder.postLikesListeners(post.getPostId(), HomeFragmentTest.this);
                        }
                    });
                } else {
                    // set vote count
                    viewHolder.setVoteCount(String.valueOf(post.getVotesCount()));
                    // set up vote button color
                    viewHolder.setVoteButtonColor(questionVotesRef, currentUser.getUserId());
                    // set vote listeners
                    viewHolder.upVoteButton.setOnClickListener(new OneShotClickListener() {
                        @Override
                        public void onClicked(View v) {
                            questionController.vote(questionVotesRef, singlePostReference, true, post.getUserId());
                        }
                    });

                    viewHolder.downVoteButton.setOnClickListener(new OneShotClickListener() {
                        @Override
                        public void onClicked(View v) {
                            questionController.vote(questionVotesRef, singlePostReference, false, post.getUserId());
                        }
                    });
                }

                viewHolder.setCreatedTimeView(TimeCalculator.getTimeAgo(post.getTime(), mContext));
                viewHolder.setSmallDescription(post.getContent());
                viewHolder.setProfilePicture(post.getUserPicture(), mContext);
                viewHolder.setQuestionImage(post.getImage());
                viewHolder.setImageClickListener(post.getImage());
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (InternetConnection.isNetworkAvailable(mContext)) {
                            Intent intent = new Intent(mContext, PostDetailsActivity.class);
                            Pair<View, String> p1 = Pair.create((View) viewHolder.questionImage, getString(R.string.details_image));
                            Pair<View, String> p2 = Pair.create((View) viewHolder.postHeader, getString(R.string.post_header));
                            ActivityOptionsCompat imageTransitionOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(mContext, p1, p2);
                            intent.putExtra(PostDetailsActivity.POST_DETAILS, post);
                            mContext.startActivity(intent, imageTransitionOptions.toBundle());
                        } else {
                            Snackbar.make(mRecyclerView, R.string.no_internet, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });

                viewHolder.moreOptionMenuVisibility(currentUser.getUserId(), post.getUserId());
                viewHolder.setMenuClickListener(questionVotesRef, post);
                viewHolder.userImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        replaceProfileFragment(post.getUsername());
                    }
                });
                viewHolder.userNameView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        replaceProfileFragment(post.getUsername());
                    }
                });
            }

        };
        setProgressBar();
        setupRecyclerView(firebaseRecyclerAdapter);
    }

    private void setupRecyclerView
            (FirebaseRecyclerAdapter<Post, PostsViewHolder> firebaseRecyclerAdapter) {
        linearLayoutManager = new LinearLayoutManager(mContext);
        // reverse the list
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null)
            mListState = savedInstanceState.getParcelable(LIST_STATE);

        if (InternetConnection.isNetworkAvailable(mContext))
            fetchPostsFromServer();
        else
            showNoInternetConnection();
    }

    private void replaceProfileFragment(String username) {
        onMainFragmentInteractionListener.setCurrentFragment(ProfileFragment.newInstance(username), getString(R.string.title_profile));
    }

    void setProgressBar() {
        noPostsView.setVisibility(View.GONE);
        mPostQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressLayout.setVisibility(View.GONE);
                boolean hasChildren = dataSnapshot.hasChildren();
                if (!hasChildren) {
                    noPostsView.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.INVISIBLE);
                } else {
                    loadingLayout.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                }
                mPostQuery.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onVoteFinish(DataSnapshot dataSnapshot, boolean voteType, boolean b) {
        hasClicked = false;
        voteCounter = (long) dataSnapshot.getValue();
        mPostsViewHolder.setVoteCount(String.valueOf(voteCounter));
        if (voteType) {
            //    mSmallBangUpVote.bang(mPostsViewHolder.upVoteButton);
            mPostsViewHolder.upVoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.up_vote_color));
        } else {
            //  mSmallBangDownVote.bang(mPostsViewHolder.downVoteButton);
            mPostsViewHolder.downVoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.down_vote_color));
        }
    }

    @Override
    public void onRevoteFinish(DataSnapshot dataSnapshot, boolean isUpButton, boolean isRevot, boolean b) {
        hasClicked = false;
        voteCounter = (long) dataSnapshot.getValue();
        mPostsViewHolder.setVoteCount(String.valueOf(voteCounter));
        if (isRevot) {
            if (isUpButton) {
                //  mSmallBangNormal.bang(mQuestionsViewHolder.upVoteButton);
                mPostsViewHolder.upVoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.icons_gray));
            } else {
                //mSmallBangNormal.bang(mQuestionsViewHolder.downVoteButton);
                mPostsViewHolder.downVoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.icons_gray));
            }
        } else {
            if (isUpButton) {
                // mSmallBangUpVote.bang(mQuestionsViewHolder.upVoteButton);
            } else {
                // mSmallBangDownVote.bang(mQuestionsViewHolder.downVoteButton);
            }
        }
    }

    @Override
    public void onVoteFailed(DatabaseError error) {
        Toast.makeText(mContext, R.string.generic_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMainFragmentInteractionListener)
            onMainFragmentInteractionListener = (OnMainFragmentInteractionListener) context;
    }

    @Override
    public void onPostLikeSuccess(Long likesCount, boolean isLike) {
        hasClicked = false;
        int color;
        if (isLike)
            color = R.color.up_vote_color;
        else
            color = R.color.dislike_color;
        // change button color
        mPostsViewHolder.mLikeButton.setColorFilter(ContextCompat.getColor(mContext, color));
    }

    @Override
    public void onPostLikeFailed(DatabaseError error) {
        hasClicked = false;
        Toast.makeText(mContext, R.string.generic_error, Toast.LENGTH_SHORT).show();
    }

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
