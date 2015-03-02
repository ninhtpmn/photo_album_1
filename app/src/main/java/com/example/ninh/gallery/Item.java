package com.example.ninh.gallery;

import android.graphics.Bitmap;


public class Item {
    Bitmap image;
    String title;
    String path;

//    public Item(Bitmap image, String title) {
//        super();
//        this.image = image;
//        this.title = title;
//    }

    public Item(Bitmap image, String title, String path) {
        super();
        this.image = image;
        this.title = title;
        this.path = path;
    }

    public Bitmap getImage() {
        return image;
    }
    public void setImage(Bitmap image) {
        this.image = image;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }


    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
}