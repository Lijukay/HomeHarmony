package com.lijukay.famecrew.activity;

import android.app.Application;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.utilities.DynamicColor;
import com.lijukay.famecrew.UncaughtExceptionHandler;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
