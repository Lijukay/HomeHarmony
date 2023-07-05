package com.lijukay.famecrew.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.FileProvider;
import androidx.core.view.WindowCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.elevation.SurfaceColors;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lijukay.famecrew.BuildConfig;
import com.lijukay.famecrew.R;
import com.lijukay.famecrew.fragments.ExercisesFragment;
import com.lijukay.famecrew.fragments.HomeFragment;
import com.lijukay.famecrew.fragments.MembersFragment;
import com.lijukay.famecrew.fragments.RulesFragment;
import com.lijukay.famecrew.objects.Exercise;
import com.lijukay.famecrew.objects.Member;
import com.lijukay.famecrew.objects.Rule;
import com.lijukay.famecrew.utils.UncaughtExceptionHandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Member> members;
    private ArrayList<Exercise> exercises;
    private ArrayList<Rule> rules;
    private MenuItem bottomNavigationViewHomeItem, bottomNavigationViewMembersItem, bottomNavigationViewTaskItem, bottomNavigationViewRulesItem;
    private MaterialToolbar mainActivityToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(this));

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        mainActivityToolbar = findViewById(R.id.titleBar);

        setSupportActionBar(mainActivityToolbar);

        BottomNavigationView mainActivityBottomNavigationView = findViewById(R.id.navigationView);
        AppBarLayout mainActivityAppBarLayout = findViewById(R.id.top_app_bar);
        int surfaceColor = SurfaceColors.SURFACE_2.getColor(this);
        Menu mainActivityBottomNavigationViewMenu = mainActivityBottomNavigationView.getMenu();

        mainActivityAppBarLayout.setBackgroundColor(surfaceColor);

        bottomNavigationViewHomeItem = mainActivityBottomNavigationViewMenu.findItem(R.id.homeItem);
        bottomNavigationViewMembersItem = mainActivityBottomNavigationViewMenu.findItem(R.id.memberItem);
        bottomNavigationViewTaskItem = mainActivityBottomNavigationViewMenu.findItem(R.id.taskItem);
        bottomNavigationViewRulesItem = mainActivityBottomNavigationViewMenu.findItem(R.id.rulesItem);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new HomeFragment()).commit();
        mainActivityToolbar.setTitle(getString(R.string.home));

        mainActivityBottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.homeItem) {
                getSupportFragmentManager().beginTransaction().setCustomAnimations(rikka.core.R.anim.fade_in, rikka.core.R.anim.fade_out).replace(R.id.fragmentContainer, new HomeFragment()).commit();
                mainActivityToolbar.setTitle(getString(R.string.home));

                bottomNavigationViewHomeItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.home_filled));
                bottomNavigationViewMembersItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.member_icon));
                bottomNavigationViewTaskItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.exercises_icon));
                bottomNavigationViewRulesItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.rulebook_icon));

                return true;
            } else if (item.getItemId() == R.id.memberItem) {
                getSupportFragmentManager().beginTransaction().setCustomAnimations(rikka.core.R.anim.fade_in, rikka.core.R.anim.fade_out).replace(R.id.fragmentContainer, new MembersFragment()).commit();
                mainActivityToolbar.setTitle(getString(R.string.members));

                bottomNavigationViewHomeItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.home_icon));
                bottomNavigationViewMembersItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.members_icon_filled));
                bottomNavigationViewTaskItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.exercises_icon));
                bottomNavigationViewRulesItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.rulebook_icon));

                return true;
            } else if (item.getItemId() == R.id.taskItem) {
                getSupportFragmentManager().beginTransaction().setCustomAnimations(rikka.core.R.anim.fade_in, rikka.core.R.anim.fade_out).replace(R.id.fragmentContainer, new ExercisesFragment()).commit();
                mainActivityToolbar.setTitle(getString(R.string.tasks));

                bottomNavigationViewHomeItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.home_icon));
                bottomNavigationViewMembersItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.member_icon));
                bottomNavigationViewTaskItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.exercises_filled));
                bottomNavigationViewRulesItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.rulebook_icon));

                return true;
            } else if (item.getItemId() == R.id.rulesItem) {
                getSupportFragmentManager().beginTransaction().setCustomAnimations(rikka.core.R.anim.fade_in, rikka.core.R.anim.fade_out).replace(R.id.fragmentContainer, new RulesFragment()).commit();
                mainActivityToolbar.setTitle(getString(R.string.rules));

                bottomNavigationViewHomeItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.home_icon));
                bottomNavigationViewMembersItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.member_icon));
                bottomNavigationViewTaskItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.exercises_icon));
                bottomNavigationViewRulesItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.rulebook_filled));

                return true;
            }
            return false;
        });

        Uri fileUri = getIntent().getData();
        if (fileUri != null) {
            getContentOfImportedFile(fileUri);
        }
    }

    private void getContentOfImportedFile(Uri fileUri) {
        String fileName = getFileName(fileUri);
        String fileExtension = null;
        String filePath = getFilePath(fileUri);

        if (fileName != null) {
            fileExtension = getFileExtension(fileName);
        }

        if (fileName != null && filePath != null) {
            switch (fileExtension) {
                case "hhe":
                    try {
                        StringBuilder fileContentBuilder = new StringBuilder();
                        InputStreamReader isr = new InputStreamReader(getContentResolver().openInputStream(fileUri));
                        BufferedReader br = new BufferedReader(isr);
                        String line;

                        while ((line = br.readLine()) != null) {
                            fileContentBuilder.append(line).append("\n");
                        }

                        br.close();

                        Gson gson = new Gson();
                        exercises = gson.fromJson(fileContentBuilder.toString(), new TypeToken<ArrayList<Exercise>>() {
                        }.getType());

                        saveFileContentE();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "hhr":
                    try {
                        StringBuilder fileContentBuilder = new StringBuilder();
                        InputStreamReader isr = new InputStreamReader(getContentResolver().openInputStream(fileUri));
                        BufferedReader br = new BufferedReader(isr);
                        String line;

                        while ((line = br.readLine()) != null) {
                            fileContentBuilder.append(line).append("\n");
                        }

                        br.close();

                        Gson gson = new Gson();
                        rules = gson.fromJson(fileContentBuilder.toString(), new TypeToken<ArrayList<Rule>>() {
                        }.getType());

                        saveFileContentR();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "hhm":
                    try {
                        StringBuilder fileContentBuilder = new StringBuilder();
                        InputStreamReader isr = new InputStreamReader(getContentResolver().openInputStream(fileUri));
                        BufferedReader br = new BufferedReader(isr);
                        String line;

                        while ((line = br.readLine()) != null) {
                            fileContentBuilder.append(line).append("\n");
                        }

                        br.close();

                        Gson gson = new Gson();
                        members = gson.fromJson(fileContentBuilder.toString(), new TypeToken<ArrayList<Member>>() {
                        }.getType());

                        saveFileContentM();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                default:
                    Toast.makeText(this, getString(R.string.invalid_file), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_share, menu);
        if (menu != null) {
            MenuItem addItem = menu.findItem(R.id.addItem);
            addItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.share) {

            String[] extensions = new String[]{
                    ".hhe",
                    ".hhm",
                    ".hhr"
            };
            boolean[] selected = new boolean[] {
                    false,
                    false,
                    false
            };

            new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                    .setTitle(getString(R.string.share_files))
                    .setMultiChoiceItems(new String[]{getString(R.string.tasks), getString(R.string.members), getString(R.string.rules)}, selected, (dialog, which, isChecked) -> selected[which] = isChecked)
                    .setPositiveButton(getString(R.string.share), (dialog, which) -> sendFiles(extensions, selected))
                    .show();
            return true;
        } else if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }
        return false;
    }

    public void sendFiles(String[] extensions, boolean[] selected) {
        ArrayList<Uri> files = new ArrayList<>();

        for (int i = 0; i < extensions.length; i++) {

            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + "/" + getString(R.string.app_name) + extensions[i]);

            if (selected[i] && file.exists()) {
                Uri fileUri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);
                files.add(fileUri);
            }
        }

        if (files.size() != 0) {
            Intent share = new Intent(Intent.ACTION_SEND_MULTIPLE);
            share.setType("application/octet-stream");
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
            for (int i = 0; i < files.size(); i++) {
                for (ResolveInfo ri : getPackageManager().queryIntentActivities(share, PackageManager.MATCH_DEFAULT_ONLY)){
                    grantUriPermission(ri.activityInfo.packageName, files.get(i), Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            }
            startActivity(Intent.createChooser(share, getString(R.string.share_files)));
        }
    }

    public String getFileExtension(String filePath) {
        if (filePath != null && !filePath.isEmpty()) {
            int dotIndex = filePath.lastIndexOf('.');
            if (dotIndex != -1 && dotIndex < filePath.length() - 1) {
                return filePath.substring(dotIndex + 1).toLowerCase();
            }
        }
        return "";
    }

    public void saveJsonAsFile(String jsonString, String destination) {
        try {
            File file = new File(destination);

            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(jsonString.getBytes());
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveFileContentE() throws IOException {
        String destination = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + "/" + getString(R.string.app_name) + ".hhe";

        Gson gson = new Gson();
        String jsonString = gson.toJson(exercises);

        FileOutputStream fos = new FileOutputStream(destination);
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        BufferedWriter bw = new BufferedWriter(osw);
        bw.write(jsonString);
        bw.close();

        Toast.makeText(this, getString(R.string.updated_tasks), Toast.LENGTH_SHORT).show();
    }

    private void saveFileContentR() throws IOException {
        String destination = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + "/" + getString(R.string.app_name) + ".hhr";

        Gson gson = new Gson();
        String jsonString = gson.toJson(rules);

        FileOutputStream fos = new FileOutputStream(destination);
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        BufferedWriter bw = new BufferedWriter(osw);
        bw.write(jsonString);
        bw.close();

        Toast.makeText(this, getString(R.string.updated_rules), Toast.LENGTH_SHORT).show();
    }

    private void saveFileContentM() throws IOException {
        String destination = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + "/" + getString(R.string.app_name) + ".hhm";

        Gson gson = new Gson();
        String jsonString = gson.toJson(members);

        FileOutputStream fos = new FileOutputStream(destination);
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        BufferedWriter bw = new BufferedWriter(osw);
        bw.write(jsonString);
        bw.close();

        Toast.makeText(this, getString(R.string.updated_members), Toast.LENGTH_SHORT).show();
    }

    public String getFileName(Uri fileUri) {
        String name = null;
        ContentResolver cr = getContentResolver();
        try (Cursor cursor = cr.query(fileUri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    name = cursor.getString(nameIndex);
                }
            }
        }
        return name;
    }

    private String getFilePath(Uri uri) {
        String path = null;
        String[] projection = {MediaStore.MediaColumns.DATA};
        ContentResolver contentResolver = getContentResolver();
        try (Cursor cursor = contentResolver.query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                path = cursor.getString(columnIndex);
            }
        }
        return path;
    }
}