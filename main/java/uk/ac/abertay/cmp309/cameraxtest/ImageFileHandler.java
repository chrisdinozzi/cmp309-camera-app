package uk.ac.abertay.cmp309.cameraxtest;

import android.media.ExifInterface;
import android.os.Environment;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//Custom class written to make reading files easier, since this is functionality needed by a few activities.
public class ImageFileHandler{
    private File directoryFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    //used to filter for all .jpg and .png files
    //While the app does not support taking an image in the png format, the user may have png images on their device that they would still like to view with the app.
    private FilenameFilter filter = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            if (s.endsWith(".jpg")||s.endsWith(".png")){
                return true;
            } else{
                return false;
            }
        }
    };

    //a File array containing all files ending with .jpg and .png in the Pictures Directory
    private File[] allFiles = directoryFile.listFiles(filter);

    private boolean readwrite = false;
    private boolean readonly = false;


    public ImageFileHandler(){
        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED)){
            readwrite = true;
        } else if(storageState.equals(Environment.MEDIA_MOUNTED_READ_ONLY)){
            readonly = true;
        }
    }

    public List<String> getAllImagePaths(){
        if (readwrite || readonly){
            List<String> filePathList = new ArrayList<String>();
            for (int i=0;i<allFiles.length;i++){
                filePathList.add(allFiles[i].getAbsolutePath());
            }
            return filePathList;
        } else{
            return null;
        }
    }


    public LatLng getImageLatlng(File imgFile){
        if (readwrite||readonly){
            try {
                ExifInterface exifInterface = new ExifInterface(imgFile.getPath());

                float[] latLong = new float[2];

                if(exifInterface.getLatLong(latLong)){
                    LatLng latlng = new LatLng(latLong[0],latLong[1]);
                    return latlng;
                }
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else{
            return null;
        }
    }


    public boolean isExternalStorageWriteable(){
        String state = Environment.getExternalStorageState();
        if(state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }
}
