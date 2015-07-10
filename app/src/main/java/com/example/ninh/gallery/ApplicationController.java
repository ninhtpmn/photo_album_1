package com.example.ninh.gallery;

import android.app.Application;

/**
 * Created by ninh on 04/03/2015.
 */
public class ApplicationController extends Application {

    //Application wide instance variables
    //Preferable to expose them via getter/setter methods
    @Override
    public void onCreate() {
        super.onCreate();
        LayoutUltis.init(getApplicationContext());
    }
    //Appplication wide methods
}
