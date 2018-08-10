package com.gameme.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.gameme.HomeActivity;
import com.gameme.R;

public class PostsWidgetProvider extends AppWidgetProvider {
    public static final String WIDGET_ID = "id";

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews rv = getPostsGridRemoteView(context);
        appWidgetManager.updateAppWidget(appWidgetId, rv);
    }

    /**
     * Updates all widget instances given the widget Ids and display information
     *
     * @param context          The calling context
     * @param appWidgetManager The widget manager
     * @param appWidgetIds     Array of widget Ids to be updated
     */
    public static void updatePostsWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    /**
     * Creates and returns the RemoteViews to be displayed in the GridView mode widget
     *
     * @param context The context
     * @return The RemoteViews for the GridView mode widget
     */
    private static RemoteViews getPostsGridRemoteView(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_grid_view);
        // Set the GridWidgetService intent to act as the adapter for the GridView
        Intent intent = new Intent(context, GridWidgetService.class);
        // set remote adapter
        views.setRemoteAdapter(R.id.widget_list_view, intent);
        // Handle empty widget
        views.setEmptyView(R.id.widget_list_view, R.id.empty_view);
        return views;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //Start the intent service update widget action, the service takes care of updating the widgets UI
        for (int appWidgetId : appWidgetIds) {
            try {
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.addCategory("android.intent.category.LAUNCHER");

                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.setComponent(new ComponentName(context.getPackageName(),
                        HomeActivity.class.getName()));
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        context, 0, intent, 0);
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_post_item);

                views.setOnClickPendingIntent(R.id.post_item_layout, pendingIntent);

                appWidgetManager.updateAppWidget(appWidgetId, views);

            } catch (ActivityNotFoundException e) {
                Toast.makeText(context.getApplicationContext(),
                        "There was a problem loading the application: ",
                        Toast.LENGTH_SHORT).show();
            }

        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        //startActionUpdateRecipeWidgets(context);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // Perform any action when one or more AppWidget instances have been deleted
    }

    @Override
    public void onEnabled(Context context) {
        // Perform any action when an AppWidget for this provider is instantiated
    }

    @Override
    public void onDisabled(Context context) {
        // Perform any action when the last AppWidget instance for this provider is deleted
    }


}
