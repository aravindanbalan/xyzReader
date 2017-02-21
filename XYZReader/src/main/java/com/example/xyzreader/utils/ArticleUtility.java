package com.example.xyzreader.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Created by arbalan on 2/18/17.
 */

public class ArticleUtility {

    public static int dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics) + 0.5f);
    }
}
