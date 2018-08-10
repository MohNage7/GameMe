package com.gameme.posts.displayposts.viewholder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gameme.R;
import com.gameme.posts.model.Post;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;


public class AnswersViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.markImageButton)
    public ImageButton answerMarkButton;
    @BindView(R.id.moreOption)
    public ImageButton ownerOptionsButton;
    @BindView(R.id.likeAnswerBtn)
    public ImageButton mCommentsLikeButton;
    @BindView(R.id.replayTxtView)
    public TextView replayTxtView;
    @BindView(R.id.vote_layout)
    public LinearLayout voteLayout;
    @BindView(R.id.up_vote_button)
    public ImageButton upVoteButton;
    @BindView(R.id.down_vote_button)
    public ImageButton downVoteButton;
    @BindView(R.id.like_layout)
    public LinearLayout likeLayout;
    @BindView(R.id.vote_count_tv)
    public TextView voteCountTxtView;
    @BindView(R.id.answererImg)
    CircleImageView userImage;
    @BindView(R.id.likesCount_txtView)
    TextView likesCountTxtView;
    @BindView(R.id.nameTxt)
    TextView userNameTxtView;
    @BindView(R.id.time_txtView)
    TextView answerTimeView;
    @BindView(R.id.answerTxt)
    TextView answerTxtView;
    private Context mContext;

    public AnswersViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
        // init views
        mContext = view.getContext();
    }

    public boolean optionMenuForAnswersVisibility(String loggedUserId, String answerOwnerId, String questionOwnerId) {
        if (loggedUserId.equals(answerOwnerId) || loggedUserId.equals(questionOwnerId)) {
            ownerOptionsButton.setVisibility(View.VISIBLE);
            return true;
        } else {
            ownerOptionsButton.setVisibility(View.GONE);
            return false;
        }

    }

    public void setContent(String answer) {
        Spannable spannable = new SpannableString(answer);
        // set hash tag as clickable and color
        setMention(answer, mContext, spannable);
        answerTxtView.setMovementMethod(LinkMovementMethod.getInstance());
        answerTxtView.setText(spannable);
    }

    private void setMention(String title, final Context mContext, Spannable spannable) {
        String[] words = title.split(" ");
        for (final String word : words) {
            if (word.startsWith("@")) {
                ClickableSpan clickableSpan = new ClickableSpan() {

                    @Override
                    public void onClick(View textView) {
                        // click event for hash tag

                    }

                    @Override
                    public void updateDrawState(TextPaint textPaint) {
                        textPaint.setColor(ContextCompat.getColor(mContext, R.color.up_vote_color));
                        // textPaint.setUnderlineText(true);
                    }
                };
                spannable.setSpan(clickableSpan, title.indexOf(word), title.indexOf(word) + word.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    public void setLikesCount(String count) {
        likesCountTxtView.setText(count);
    }

    public void setUserName(String title) {
        userNameTxtView.setText(title);
    }

    public void setAnswerTime(String answerTime) {
        answerTimeView.setText(answerTime);
    }

    public void setUserImage(String img) {
        if (img.isEmpty())
            userImage.setImageResource(R.drawable.default_profile_picture);
        else
            Glide.with(mContext).load(img).into(userImage);
    }

    public void setAnswerMarkButton(boolean isAnswer) {
        if (isAnswer)
            answerMarkButton.setColorFilter(ContextCompat.getColor(mContext, android.R.color.holo_green_light));
        else
            answerMarkButton.setColorFilter(ContextCompat.getColor(mContext, R.color.icons_gray));

    }

    public void setMarkVisibility(boolean isAsker, boolean isAnswer) {
        if (isAsker)
            answerMarkButton.setVisibility(View.VISIBLE);
        else {
            if (isAnswer)
                answerMarkButton.setVisibility(View.VISIBLE);
            else
                answerMarkButton.setVisibility(View.GONE);

        }
    }

    public void setVoteButtonColor(final DatabaseReference questionVotes, final String userId) {
        questionVotes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userId)) {
                    HashMap<String, String> yourData = (HashMap<String, String>) dataSnapshot.getValue();
                    String voteType = yourData.get(userId);
                    if (voteType.equals("up")) {
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

    public void setVoteCount(String voteCount) {
        this.voteCountTxtView.setText(voteCount);
    }

    public void setLikesButtonColor(final DatabaseReference likesReference, final String userId) {
        likesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userId)) {
                    mCommentsLikeButton.setColorFilter(ContextCompat.getColor(mContext, R.color.up_vote_color));
                } else {
                    mCommentsLikeButton.setColorFilter(ContextCompat.getColor(mContext, R.color.icons_gray));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
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

}
