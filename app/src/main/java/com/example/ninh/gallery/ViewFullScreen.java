package com.example.ninh.gallery;

import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ninh on 06/02/2015.
 */
public class ViewFullScreen extends ActionBarActivity{

    static TextView tv;
    static ActionBar actionbar;
    Bundle extras;
    private FullScreenImageAdapter adapter;
    ViewPager viewPager;
    static int width;
    static int height;


    @Override
    public void onBackPressed() {

       if(tv.isShown())
       {
           tv.setVisibility(View.INVISIBLE);
           actionbar.hide();
       }
        else
            super.onBackPressed();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Display display = getWindowManager().getDefaultDisplay();
        width = display.getWidth();
        height = display.getHeight();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_fullscreen);

        actionbar = getSupportActionBar();
        actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.hide();

        tv = (TextView)findViewById(R.id.info);
        tv.setVisibility(View.INVISIBLE);


        viewPager = (ViewPager) findViewById(R.id.pager);

        extras = getIntent().getExtras();
        int pos = extras.getInt("POSITION");
        adapter = new FullScreenImageAdapter(this, ViewListImage.gridArray);

        viewPager.setAdapter(adapter);
        // displaying selected image first
        viewPager.setCurrentItem(pos);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                tv.setVisibility(View.INVISIBLE);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_image_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.setbg:
                    WallpaperManager myWallpaperManager = WallpaperManager.getInstance(this);
                    try {
                        Bitmap bitmap = BitmapFactory.decodeFile(ViewListImage.gridArray.get(viewPager.getCurrentItem()).getPath());
                        myWallpaperManager.setBitmap(bitmap);
                        Toast.makeText(this,
                                "Wallpaper successfully changed", Toast.LENGTH_SHORT)
                                .show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                return true;

            case R.id.delete:

                    String path = ViewListImage.gridArray.get(viewPager.getCurrentItem()).getPath();

                    try {
                        getContentResolver().delete(getUriFromPath(path), null, null);
                    }
                    catch (Exception e)
                    {
                        }

                File file = new File(path);
                file.delete();


                if(!new File(path).exists()) {
                    Toast.makeText(this, "Delete successfully", Toast.LENGTH_SHORT).show();

                    ViewListImage.gridArray.remove(viewPager.getCurrentItem());
                    GridviewAdapter customGridAdapter2 = new GridviewAdapter(this, R.layout.row_grid, ViewListImage.gridArray);
                    ViewListImage.gridview.setAdapter(customGridAdapter2);
                }
                else Toast.makeText(this, "Cannot delete this picture", Toast.LENGTH_SHORT).show();

                    if(tv.isShown()) tv.setVisibility(View.INVISIBLE);

                    onBackPressed();

                return true;

            case R.id.Share:

                share(ViewListImage.gridArray.get(viewPager.getCurrentItem()).getPath());

                return true;

            case  R.id.ViewInfo:

                int width = DecodeBitmap.Size.x;
                int height = DecodeBitmap.Size.y;
                File file1 = new File(ViewListImage.gridArray.get(viewPager.getCurrentItem()).getPath());

                Date date = new Date(file1.lastModified());
                DateFormat formatter =  new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                String formattedDate = formatter.format(date);


                String size = getSize(file1);
                tv.setText("Details:"+"\n"+"Width: "+width + "\n" + "Height: "+height +"\n" + "Size: " + size + "\n" + "Date:" + formattedDate);
                tv.setVisibility(View.VISIBLE);
                return true;


        }
        return false;
    }

    private void share(String imagePath) {
        try
        {
            String face ="facebook", tw ="twitter";
            List<Intent> targetedShareIntents = new ArrayList<>();
            Intent share = new Intent(android.content.Intent.ACTION_SEND);
            share.setType("image/*");
            List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(share, 0);
            if (!resInfo.isEmpty()){
                for (ResolveInfo info : resInfo) {
                    Intent targetedShare = new Intent(android.content.Intent.ACTION_SEND);
                    targetedShare.setType("image/*");
                    if (info.activityInfo.packageName.toLowerCase().contains(face) || info.activityInfo.name.toLowerCase().contains(face) ||
                    info.activityInfo.packageName.toLowerCase().contains(tw) || info.activityInfo.name.toLowerCase().contains(tw)
                            ) {
                        targetedShare.putExtra(Intent.EXTRA_SUBJECT, "Photo");
                        targetedShare.putExtra(Intent.EXTRA_TEXT,"Test Project 1");
                        targetedShare.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(imagePath)) );
                        targetedShare.setPackage(info.activityInfo.packageName);
                        targetedShareIntents.add(targetedShare);
                    }
                }
                Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), "Share via");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
                startActivity(chooserIntent);
            }
        }
        catch(Exception e){
            Log.v("VM","Exception while sending image on"  + " "+  e.getMessage());
        }
    }

    private String getSize(File file) {
        DecimalFormat df = new DecimalFormat("0.##");

        float size = file.length();
        if(size/1024<1) return (df.format(size) + "Bytes");
        if(size/1024/1024<1)
        {
            size = size/1024;

            return (df.format(size)  + "KB");

        }
        if(size/1024/1024/1024<1)
        {
            size = size/1024/1024;
            return (df.format(size) + "MB");
        }

        return null;
    }


    private Uri getUriFromPath(String filePath) {
        long photoId;
        Uri photoUri = MediaStore.Images.Media.getContentUri("external");

        String[] projection = {MediaStore.Images.ImageColumns._ID};
// TODO This will break if we have no matching item in the MediaStore.
        Cursor cursor = getContentResolver().query(photoUri, projection, MediaStore.Images.ImageColumns.DATA + " LIKE ?", new String[]{filePath}, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(projection[0]);
        photoId = cursor.getLong(columnIndex);

        cursor.close();
        return Uri.parse(photoUri.toString() + "/" + photoId);
    }

    }
