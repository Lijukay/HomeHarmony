package com.lijukay.famecrew.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.view.WindowCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lijukay.famecrew.BuildConfig;
import com.lijukay.famecrew.R;
import com.lijukay.famecrew.fragments.ExercisesFragment;
import com.lijukay.famecrew.fragments.HomeFragment;
import com.lijukay.famecrew.fragments.MemberOverview;
import com.lijukay.famecrew.fragments.RulesOverview;
import com.lijukay.famecrew.objects.Member;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private ArrayList<Member> members;
    private MaterialToolbar materialToolbar;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        SharedPreferences membersPreference = getSharedPreferences("Members", 0);
        String filePath = membersPreference.getString("filePath", null);
        boolean fileShouldExist = membersPreference.getBoolean("shouldExist", false);

        materialToolbar = findViewById(R.id.titleBar);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        Menu menu = navigationView.getMenu();


        setSupportActionBar(materialToolbar);

        //Create submenu
        menu.add(0, 103, 0, "Home").setCheckable(true);
        SubMenu rulesMenu = menu.addSubMenu("Rules");
        rulesMenu.add(0, 100, 1, "Rulebook").setCheckable(true);


        //check if file in file path exists
        if (filePath != null) {
            getFileContent(new File(filePath));
            if (members != null) {
                SubMenu memberMenu = menu.addSubMenu("Members");
                for(int i = 0; i < members.size(); i++) {
                    memberMenu.add(1, i, i+2, members.get(i).getPrename() + " (" + members.get(i).getNickname() + ")").setCheckable(true);
                }
                SubMenu exercises = menu.addSubMenu("Exercises");
                exercises.add(2, 101, members.size()+2, "Exercises Overview").setCheckable(true);
                exercises.add(2, 102, members.size() + 3, "Unsorted exercises").setCheckable(true);
            } else {
                new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                        .setTitle("No members")
                        .setMessage("The file was found but it seems like it does not contain members. Try again")
                        .setPositiveButton("Okay", (dialog, which) -> dialog.cancel())
                        .show();
            }
        } else if (fileShouldExist) {
            new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                    .setTitle("No file found")
                    .setMessage("You previously created a file, but the app was not able to find this file. If this problem stays, you may need to recreate a file by deleting the app's data.")
                    .setPositiveButton("Okay", (dialog, which) -> dialog.cancel())
                    .show();
        }

        navigationView.setCheckedItem(menu.findItem(103));
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new HomeFragment(materialToolbar)).commit();

        navigationView.setNavigationItemSelectedListener(item -> {
            navigationView.setCheckedItem(item.getItemId());
            if (item.getItemId() != 100 && item.getItemId() != 101 && item.getItemId() != 102 && item.getItemId() != 103) {
                drawerLayout.close();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new MemberOverview(item.getTitle().toString(), materialToolbar)).commit();
            } else if (item.getItemId() == 100) {
                drawerLayout.close();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new RulesOverview(materialToolbar)).commit();
            } else if (item.getItemId() == 101){
                drawerLayout.close();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new ExercisesFragment("all", materialToolbar)).commit();
            } else if (item.getItemId() == 102) {
                drawerLayout.close();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new ExercisesFragment("unsorted", materialToolbar)).commit();
            } else if(item.getItemId() == 103) {
                drawerLayout.close();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new HomeFragment(materialToolbar)).commit();
            } else {
                drawerLayout.close();
                new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                        .setTitle("Oh no!")
                        .setMessage("This was not suppose to happen. The menu item you just clicked has an ID that is unknown to the application. Please wait for an update that may fix this error.")
                        .setPositiveButton("Okay", (dialog, which) -> dialog.cancel())
                        .show();
            }
            return true;
        });
    }

    private void getFileContent(File file) {
        StringBuilder fileContent = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        getMembers(fileContent);
    }


    private void getMembers(StringBuilder fileContent) {
        members = new ArrayList<>();

        Gson gson = new Gson();
        String jsonString = fileContent.toString();

        Type memberType = new TypeToken<ArrayList<Member>>(){}.getType();
        members = gson.fromJson(jsonString, memberType);
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
                    .setTitle("Share files")
                    .setMultiChoiceItems(strings, selected, (dialog, which, isChecked) -> selected[which] = isChecked)
                    .setPositiveButton("Share", (dialog, which) -> sendFiles(strings, selected))
                    .show();

            // TODO: 30.05.2023 Request, what file should be send
        }
        return true;
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

        Intent share = new Intent(Intent.ACTION_SEND_MULTIPLE);
        share.setType("application/octet-stream");
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
        for (int i = 0; i < files.size(); i++) {
            for (ResolveInfo ri : getPackageManager().queryIntentActivities(share, PackageManager.MATCH_DEFAULT_ONLY)){
                grantUriPermission(ri.activityInfo.packageName, files.get(i), Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
        startActivity(Intent.createChooser(share, "Share file"));

    }
}