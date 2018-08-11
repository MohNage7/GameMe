package com.gameme.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.gameme.R;

public class PostsService extends IntentService {

    /**
     * An {@link IntentService} subclass for handling asynchronous task requests in
     * a service on a separate handler thread.
     */


    public PostsService() {
        super("PlantWateringService");
    }

    /**
     * Starts this service to perform post widget update action. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionPosts(Context context) {
        Intent intent = new Intent(context, PostsService.class);
        context.startService(intent);
    }


    /**
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        startActionPosts();
    }


    private void startActionPosts() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        // get available widgets ids
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, PostsWidgetProvider.class));
        //Trigger data update to handle the GridView widgets and force a data refresh
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view);
        //Now update all widgets
        PostsWidgetProvider.updatePostsWidgets(this, appWidgetManager, appWidgetIds);
    }


}

