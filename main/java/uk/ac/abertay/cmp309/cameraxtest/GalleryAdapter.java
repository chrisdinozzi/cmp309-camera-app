package uk.ac.abertay.cmp309.cameraxtest;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.CustomViewHolder>{

    private final ArrayList<String> mFilePathList;
    private final Activity mActivity;
    private Intent intent;

    public GalleryAdapter(Activity activity, List<String> filePathList){
        mActivity = activity;
        mFilePathList = new ArrayList<String>(filePathList);

    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_layout, parent,false);
        return new GalleryAdapter.CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryAdapter.CustomViewHolder holder, int position) {

        Glide
                .with(mActivity)
                .load(mFilePathList.get(position))
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .thumbnail(0.5f)
                .placeholder(R.drawable.loading_foreground)
                .centerCrop()
                .into(holder.imageResource);

        holder.imageResource.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                intent = new Intent (mActivity, SwipeableGallery.class);
                intent.putStringArrayListExtra("imagepaths",mFilePathList);
                intent.putExtra("position",position);
                mActivity.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFilePathList.size();
    }

    static class CustomViewHolder extends RecyclerView.ViewHolder{
        final ImageView imageResource;
        CustomViewHolder(View itemView){
            super(itemView);
            this.imageResource = (ImageView) itemView.findViewById(R.id.ivImage);
        }
    }
}
