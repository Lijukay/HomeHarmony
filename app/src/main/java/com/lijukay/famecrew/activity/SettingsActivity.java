package com.lijukay.famecrew.activity;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.FileProvider;
import androidx.core.view.WindowCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.elevation.SurfaceColors;
import com.lijukay.famecrew.BuildConfig;
import com.lijukay.famecrew.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import rikka.material.preference.MaterialSwitchPreference;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        AppBarLayout appBarLayout = findViewById(R.id.top_app_bar);
        int color = SurfaceColors.SURFACE_2.getColor(this);
        MaterialToolbar materialToolbar = findViewById(R.id.titleBar);

        materialToolbar.setNavigationIcon(AppCompatResources.getDrawable(this, R.drawable.back_icon));

        setSupportActionBar(materialToolbar);

        materialToolbar.setNavigationOnClickListener(v -> onBackPressed());

        appBarLayout.setBackgroundColor(color);
    }


    public static class SettingsFragment extends PreferenceFragmentCompat {

        private RequestQueue mRequestQueue;
        private SharedPreferences settingsPreferences;
        private Preference updater;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            settingsPreferences = requireContext().getSharedPreferences("Settings", 0);

            boolean connected = isInternetConnected(requireContext());

            mRequestQueue = Volley.newRequestQueue(requireContext());

            if (mRequestQueue.getCache() != null) {
                Cache cache = mRequestQueue.getCache();
                cache.clear();
            }

            updater = findPreference("updater");
            MaterialSwitchPreference betaSwitch = findPreference("beta");

            if (connected) {
                parseJSONVersion();
            } else {
                updater.setSummary("You are not connected to the Internet."); // TODO: 02.07.2023 Add to Strings.xml
            }

            updater.setOnPreferenceClickListener(preference -> {
                showUpdateDialog();
                return true;
            });

            assert betaSwitch != null;
            betaSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                betaSwitch.setChecked(betaSwitch.isChecked());
                settingsPreferences.edit().putBoolean("wantsBeta", !betaSwitch.isChecked()).apply();
                parseJSONVersion();
                return true;
            });
        }

        public boolean isInternetConnected(Context context) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                    // Determine the connection type
                    int connectionType = activeNetwork.getType();
                    switch (connectionType) {
                        case ConnectivityManager.TYPE_WIFI -> Log.d("Connection", "Using WiFi");
                        case ConnectivityManager.TYPE_MOBILE -> Log.d("Connection", "Using Mobile Data");
                        default -> Log.d("Connection", "Using Other Connection");
                    }

                    return true;
                }
            }

            // No internet connection
            Log.d("Connection", "Disconnected");
            return false;
        }

        public static void InstallUpdate(Context context, String url, String versionName) {

            //------Set the destination as a string------//
            String destination = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + context.getString(R.string.app_name) + "." + versionName + ".apk";
            //------Set the file uri------//
            Uri fileUri = Uri.parse("file://" + destination);

            File file = new File(destination);

            if (file.exists()) //noinspection ResultOfMethodCallIgnored
                file.delete();

            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

            request.setMimeType("application/vnd.android.package-archive");
            request.setTitle(context.getString(R.string.app_name) + " Update"); // TODO: 02.07.2023 Add to Strings.xml
            request.setDescription(versionName);
            request.setDestinationUri(fileUri);

            BroadcastReceiver onComplete = new BroadcastReceiver() {
                public void onReceive(Context ctxt, Intent intent) {

                    Uri apkFileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", new File(destination));

                    Intent install = new Intent(Intent.ACTION_VIEW);

                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                    install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    install.setDataAndType(apkFileUri, "application/vnd.android.package-archive");

                    context.startActivity(install);
                    context.unregisterReceiver(this);
                }
            };
            context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            downloadManager.enqueue(request);
        }

        private void parseJSONVersion() {
            boolean wantsBeta = settingsPreferences.getBoolean("wantsBeta", false);

            String urlU = "https://lijukay.github.io/PrUp/prUp.json"; // TODO: 02.07.2023 Add to Strings.xml


            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, urlU, null, jsonObject -> {
                try {
                    int versionsCode = 0;
                    int versionsCodeBeta = 0;
                    int versionC = BuildConfig.VERSION_CODE;

                    JSONArray jsonArray = jsonObject.getJSONArray("Home Harmony");

                    for (int a = 0; a < jsonArray.length(); a++) {

                        JSONObject v = jsonArray.getJSONObject(a);

                        versionsCode = v.getInt("versionsCode");
                        versionsCodeBeta = v.getInt("versionsCodeBeta");
                    }

                    if (updater != null && versionsCodeBeta > versionC && wantsBeta) {
                        updater.setSummary("Update available"); // TODO: 02.07.2023 Add to Strings.xml
                        updater.setEnabled(true);
                    }else  if (updater != null && versionsCode <= versionC) {
                        updater.setSummary("No update available"); // TODO: 02.07.2023 Add to Strings.xml
                        updater.setEnabled(false);
                    }  else if(updater != null) {
                        updater.setSummary("Update available"); // TODO: 02.07.2023 Add to Strings.xml
                        updater.setEnabled(true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, Throwable::printStackTrace);

            mRequestQueue.add(jsonObjectRequest);
            if (mRequestQueue.getCache() != null) {
                mRequestQueue.getCache().clear();
            }
        }
        private void showUpdateDialog() {

            String urlU = "https://lijukay.github.io/PrUp/prUp.json";


            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, urlU, null, jsonObject -> {
                try {
                    int versionsCode = 0;
                    int versionsCodeBeta = 0;
                    int versionC = BuildConfig.VERSION_CODE;
                    String apkUrl = "";
                    String apkUrlBeta = "";
                    String changelog = "";
                    String changelogBeta = "";
                    String versionsName = "";
                    String versionsNameBeta = "";

                    JSONArray jsonArray = jsonObject.getJSONArray("Home Harmony");

                    for (int a = 0; a < jsonArray.length(); a++) {

                        JSONObject v = jsonArray.getJSONObject(a);

                        versionsCode = v.getInt("versionsCode");
                        versionsCodeBeta = v.getInt("versionsCodeBeta");
                        apkUrl = v.getString("apkUrl");
                        apkUrlBeta = v.getString("apkUrlBeta");
                        changelog = v.getString("changelog");
                        changelogBeta = v.getString("changelogBeta");
                        versionsName = v.getString("versionName");
                        versionsNameBeta = v.getString("versionsNameBeta");
                    }

                    assert updater != null;
                    String finalVersionsName1 = versionsName;
                    String finalChangelog1 = changelog;
                    int finalVersionsCode = versionsCode;
                    int finalVersionsCodeBeta = versionsCodeBeta;
                    String finalChangelogBeta1 = changelogBeta;
                    String finalVersionsNameBeta1 = versionsNameBeta;
                    String finalApkUrlBeta1 = apkUrlBeta;
                    String finalApkUrl1 = apkUrl;

                    if (finalVersionsCodeBeta > versionC && settingsPreferences.getBoolean("wantsBeta", false)) {
                        new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                                .setTitle(BuildConfig.VERSION_NAME + "->" + finalVersionsNameBeta1)
                                .setMessage(finalChangelogBeta1)
                                .setPositiveButton("Update", (dialog, which) -> InstallUpdate(requireContext(), finalApkUrlBeta1, finalVersionsNameBeta1)) // TODO: 02.07.2023 Add to Strings.xml
                                .setNeutralButton("Later", (dialog, which) -> dialog.cancel()) // TODO: 02.07.2023 Add to Strings.xml
                                .show();
                        } else if(finalVersionsCode > versionC) {
                            new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                                    .setTitle(BuildConfig.VERSION_NAME + "->" + finalVersionsName1)
                                    .setMessage(finalChangelog1)
                                    .setPositiveButton("Update", (dialog, which) -> InstallUpdate(requireContext(), finalApkUrl1, finalVersionsName1)) // TODO: 02.07.2023 Add to Strings.xml
                                    .setNeutralButton("Later", (dialog, which) -> dialog.cancel()) // TODO: 02.07.2023 Add to Strings.xml
                                    .show();
                        }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, Throwable::printStackTrace);

            mRequestQueue.add(jsonObjectRequest);
            if (mRequestQueue.getCache() != null) {
                mRequestQueue.getCache().clear();
            }

        }
    }


}