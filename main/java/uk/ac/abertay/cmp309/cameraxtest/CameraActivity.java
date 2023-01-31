package uk.ac.abertay.cmp309.cameraxtest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Size;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCaseGroup;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;


public class CameraActivity extends AppCompatActivity {

    //CameraX
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private CameraSelector cameraSelector;
    private ImageCapture imageCapture;

    private Size res; //image resolution


    //Location Handling
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;


    //for playing a shutter noise
    private MediaPlayer mp;

    private ImageFileHandler ifh;

    //Metadata
    private int rotation; //rotation value
    private double userLat;
    private double userLng;
    private Location userLocation;

    //User Settings
    private String quality;
    private String shuttersound;
    private SettingsHandler settingsHandler;

    //UI elements
    private PreviewView previewView;
    private ToggleButton toggleCamera;
    private ToggleButton toggleFlash;

    private int lens;
    private int flash;

    private boolean cameraBound = false; //Camera is not bound on create


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.previewView);//the view the users see from the camera
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);//future for camera provider
        settingsHandler = new SettingsHandler(this);

        //used to hold the value set by the ui
        toggleCamera = findViewById(R.id.tbCamera);
        toggleFlash = findViewById(R.id.tbFlash);

        ifh = new ImageFileHandler();//used for writing to storage

    }

    @Override
    protected void onStart() {
        super.onStart();

        //set up orientation listener, for image meta data.
        //https://developer.android.com/training/camerax/configuration
        OrientationEventListener orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation >= 45 && orientation < 135) {
                    rotation = Surface.ROTATION_270;
                } else if (orientation >= 135 && orientation < 225) {
                    rotation = Surface.ROTATION_180;
                } else if (orientation >= 225 && orientation < 315) {
                    rotation = Surface.ROTATION_90;
                } else {
                    rotation = Surface.ROTATION_0;

                }
            }
        };
        orientationEventListener.enable();


        //-----------------
        //LOCATION HANDLING
        //-----------------
        boolean checkLocationPerm = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (checkLocationPerm) {
            //Location is used to geo tag images.
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        userLocation = location;
                        userLat = location.getLatitude();
                        userLng = location.getLongitude();
                    }

                }
            });
            fusedLocationClient.getLastLocation().addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CameraActivity.this, "An error has occured with your location, photos may not be geatagged.", Toast.LENGTH_LONG).show();
                }
            });
        }

        //-----------------
        //LOCATION HANDLING
        //-----------------
        locationRequest = LocationRequest.create();
        //Due to the nature of taking photos, the location does not need to update very quickly.
        // A user can't move a significant distance on foot in 5 seconds.
        // The only caveat is a user taking photos in a car, but this is a fair trade off between accuracy and battery life.
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(500);// If there's another app requesting location, this is how quickly it will update.
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult != null){
                    userLat = locationResult.getLastLocation().getLatitude();
                    userLng = locationResult.getLastLocation().getLongitude();
                }
            }
        };
        if(fusedLocationClient != null && checkLocationPerm){
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }


        //------------------
        //CAMERA PERMISSIONS
        //------------------
        //check camera permission, if denied, don't bind the camera.
        boolean checkCameraPermission  = checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        if (checkCameraPermission){
            cameraProviderFuture.addListener(() -> {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    if (!cameraBound){
                        bind(cameraProvider);
                        cameraBound = true; //to stop the camera trying to rebind
                    }

                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }, ContextCompat.getMainExecutor(this));
        } else{
            LinearLayout llError = (LinearLayout)findViewById(R.id.llError);
            llError.setVisibility(View.VISIBLE);
        }

        //-----------------
        //SETTINGS HANDLING
        //-----------------
        //photo quality, low, medium, or high. Denotes image resolution.
        //quality = preferences.getString("quality","medium");
        quality = settingsHandler.getQuality();
        switch(quality){
            case "low":
                res = new Size(640,480);
                break;
            case "medium":
                res = new Size(1440,1080);
                break;
            case "high":
                res = new Size(2048,1536);
                break;
        }

        //Set the shutter sound, this is just for fun more than anything else.
        shuttersound = settingsHandler.getShutterSound();
        //Create a media player to play said shutter sound.
        mp = MediaPlayer.create(this, getResources().getIdentifier(shuttersound,"raw",getPackageName()));

    }

    @Override
    protected void onResume() {
        super.onResume();

        //Get current toggle values
        if (!toggleCamera.isChecked()){
            lens = CameraSelector.LENS_FACING_BACK;
        } else{
            lens = CameraSelector.LENS_FACING_FRONT;
        }

        if (!toggleFlash.isChecked()){
            flash = ImageCapture.FLASH_MODE_OFF;
        } else{
            flash = ImageCapture.FLASH_MODE_ON;
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }

    }


    @SuppressLint({"UnsafeExperimentalUsageError", "RestrictedApi"})
    void bind(@NonNull ProcessCameraProvider cameraProvider) {
        //-------------
        //IMAGE PREVIEW
        //-------------
        //As the name suggests, this handles the preview of the image. It streams the view of the device camera to the PreviewView in a 4/3 ratio.
        //https://developer.android.com/training/camerax/preview
        Preview preview = new Preview.Builder()
                .setTargetRotation(rotation)
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build();


        //-------------
        //IMAGE CAPTURE
        //-------------
        //What to do when the image is captured. flash variable is toggled via the toggle button.
        //https://developer.android.com/training/camerax/take-photo
        imageCapture = new ImageCapture.Builder()
                .setFlashMode(flash)
                .setTargetRotation(rotation)
                .setDefaultResolution(res)
                .build();

        @SuppressLint("UnsafeExperimentalUsageError")
        UseCaseGroup useCaseGroup = new UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(imageCapture)
                .build();

        //Selects between front and back camera. lens variable is changed when the toggle button is pressed.
        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lens)
                .build();

        //Set the preview surface.
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        //Bind camera settings to lifecycle. CameraX can be lifecycle aware, making my life easier.
        cameraProvider.bindToLifecycle(this,cameraSelector,useCaseGroup);

        //----------
        //UI BUTTONS
        //----------
        //switch lens
        ToggleButton toggleCamera = findViewById(R.id.tbCamera);
        toggleCamera.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                cameraProvider.unbindAll();
                //on = front
                //off = back
                if (b){
                    lens = CameraSelector.LENS_FACING_FRONT;
                } else{
                    lens = CameraSelector.LENS_FACING_BACK;
                }
                bind(cameraProvider);
            }
        });

        //toggle flash
        ToggleButton toggleFlash = findViewById(R.id.tbFlash);
        toggleFlash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b){
                    flash = ImageCapture.FLASH_MODE_ON;
                } else{
                    flash = ImageCapture.FLASH_MODE_OFF;
                }
                imageCapture.setFlashMode(flash);
            }
        });

    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Allow a user to take a photo using the volume keys, either up or down.
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            capture(null);
        }

        //this override broke the back button functionality, this code below fixes it.
        if (keyCode == KeyEvent.KEYCODE_BACK){
            super.onBackPressed();
        }
        return true;
    }



    //Called when the capture button is pressed.
    public void capture(View v){
        //play sound effect
        mp.start();

        //play animation
        previewView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.capture_animation));

        //Check write external storage permissions are granted. If not, they image cannot be saved :(
        boolean checkResult = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if(checkResult && ifh.isExternalStorageWriteable()){
            //get timestamp for file name
            String timestamp = String.valueOf(System.currentTimeMillis());
            String imagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + "PHOTO_MAP_" + timestamp +".jpg";
            ImageCapture.OutputFileOptions outputFileOptions =
                    new ImageCapture.OutputFileOptions.Builder(new File(imagePath)).build();


            imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                                Toast.makeText(CameraActivity.this, "Location permissions not granted. Photo saved but not geotagged.", Toast.LENGTH_LONG).show();
                            } else if (userLocation!=null){
                                setGeoTag(imagePath);
                            }

                        }
                        @Override
                        public void onError(ImageCaptureException error) {
                            Toast.makeText(CameraActivity.this, "Could not save image... Please try again...", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else{
            Toast.makeText(this, "Cannot save photo, permission denied or storage not writeable...", Toast.LENGTH_LONG).show();
        }

    }

    
    //Handle gallery and map buttons
    public void openGallery(View v){
        Intent intent = new Intent(this, GalleryActivity.class);
        startActivity(intent);
    }

    public void openMap(View v){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }


    //Set the geotag of an image
    public void setGeoTag(String imagePath){
        ExifInterface exif = null;
        try{
            exif = new ExifInterface(imagePath);

            //set the lat and lng ref. either North South East or West
            String latRef;
            String lngRef;
            if (userLat>0){//north
                latRef = "N";
            } else{//south
                latRef = "S";
            }
            if (userLng>0){//east
                lngRef = "E";
            } else{//west
                lngRef="W";
            }

            //set image attributes
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE,dec2DMS(userLat));
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF,latRef);

            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE,dec2DMS(userLng));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF,lngRef);
            exif.saveAttributes();

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    //https://stackoverflow.com/a/15645984
    //By seanpj
    //This code turns a decimal value into a DMS, the format that the metadata requires.
    String dec2DMS(double coord) {
        coord = coord > 0 ? coord : -coord;  // -105.9876543 -> 105.9876543
        String sOut = Integer.toString((int)coord) + "/1,";   // 105/1,
        coord = (coord % 1) * 60;         // .987654321 * 60 = 59.259258
        sOut = sOut + Integer.toString((int)coord) + "/1,";   // 105/1,59/1,
        coord = (coord % 1) * 60000;             // .259258 * 60000 = 15555
        sOut = sOut + Integer.toString((int)coord) + "/1000";   // 105/1,59/1,15555/1000
        return sOut;
    }


}