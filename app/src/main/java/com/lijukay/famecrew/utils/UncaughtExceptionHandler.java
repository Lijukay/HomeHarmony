package com.lijukay.famecrew.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;

import com.lijukay.famecrew.BuildConfig;
import com.lijukay.famecrew.activity.LogActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final Context context;

    public UncaughtExceptionHandler(Context context) {
        this.context = context;
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
        // Custom exception handling logic
        String logs = throwable.getMessage();
        StringWriter logWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(logWriter));

        if (logs != null) {
            try {

                FileOutputStream outputStream = context.openFileOutput("logs.txt", Context.MODE_PRIVATE);
                outputStream.write(("------Message------\n\n"+
                        throwable +
                        "\n\n------Logs------\n\n" +
                        logWriter.toString().replace(",", ",\n") +
                        "\n\n------App information------\n\nVersion Code: " +
                        BuildConfig.VERSION_CODE +
                        "\nVersion Name: " + BuildConfig.VERSION_NAME +
                        "\n\n------Device information------\n\n" +
                        "Android version: " + Build.VERSION.RELEASE +
                        "\nAndroid SDK Integer: " + Build.VERSION.SDK_INT +
                        "\nDevice Brand: " + Build.BRAND +
                        "\nDevice Model: " + Build.MODEL
                ).getBytes());
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Perform any additional actions if necessary, such as logging or reporting the exception.

        // Launch the LogActivity to display the logs
        Intent intent = new Intent(context, LogActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("filePath", context.getFilesDir() + File.separator + "logs.txt");
        intent.putExtra("logs", logs);
        intent.putExtra("logs_detailed", logWriter.toString());
        context.startActivity(intent);

        System.exit(1);
    }
}