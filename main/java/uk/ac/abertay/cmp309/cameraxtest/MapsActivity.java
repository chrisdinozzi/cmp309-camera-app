package uk.ac.abertay.cmp309.cameraxtest;
//todo
//look into IconGenerator

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ImageFileHandler ifh;
    private List<String> imagePaths;
    ClusterManager<ImageClusterItem> clusterManager;



    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    private ArrayList<String> neededPerms = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

    }

    @Override
    protected void onStart() {
        super.onStart();
        for (String perm : REQUIRED_PERMISSIONS){
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED){//permission not granted
                neededPerms.add(perm);
            }
        }

        if (neededPerms.size()!=0){
            requestPermissions(neededPerms.toArray(new String[0]),1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){//permission not granted
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Permission Denied");
            alertDialog.setMessage("Read External Storage Permissions have been denied, so images cannot be displayer on the map. Please allow this permission in the Android Settings or when prompted again.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        //check permissions
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {//permission not granted
            initMap();
        }
    }

    public void initMap(){

        //check if the map was opened via the gallery, is it was, we want to zoom to the photo that it was opened for
        Intent intent = getIntent();
        if (intent.hasExtra("latlng")){
            LatLng customLatlng = intent.getParcelableExtra("latlng");
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(customLatlng,19);
            mMap.animateCamera(cameraUpdate);
        }

        //retrieve image file paths
        ifh = new ImageFileHandler();
        imagePaths = ifh.getAllImagePaths();

        //if clustermanager has already been set up, don't bother doing it all again. This stops it being recreated when coming back from the gallery.
        if (clusterManager == null) {
            clusterManager = new ClusterManager<>(this, mMap);

            //Give control of certain listeners that map usually has control over to the cluster manager
            mMap.setOnCameraIdleListener(clusterManager);
            mMap.setOnInfoWindowClickListener(clusterManager);

            //load in markers based off image metadata
            if (imagePaths==null){
                Toast.makeText(this,
                        "External storage is not readable",
                        Toast.LENGTH_LONG).show();
            } else {
                for (int i = 0; i < imagePaths.size(); i++) {
                    File file = new File(imagePaths.get(i));
                    LatLng latlng = ifh.getImageLatlng(file);
                    if (latlng != null) {//not all images have latlng metadata so best to check before trying to use NULL data
                        clusterManager.addItem
                                (new ImageClusterItem(latlng, file.getPath(), file.getName(), file.getAbsolutePath())); //location, path, title, snippet
                    }
                }
            }

            //set custom renderer
            CustomRenderer customRenderer;
            customRenderer = new CustomRenderer(this, mMap, clusterManager);
            clusterManager.setRenderer(customRenderer);


            //Setup custom info window information
            CustomInfoWindowAdapter customInfoWindowAdapter = new CustomInfoWindowAdapter(this);
            clusterManager.getMarkerCollection().setOnInfoWindowAdapter(customInfoWindowAdapter);
            clusterManager.getClusterMarkerCollection().setOnInfoWindowAdapter(customInfoWindowAdapter);

            //When a cluster window is clicked, it opens the gallery with only the images in the cluster, allowing the user to scroll or swipe through those photos
            clusterManager.setOnClusterInfoWindowClickListener(new ClusterManager.OnClusterInfoWindowClickListener<ImageClusterItem>() {
                @Override
                public void onClusterInfoWindowClick(Cluster<ImageClusterItem> cluster) {


                    //Create an arraylist with al the imagepaths to open
                    ArrayList<String> imagePaths = new ArrayList<>();
                    cluster.getItems().toArray();
                    for (ImageClusterItem tmp : cluster.getItems()){
                        imagePaths.add(tmp.getPath());
                    }


                    //Create the intent and add the arraylist of paths to an extra before starting the gallery activity
                    Intent intent = new Intent(MapsActivity.this, GalleryActivity.class);
                    intent.putStringArrayListExtra("clusterImagePaths", imagePaths);
                    startActivity(intent);
                }
            });

            //Same as above code but instead of a cluster it's for the single image and opens it straight into the swipeable gallery, giving the illusion of a fullscreen view.
            clusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<ImageClusterItem>() {
                @Override
                public void onClusterItemInfoWindowClick(ImageClusterItem imageClusterItem) {
                    //open swipeable gallery for a full screen image
                    ArrayList<String> path = new ArrayList<>();
                    path.add(imageClusterItem.getPath());
                    Intent intent = new Intent(MapsActivity.this, SwipeableGallery.class);
                    intent.putStringArrayListExtra("imagepaths", path);
                    startActivity(intent);
                }
            });

            mMap.setInfoWindowAdapter(clusterManager.getMarkerManager());
            mMap.setOnInfoWindowClickListener(clusterManager);

            //Create the cluters
            clusterManager.cluster();
        }
    }
}