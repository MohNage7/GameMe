package com.gameme.widget;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.gameme.R;
import com.gameme.database.AppDatabase;
import com.gameme.posts.model.Post;

import java.util.List;


public class GridWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new GridRemoteViewsFactory(this.getApplicationContext());
    }
}

class GridRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {


    private Context mContext;
    private List<Post> postList;

    GridRemoteViewsFactory(Context applicationContext) {
        mContext = applicationContext;
    }

    @Override
    public void onCreate() {

    }

    //called on start and when notifyAppWidgetViewDataChanged is called
    @Override
    public void onDataSetChanged() {
        // Get get all posts
        AppDatabase mDatabase = AppDatabase.getsInstance();
        postList = mDatabase.getPostsDao().getPosts();
    }


    @Override
    public void onDestroy() {
        postList = null;
    }

    @Override
    public int getCount() {
        if (postList == null) return 0;
        return postList.size();
    }

    /**
     * This method acts like the onBindViewHolder method in an Adapter
     *
     * @param position The current position of the item in the ListView to be displayed
     * @return The RemoteViews object to display for the provided postion
     */
    @Override
    public RemoteViews getViewAt(int position) {
        Post post = postList.get(position);

        final RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_post_item);
        // Update view
        if (!TextUtils.isEmpty(post.getContent())) {
            views.setTextViewText(R.id.post_text_tv, post.getContent());
            views.setViewVisibility(R.id.post_text_tv, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.post_text_tv, View.GONE);
        }
        if (post.getPostType() == Post.POST) {
            views.setTextViewText(R.id.react_count_tv, String.valueOf(post.getLikesCount()));
            views.setImageViewResource(R.id.react_iv, R.drawable.ic_thumb_up_white_24dp);
        } else {
            views.setTextViewText(R.id.react_count_tv, String.valueOf(post.getVotesCount()));
            views.setImageViewResource(R.id.react_iv, R.drawable.ic_thumbs_up_down_white);

        }

        views.setTextViewText(R.id.comments_counter_tv, String.valueOf(post.getAnswersCount()));
        return views;

    }


    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1; // Treat all items in the GridView the same
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
