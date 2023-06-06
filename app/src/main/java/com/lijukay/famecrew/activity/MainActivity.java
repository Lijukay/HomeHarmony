package com.lijukay.famecrew.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.FileProvider;
import androidx.core.view.WindowCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.elevation.SurfaceColors;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lijukay.famecrew.BuildConfig;
import com.lijukay.famecrew.R;
import com.lijukay.famecrew.fragments.ExercisesFragment;
import com.lijukay.famecrew.fragments.HomeFragment;
import com.lijukay.famecrew.fragments.MemberOverview;
import com.lijukay.famecrew.fragments.RulesOverview;
import com.lijukay.famecrew.objects.Exercise;
import com.lijukay.famecrew.objects.Member;
import com.lijukay.famecrew.objects.Rule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Member> members;
    private ArrayList<Exercise> exercises;
    private ArrayList<Rule> rules;
    private SharedPreferences membersPreference, exercisesPreference, rulesPreference;
    private BottomNavigationView navigationBar;
    private MenuItem homeItem, membersItem, exercisesItem, rulesItem;
    private MaterialToolbar materialToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        membersPreference = getSharedPreferences("Members", 0);
        exercisesPreference = getSharedPreferences("Exercises", 0);
        rulesPreference = getSharedPreferences("Rules", 0);

        AppBarLayout appBarLayout = findViewById(R.id.topAppBar);
        navigationBar = findViewById(R.id.navigationView);

        int color = SurfaceColors.SURFACE_2.getColor(this);

        appBarLayout.setBackgroundColor(color);

        materialToolbar = findViewById(R.id.titleBar);

        setSupportActionBar(materialToolbar);

        Menu navigationBarMenu = navigationBar.getMenu();
        homeItem = navigationBarMenu.findItem(R.id.home);
        membersItem = navigationBarMenu.findItem(R.id.members);
        exercisesItem = navigationBarMenu.findItem(R.id.exercises);
        rulesItem = navigationBarMenu.findItem(R.id.rules);

        //Inflate fragment: Home
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new HomeFragment()).commit();

        selectedItem();


        navigationBar.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                getSupportFragmentManager().beginTransaction().setCustomAnimations(rikka.core.R.anim.fade_in, rikka.core.R.anim.fade_out).replace(R.id.fragmentContainer, new HomeFragment()).commit();
                homeItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.home_filled));
                membersItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.member_icon));
                exercisesItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.exercises_icon));
                rulesItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.rulebook_icon));
                return true;
            } else if (item.getItemId() == R.id.members) {
                getSupportFragmentManager().beginTransaction().setCustomAnimations(rikka.core.R.anim.fade_in, rikka.core.R.anim.fade_out).replace(R.id.fragmentContainer, new MemberOverview(materialToolbar)).commit();
                homeItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.home_icon));
                membersItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.members_icon_filled));
                exercisesItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.exercises_icon));
                rulesItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.rulebook_icon));
                return true;
            } else if (item.getItemId() == R.id.exercises) {
                getSupportFragmentManager().beginTransaction().setCustomAnimations(rikka.core.R.anim.fade_in, rikka.core.R.anim.fade_out).replace(R.id.fragmentContainer, new ExercisesFragment()).commit();
                homeItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.home_icon));
                membersItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.member_icon));
                exercisesItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.exercises_filled));
                rulesItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.rulebook_icon));
                return true;
            } else if (item.getItemId() == R.id.rules) {
                getSupportFragmentManager().beginTransaction().setCustomAnimations(rikka.core.R.anim.fade_in, rikka.core.R.anim.fade_out).replace(R.id.fragmentContainer, new RulesOverview(materialToolbar)).commit();
                homeItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.home_icon));
                membersItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.member_icon));
                exercisesItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.exercises_icon));
                rulesItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.rulebook_filled));
                return true;
            }
            return false;
        });

        //Import data from a file which was called outside the application
        Uri fileUri = getIntentData(getIntent());
        if (fileUri != null) {
            getContentOfImportedFile(fileUri);
        }

    }

    private void selectedItem() {
        if (navigationBar.getSelectedItemId() == R.id.home) {
            homeItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.home_filled));
            membersItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.member_icon));
            exercisesItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.exercises_icon));
            rulesItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.rulebook_icon));
        } else if (navigationBar.getSelectedItemId() == R.id.members) {
            homeItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.home_icon));
            membersItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.members_icon_filled));
            exercisesItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.exercises_icon));
            rulesItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.rulebook_icon));
        } else if (navigationBar.getSelectedItemId() == R.id.exercises) {
            homeItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.home_icon));
            membersItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.member_icon));
            exercisesItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.exercises_filled));
            rulesItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.rulebook_icon));
        } else if (navigationBar.getSelectedItemId() == R.id.rules) {
            homeItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.home_icon));
            membersItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.member_icon));
            exercisesItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.exercises_icon));
            rulesItem.setIcon(AppCompatResources.getDrawable(MainActivity.this, R.drawable.rulebook_filled));
        }
    }

    private void getContentOfImportedFile(Uri fileUri) {
        String destination;
        //Check file type; hhe = exercises, hhr = rules, hhm = members
        if (getFileExtension(String.valueOf(fileUri)).equals("hhe")) {
            destination = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + "/" + getString(R.string.app_name) + ".hhe";
            try {
                InputStream inputStream = getContentResolver().openInputStream(fileUri);
                File outputFile = new File(destination);
                if (inputStream != null) {
                    FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        fileOutputStream.write(buffer, 0, length);
                    }

                    fileOutputStream.close();

                    //Change file path saved in SP to the new file path (may be redundant, as files in app's storage are always named DocumentsHome Harmony.hhe
                    exercisesPreference.edit().putString("filePath", outputFile.getAbsolutePath()).apply();

                    StringBuilder fileContent = new StringBuilder();

                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(outputFile));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            fileContent.append(line);
                        }
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    exercises = new ArrayList<>();
                    Gson gson = new Gson();
                    String jsonString = fileContent.toString();
                    Type exercisesType = new TypeToken<ArrayList<Exercise>>(){}.getType();
                    exercises = gson.fromJson(jsonString, exercisesType);
                    addExercisesToFile(outputFile);
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (getFileExtension(String.valueOf(fileUri)).equals("hhr")) {
            destination = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + "/" + getString(R.string.app_name) + ".hhr";
            try {
                InputStream inputStream = getContentResolver().openInputStream(fileUri);
                File outputFile = new File(destination);
                if (inputStream != null) {
                    FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        fileOutputStream.write(buffer, 0, length);
                    }
                    fileOutputStream.close();
                    rulesPreference.edit().putString("filePath", outputFile.getAbsolutePath()).apply();
                    StringBuilder fileContent = new StringBuilder();

                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(outputFile));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            fileContent.append(line);
                        }
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    rules = new ArrayList<>();
                    Gson gson = new Gson();
                    String jsonString = fileContent.toString();
                    Type rulesType = new TypeToken<ArrayList<Rule>>(){}.getType();
                    rules = gson.fromJson(jsonString, rulesType);
                    addRulesToFile(outputFile);
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (getFileExtension(String.valueOf(fileUri)).equals("hhm")) {
            destination = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + "/" + getString(R.string.app_name) + ".hhm";
            try {
                InputStream inputStream = getContentResolver().openInputStream(fileUri);
                File outputFile = new File(destination);
                if (inputStream != null) {
                    FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        fileOutputStream.write(buffer, 0, length);
                    }
                    fileOutputStream.close();
                    membersPreference.edit().putString("filePath", outputFile.getAbsolutePath()).apply();
                    StringBuilder fileContent = new StringBuilder();

                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(outputFile)); //Fallback: file
                        String line;
                        while ((line = reader.readLine()) != null) {
                            fileContent.append(line);
                        }
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    members = new ArrayList<>();
                    Gson gson = new Gson();
                    String jsonString = fileContent.toString();
                    Type membersType = new TypeToken<ArrayList<Member>>(){}.getType();
                    members = gson.fromJson(jsonString, membersType);
                    addMembersToFile(outputFile);
                }
                if (inputStream != null) {
                    inputStream.close();
                }

                startActivity(new Intent(this, MainActivity.class));

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Uri getIntentData(Intent intent) {
        if (intent.getData() != null) {
            return intent.getData();
        } else {
            return null;
        }
    }

    private void addExercisesToFile(File file) {
        Gson gson = new Gson();
        if (exercises.size() != 0) {
            String jsonString = gson.toJson(exercises);
            try {
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(jsonString.getBytes());
                outputStream.close();

                exercisesPreference.edit().putString("filePath", file.getAbsolutePath()).apply();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void addMembersToFile(File file) {
        Gson gson = new Gson();
        if (members.size() != 0) {
            String jsonString = gson.toJson(members);
            try {
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(jsonString.getBytes());
                outputStream.close();

                membersPreference.edit().putString("filePath", file.getAbsolutePath()).apply();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private void addRulesToFile(File file) {
        Gson gson = new Gson();
        if (rules.size() != 0) {
            String jsonString = gson.toJson(rules);
            try {
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(jsonString.getBytes());
                outputStream.close();

                rulesPreference.edit().putString("filePath", file.getAbsolutePath()).apply();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_wshare, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.share) {

            String[] strings = new String[]{
                    "Exercises",
                    "Members",
                    "Rules"
            };
            boolean[] selected = new boolean[] {
                    false,
                    false,
                    false
            };

            new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                    .setTitle(getString(R.string.share_files_dialog_title))
                    .setMultiChoiceItems(strings, selected, (dialog, which, isChecked) -> selected[which] = isChecked)
                    .setPositiveButton(getString(R.string.share_files_dialog_positive_button), (dialog, which) -> sendFiles(strings, selected))
                    .show();
            return true;
        } else if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(MainActivity.this, Settings.class));
            return true;
        }
        return false;
    }

    public void sendFiles(String[] fileType, boolean[] selected) {
        //M: MEMBERS, E: EXERCISES, R: RULES

        ArrayList<Uri> files = new ArrayList<>();

        for (int i = 0; i < fileType.length; i++) {
            if(selected[i]){
                if (getSharedPreferences(fileType[i], 0).getString("filePath", null)!= null) {
                    File file = new File(getSharedPreferences(fileType[i], 0).getString("filePath", null));
                    Uri fileUri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);
                    files.add(fileUri);
                }
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
            startActivity(Intent.createChooser(share, getString(R.string.share_files_dialog_title)));
        }
    }

    private String getFileExtension(String filePath) {
        if (filePath != null && !filePath.isEmpty()) {
            int dotIndex = filePath.lastIndexOf('.');
            if (dotIndex != -1 && dotIndex < filePath.length() - 1) {
                return filePath.substring(dotIndex + 1).toLowerCase();
            }
        }
        return "";
    }
}