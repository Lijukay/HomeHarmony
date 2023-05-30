package com.lijukay.famecrew.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.os.BuildCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import com.airbnb.lottie.BuildConfig;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lijukay.famecrew.R;
import com.lijukay.famecrew.fragments.ExercisesFragment;
import com.lijukay.famecrew.fragments.RulesOverview;
import com.lijukay.famecrew.fragments.MemberOverview;
import com.lijukay.famecrew.objects.Member;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.channels.FileChannel;
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

        int color = SurfaceColors.SURFACE_2.getColor(this);

        materialToolbar = findViewById(R.id.titleBar);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        Menu menu = navigationView.getMenu();

        materialToolbar.setBackgroundColor(color);
        getWindow().setNavigationBarColor(color);
        getWindow().setStatusBarColor(color);

        setSupportActionBar(materialToolbar);

        //Create submenu
        SubMenu subMenu = menu.addSubMenu("Rules");
        subMenu.add(0, 100, 0, "Rulebook").setCheckable(true);
        SubMenu memberMenu = menu.addSubMenu("Members");

        //check if file in file path exists
        if (getSharedPreferences("file_path_members", 0).getString("file_path_members", null) != null) {
            getFileContent(new File(getSharedPreferences("file_path_members", 0).getString("file_path_members", null)));
        } else {
            new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                    .setTitle("No file found")
                    .setMessage("There is no file saved so the app can't parse the file's content as it tries to. You may need to restart the process.")
                    .setPositiveButton("Okay", (dialog, which) -> dialog.cancel())
                    .show();
        }

        if (members != null) {
            for(int i = 0; i < members.size(); i++) {
                memberMenu.add(1, i, i+1, members.get(i).getPrename() + " (" + members.get(i).getNickname() + ")").setCheckable(true);
            }
            SubMenu exercises = menu.addSubMenu("Exercises");
            exercises.add(2, 101, members.size()+2, "Exercises Overview").setCheckable(true);
            exercises.add(2, 102, members.size() + 3, "Unsorted exercises").setCheckable(true);
        } else {
            new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                    .setTitle("Members are not initialized")
                    .setMessage("The list members is not existing. This normally happens, if the file, that contains all data has been deleted. Since the file is saved in the app's private storage, you either have root rights or the file was deleted by something else.\nYou cannot use this app without the file. To create a new one, you should delete the app's storage and start from the beginning.\n\nI am sorry for the troubles you have.")
                    .setPositiveButton("Okay", (dialog, which) -> dialog.cancel())
                    .show();
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            navigationView.setCheckedItem(item.getItemId());
            if (item.getItemId() != 100 && item.getItemId() != 101 && item.getItemId() != 102) {
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
            Intent share = new Intent(Intent.ACTION_SEND);

            share.setType("application/octet-stream");
            share.putExtra(Intent.EXTRA_STREAM,
                    Uri.parse("file://"+getSharedPreferences("file_path_ex", 0).getString("file_path_ex", null)));
            startActivity(Intent.createChooser(share, "Share file"));

            /*File sourceFile = new File(getSharedPreferences("file_path_ex", 0).getString("file_path_ex", null));
            String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + getString(R.string.app_name) + "-E.famecrew";

            try {
                FileChannel fileChannel = new FileInputStream(sourceFile).getChannel();
                FileChannel destinationChannel = new FileOutputStream(destination).getChannel();
                destinationChannel.transferFrom(fileChannel, 0, fileChannel.size());

                // Close the channels
                fileChannel.close();
                destinationChannel.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }*/
        }
        return true;
    }
}