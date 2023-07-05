package com.lijukay.famecrew.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.core.view.WindowCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.elevation.SurfaceColors;
import com.lijukay.famecrew.BuildConfig;
import com.lijukay.famecrew.R;

import java.io.File;

public class LogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        AppBarLayout appBarLayout = findViewById(R.id.top_app_bar);
        ConstraintLayout buttonLayout = findViewById(R.id.button_layout);

        int color = SurfaceColors.SURFACE_2.getColor(this);

        appBarLayout.setBackgroundColor(color);
        buttonLayout.setBackgroundColor(color);
        getWindow().setNavigationBarColor(color);
        getWindow().setStatusBarColor(color);

        Intent intent = getIntent();

        if (getIntent() != null) {
            String logText = intent.getStringExtra("logs") == null ? "No logs" : intent.getStringExtra("logs");
            String logTextDetailed = intent.getStringExtra("logs_detailed") == null ? "No logs" : intent.getStringExtra("logs_detailed");

            TextView nonDetailedLogsTextView = findViewById(R.id.error_text_non_detailed);
            nonDetailedLogsTextView.setText(getString(R.string.app_error, logText));

            findViewById(R.id.copy_logs_button).setOnClickListener(v -> copyText(logText));
            findViewById(R.id.send_logs_button).setOnClickListener(v -> sendFile());

            TextView detailedLogsTextView = findViewById(R.id.error_text_detailed);
            detailedLogsTextView.setText(logTextDetailed);
        }
    }

    private void copyText(String logs) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("ErrorLogs", logs);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, getString(R.string.copied_logs), Toast.LENGTH_SHORT).show();
    }

    public void sendFile() {
        Uri fileUri;

        if (getIntent().getStringExtra("filePath") != null) {

            File file = new File(getIntent().getStringExtra("filePath"));
            fileUri = FileProvider.getUriForFile(LogActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);

            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("application/octet-stream");
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            share.putExtra(Intent.EXTRA_STREAM, fileUri);

            for (ResolveInfo ri : getPackageManager().queryIntentActivities(share, PackageManager.MATCH_DEFAULT_ONLY)){
                grantUriPermission(ri.activityInfo.packageName, fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            startActivity(Intent.createChooser(share, getString(R.string.share_files)));
        }
    }
}