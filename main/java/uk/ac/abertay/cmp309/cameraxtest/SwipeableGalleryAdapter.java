package uk.ac.abertay.cmp309.cameraxtest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class SwipeableGalleryAdapter extends RecyclerView.Adapter<SwipeableGalleryAdapter.ViewHolder> {

    private final LayoutInflater mInflator;
    private List<String> mImagepaths;//images to load
    private Context mContext;

    //Constructor
    SwipeableGalleryAdapter(Context context, List<String> imagepaths){
        mInflator = LayoutInflater.from(context);
        mImagepaths = imagepaths;
        mContext = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflator.inflate(R.layout.swipeable_gallery_layout, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SwipeableGalleryAdapter.ViewHolder holder, int position) {
        //Import images using the Glide library for a very noticeable increase in efficiency.
        Glide
                .with(mContext)
                .load(mImagepaths.get(position))
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.loading_foreground)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return mImagepaths.size();
    }

    //Custom viewhlders for the image
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        RelativeLayout relativeLayout;

        ViewHolder(View itemView) {
            super(itemView);
            relativeLayout = itemView.findViewById(R.id.rlImageSwipeable);
            imageView = itemView.findViewById(R.id.ivImageSwipeable);
        }
    }
}