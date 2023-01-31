package uk.ac.abertay.cmp309.cameraxtest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION};
    private ArrayList<String> neededPerms = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Since the app is essentially pointless without camera, storage and location permissions, these are requested when the app is first launched.
        //check permissions
        for (String perm : REQUIRED_PERMISSIONS){
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED){//permission not granted
                neededPerms.add(perm);
            }
        }
        if (neededPerms.size()!=0){
            requestPermissions(neededPerms.toArray(new String[0]),1);
        }

    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length>0){//checks there's actually something in there to deal with
            for(int i=0;i<grantResults.length;i++){
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Permissions not granted, some features may be limited.", Toast.LENGTH_LONG).show();
                }
            }
        }

    }


    //Functions to assign to the four different buttons that appear at launch.
    public void openCamera(View v){
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    public void openGallery(View v){
        Intent intent = new Intent(this, GalleryActivity.class);
        startActivity(intent);
    }

    public void openMap(View v){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public void openSettings(View v){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}