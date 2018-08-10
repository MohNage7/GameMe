package com.gameme.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.gameme.R;
import com.gameme.base.BaseFragment;
import com.gameme.firebase.FirebaseConstants;
import com.gameme.firebase.FirebaseUtils;
import com.gameme.posts.displayposts.view.PostDetailsActivity;
import com.gameme.posts.model.Post;
import com.gameme.utils.InternetConnection;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ProfilePostsFragment extends BaseFragment {

    private static final String USER_ID = "userId";

    DatabaseReference mUsersReference;
    String userId;
    @BindView(R.id.no_posts_txtView)
    TextView noPostsTxtView;
    @BindView(R.id.profilePostsRecycler)
    RecyclerView mRecyclerView;
    @BindView(R.id.loading_layout)
    LinearLayout progressLayout;
    Context mContext;
    private Query query;

    public ProfilePostsFragment() {
        // Required empty public constructor
    }


    public static ProfilePostsFragment newInstance(String userId) {
        ProfilePostsFragment fragment = new ProfilePostsFragment();
        Bundle args = new Bundle();
        args.putString(USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        if (getArguments() != null) {
            userId = getArguments().getString(USER_ID);
        }
        mUsersReference = FirebaseUtils.getReference(FirebaseConstants.Childs.POSTS);
        query = mUsersReference.orderByChild(USER_ID).equalTo(userId);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View postsView = inflater.inflate(R.layout.fragment_profile_posts, container, false);
        ButterKnife.bind(this, postsView);
        loadData();
        return postsView;
    }


    private void loadData() {
        noPostsTxtView.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        final FirebaseRecyclerAdapter<Post, ViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Post, ViewHolder>
                (Post.class, R.layout.item_profile_posts, ProfilePostsFragment.ViewHolder.class, query) {
            @Override
            protected void populateViewHolder(final ProfilePostsFragment.ViewHolder viewHolder, final Post post, int position) {
                viewHolder.setPostImage(post.getImage(), mContext);
                viewHolder.setPostContent(post.getContent());
                if (post.getPostType() == Post.POST) {
                    viewHolder.reactCountTxt.setText(String.valueOf(post.getLikesCount()));
                    viewHolder.reactImgView.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_thumb_up_white_24dp));
                } else {
                    viewHolder.reactCountTxt.setText(String.valueOf(post.getVotesCount()));
                    viewHolder.reactImgView.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_thumbs_up_down_white));
                }
                viewHolder.commentsTxtView.setText(String.valueOf(post.getAnswersCount()));
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (InternetConnection.isNetworkAvailable(mContext)) {
                            Intent intent = new Intent(mContext, PostDetailsActivity.class);
                            Pair<View, String> p1 = Pair.create((View) viewHolder.postImageImgView, getString(R.string.details_image));
                            ActivityOptionsCompat imageTransitionOptions = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, p1);
                            intent.putExtra(PostDetailsActivity.POST_DETAILS, post);
                            mContext.startActivity(intent, imageTransitionOptions.toBundle());
                        } else {
                            Snackbar.make(mRecyclerView, R.string.no_internet, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            // reverse list
            @Override
            public Post getItem(int position) {
                return super.getItem(getItemCount() - 1 - position);
            }
        };
        setProgressDialog();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(firebaseRecyclerAdapter);

    }

    void setProgressDialog() {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressLayout.setVisibility(View.GONE);
                if (!dataSnapshot.hasChildren()) {
                    if (mCurrentUser.getUserId().equals(userId)) {
                        noPostsTxtView.setVisibility(View.VISIBLE);
                        mRecyclerView.setVisibility(View.GONE);
                        noPostsTxtView.setText(R.string.we_are_excited);
                        progressLayout.setVisibility(View.GONE);
                    } else {
                        noPostsTxtView.setText(R.string.user_still_searcing);
                        mRecyclerView.setVisibility(View.GONE);
                        noPostsTxtView.setVisibility(View.VISIBLE);
                        progressLayout.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        @BindView(R.id.post_image_iv)
        ImageView postImageImgView;
        @BindView(R.id.react_iv)
        ImageView reactImgView;
        @BindView(R.id.post_item_layout)
        LinearLayout postLayout;
        @BindView(R.id.post_text_tv)
        TextView postTextTxtView;
        @BindView(R.id.react_count_tv)
        TextView reactCountTxt;
        @BindView(R.id.comments_counter_tv)
        TextView commentsTxtView;


        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            // init views
            mView = view;
        }


        void setPostImage(String image, Context mContext) {
            if (!TextUtils.isEmpty(image)) {
                postImageImgView.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(image).apply(new RequestOptions().placeholder(R.drawable.placeholder_one_image)).into(postImageImgView);
            } else
                postImageImgView.setVisibility(View.GONE);
        }


        public void setPostContent(String content) {
            if (!TextUtils.isEmpty(content)) {
                postTextTxtView.setText(content);
                postTextTxtView.setVisibility(View.VISIBLE);
            } else {
                postTextTxtView.setVisibility(View.GONE);
            }
        }
    }

}
