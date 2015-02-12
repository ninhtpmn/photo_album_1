package com.example.ninh.gallery;

/**
 * Created by ninh on 10/02/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.File;
import java.util.ArrayList;

public class FullScreenImageAdapter extends PagerAdapter {

    private Activity _activity;
    private ArrayList<Item> _imagePaths;
    private LayoutInflater inflater;
    Bitmap bitmap;
    String path;
    int k;

    // constructor
    public FullScreenImageAdapter(Activity activity, ArrayList<Item> imagePaths) {
        this._activity = activity;
        this._imagePaths = imagePaths;
        k = ViewListImage.gridArray.size();
    }

    @Override
    public int getCount() {
        if(ViewListImage.gridArray.size()!=k)
        {
            k= ViewListImage.gridArray.size();
            notifyDataSetChanged();
        }

        return this._imagePaths.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((FrameLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {

        TouchImageView imgDisplay;

        inflater = (LayoutInflater) _activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View viewLayout = inflater.inflate(R.layout.pager_adapter_layout, container,false);

        imgDisplay = (TouchImageView)viewLayout.findViewById(R.id.imageview);
        imgDisplay.setZoom(1f/3, 10f);

        path = _imagePaths.get(position).getPath();

        DecodeBitmap de = new DecodeBitmap();
        bitmap = de.decodeSampledBitmapFromPath(path, ViewFullScreen.width, ViewFullScreen.height);
        imgDisplay.setImageBitmap(bitmap);

        ((ViewPager) container).addView(viewLayout);

            imgDisplay.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!ViewFullScreen.actionbar.isShowing()) {
                        ViewFullScreen.actionbar.setTitle(new File(path).getName());
                        ViewFullScreen.actionbar.show();
                    } else {
                        ViewFullScreen.actionbar.hide();
                        ViewFullScreen.tv.setVisibility(View.INVISIBLE);
                    }
                }
            });


        return viewLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((FrameLayout) object);

    }
}