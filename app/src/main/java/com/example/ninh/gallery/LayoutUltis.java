package com.example.ninh.gallery;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by ninh on 02/03/2015.
 */
public class LayoutUltis {

    private static Context mContext;

    /** width, height */
    private static int Width;
    private static int Height;


    public static void init(final Context pActivity) {
        mContext = pActivity;

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Width = display.getWidth();
        Height = display.getHeight();
    }

    public static int GetWidth()
    {
        return  Width;
    }

    public static int GetHeight()
    {
        return  Height;
    }

}
