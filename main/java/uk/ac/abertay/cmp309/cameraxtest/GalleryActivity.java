package uk.ac.abertay.cmp309.cameraxtest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ImageFileHandler ifh;

    private ArrayList<String> neededPerms = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

    }

    @Override
    protected void onStart() {
        super.onStart();
        //check if external read permissions have been allowed.
        boolean checkResult = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (checkResult){
            ifh = new ImageFileHandler();
            populateGallery();
        } else{
            TextView txtNoPerm = findViewById(R.id.txtNoPerm);
            txtNoPerm.setVisibility(View.VISIBLE);
        }

    }


    private void populateGallery(){
        //hold all the image paths to load
        List<String> imagePaths = new ArrayList<>();

        //check if something was passed with an intent
        //if there was something passed, open that
        //else get all images
        Intent intent = getIntent();
        ArrayList<String> clusterImagePaths =  intent.getStringArrayListExtra("clusterImagePaths");
        if (clusterImagePaths != null){
            for (int i=0;i<clusterImagePaths.size();i++){
                String tmp = clusterImagePaths.get(i);
                imagePaths.add(tmp);
            }
        } else{
            imagePaths = ifh.getAllImagePaths();
        }

        //Check if there are any images to show.
        if (imagePaths.isEmpty() || imagePaths==null){
            TextView noImages = findViewById(R.id.txtNoImagesFound);
            noImages.setVisibility(View.VISIBLE);
        } else{ //no errors, proceed
            recyclerView = findViewById(R.id.recyclerView);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(this,3);

            Collections.reverse(imagePaths);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.setHasFixedSize(true);

            GalleryAdapter adapter = new GalleryAdapter(this, imagePaths);

            recyclerView.setAdapter(adapter);
        }
    }
}