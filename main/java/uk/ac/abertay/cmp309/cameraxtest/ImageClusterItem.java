package uk.ac.abertay.cmp309.cameraxtest;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

//A custom version of the ClusterItem to hold some data that makes the cluster map layout easier to develop and implement.
public class ImageClusterItem implements ClusterItem {
    private final LatLng mPosition;
    private final String mPath;
    private final String mTitle;
    private final String mSnippet;


    public ImageClusterItem(LatLng position, String path, String title, String snippet){
        mPosition = position;
        mPath = path;
        mTitle = title;
        mSnippet = snippet;

    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }

    public String getPath(){
        return mPath;
    }


}