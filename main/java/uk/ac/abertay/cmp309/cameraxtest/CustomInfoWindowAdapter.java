package uk.ac.abertay.cmp309.cameraxtest;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter{
    private Activity mContext;

    CustomInfoWindowAdapter(Activity context){
        mContext = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        View view = mContext.getLayoutInflater().inflate(R.layout.image_info_window_layout,null);
        ImageView ivImage = view.findViewById(R.id.ivInfoWindowImage);
        ivImage.setImageBitmap(BitmapFactory.decodeFile(marker.getSnippet()));
        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}