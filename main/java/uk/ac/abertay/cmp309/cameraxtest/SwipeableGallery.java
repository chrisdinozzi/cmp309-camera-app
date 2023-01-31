package uk.ac.abertay.cmp309.cameraxtest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.util.List;

public class SwipeableGallery extends AppCompatActivity {
    ViewPager2 viewPager2;
    ImageFileHandler ifh;
    List<String> imagepaths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipeable_gallery);

        viewPager2 = findViewById(R.id.viewPager);
        ifh = new ImageFileHandler();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //check for an intent of certin images, if theres an intent, only load those images, else load all
        Intent intent = getIntent();
        imagepaths = intent.getStringArrayListExtra("imagepaths");
        if (!imagepaths.isEmpty()){
            viewPager2.setAdapter(new SwipeableGalleryAdapter(this, imagepaths));

            if (intent.hasExtra("position")) {//custom position to start at i.e. user selects the fourth image along, so start the swipeable gallery at image four
                int customPosition = intent.getIntExtra("position", 0);
                viewPager2.setCurrentItem(customPosition,false);
            }

        } else{
            Toast.makeText(this, "No Images Selected", Toast.LENGTH_LONG).show();//I can't imagine the user ever managing to get to this point...
        }

    }

    //Allows the user to see the location an image was taken.
    public void viewOnMap(View v){
        int currentItem = viewPager2.getCurrentItem();
        LatLng latLng = ifh.getImageLatlng(new File(imagepaths.get(currentItem)));
        if (latLng==null){
            Toast.makeText(this, "Error: No location found for this image...", Toast.LENGTH_LONG).show();
        } else {
            //open the map at those latlng
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("latlng", latLng);
            startActivity(intent);
        }
    }
}