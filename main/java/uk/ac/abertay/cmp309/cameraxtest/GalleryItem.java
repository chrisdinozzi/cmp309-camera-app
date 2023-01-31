package uk.ac.abertay.cmp309.cameraxtest;

import android.content.Context;
import android.util.AttributeSet;

public class GalleryItem extends androidx.appcompat.widget.AppCompatImageView {

    public GalleryItem(Context context) {
        super(context);
    }


    public GalleryItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GalleryItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
