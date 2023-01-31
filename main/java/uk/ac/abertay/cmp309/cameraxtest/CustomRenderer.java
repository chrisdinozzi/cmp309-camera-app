package uk.ac.abertay.cmp309.cameraxtest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.util.Collection;
import java.util.Iterator;

public class CustomRenderer extends DefaultClusterRenderer<ImageClusterItem> {
    private final Context mContext;


    public CustomRenderer(Context context, GoogleMap map, ClusterManager<ImageClusterItem> clusterManager) {
        super(context, map, clusterManager);
        setMinClusterSize(4);//the minimum number of items required to form a cluster
        mContext = context;
    }


    @Override
    protected void onBeforeClusterItemRendered(ImageClusterItem item, MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);
        markerOptions.icon(getBitmapDescriptor(R.drawable.photo_pin));
        markerOptions.snippet(item.getSnippet());//This snippet is then used to load an image into the InfoWindow
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<ImageClusterItem> cluster, MarkerOptions markerOptions) {
        super.onBeforeClusterRendered(cluster, markerOptions);
        Collection<ImageClusterItem> items = cluster.getItems();
        Iterator<ImageClusterItem> iterator = items.iterator();
        ImageClusterItem first = iterator.next();//Get the first item in the cluster

        //Generate and populate the custom icon used for clusters.
        IconGenerator clusterIconGenerator = new IconGenerator(mContext);
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View iconlayout = inflater.inflate(R.layout.cluster_icon_layout,null,false);

        //Add first three images to icon
        ImageView image1 = iconlayout.findViewById(R.id.ivImage1);
        image1.setImageBitmap(BitmapFactory.decodeFile(iterator.next().getPath()));
        ImageView image2 = iconlayout.findViewById(R.id.ivImage2);
        image2.setImageBitmap(BitmapFactory.decodeFile(iterator.next().getPath()));
        ImageView image3 = iconlayout.findViewById(R.id.ivImage3);
        image3.setImageBitmap(BitmapFactory.decodeFile(iterator.next().getPath()));

        //Final slot on icon grid taken up by an indicator of how many more images are in the cluster.
        clusterIconGenerator.setContentView(iconlayout);
        clusterIconGenerator.makeIcon("+"+(cluster.getSize()-3));

        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(clusterIconGenerator.makeIcon());
        markerOptions.icon(icon);

        markerOptions.snippet(first.getSnippet());//This snippet is then used to load an image into the InfoWindow

    }

    //https://gist.github.com/Ozius/1ef2151908c701854736
    private BitmapDescriptor getBitmapDescriptor(int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            VectorDrawable vectorDrawable = (VectorDrawable) mContext.getDrawable(id);

            int h = vectorDrawable.getIntrinsicHeight();
            int w = vectorDrawable.getIntrinsicWidth();

            vectorDrawable.setBounds(0, 0, w, h);

            Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bm);
            vectorDrawable.draw(canvas);

            return BitmapDescriptorFactory.fromBitmap(bm);

        } else {
            return BitmapDescriptorFactory.fromResource(id);
        }
    }

}
