package com.example.xyzreader.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created by arbalan on 2/18/17.
 */

public class ArticleUtility {

    public static int dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics) + 0.5f);
    }

    public static String getCacheKey(String url) {
        //ImageLoader Volley stores it in this format.
        if (url == null || url.length() == 0) {
            return "";
        }
        return (new StringBuilder(url.length() + 12)).append("#W").append(0).append("#H").append(0).append(url).toString();
    }

    public static String getDescriptionTagCardKeyFromUrl(String url) {
        return "Desc#" + getCacheKey(url);
    }

    public static String getDescriptionTagCardKey(String key) {
        return "Desc#" + key;
    }

    public static String getTitleTagKeyFromUrl(String url) {
        return "Title#" + getCacheKey(url);
    }

    public static String getSubTitleTagKeyFromUrl(String url) {
        return "SubTitle#" + getCacheKey(url);
    }

//    public static String getTitleTagKey(String key){
//        return "Title#" + key;
//    }
//
//    public static String getSubTitleTagKey(String key){
//        return "SubTitle#" + key;
//    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }
        //I added this to try to fix half hidden row
        totalHeight++;

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}
