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
import androidx.core.content.FileProvider;

import com.lijukay.famecrew.BuildConfig;
import com.lijukay.famecrew.R;

import java.io.File;
import java.util.ArrayList;

public class LogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        Intent intent = getIntent();

        String logText = intent.getStringExtra("logs");

        if (logText != null) {
            TextView tv = findViewById(R.id.errorText);
            tv.setText(getString(R.string.errorMessage, logText));
            findViewById(R.id.copyButton).setOnClickListener(v -> copyText(logText));
            findViewById(R.id.sendButton).setOnClickListener(v -> sendFile());
        }
    }

    private void copyText(String logs) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("ErrorLogs", logs);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show();
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
            startActivity(Intent.createChooser(share, getString(R.string.share_files_dialog_title)));
        }
    }

}