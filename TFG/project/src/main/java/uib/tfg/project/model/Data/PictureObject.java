package uib.tfg.project.model.Data;

import android.graphics.Bitmap;
import android.location.Location;

import java.io.Serializable;
import java.util.Vector;

public class PictureObject implements Serializable{
    private float picture_id;
    private float user_id;
    private String image_path;
    private Bitmap bitmap;
    private Location location;
    //In meters
    private double height;

    public PictureObject(float user_id, float picture_id, String img_path, Location img_location, double height){
            this.image_path = img_path;
            this.location = img_location;
            this.height = height;
            this.user_id = user_id;
            this.picture_id = user_id;
    }

    public void loadImageBitmap(){

    }

    public float getPicture_id() {
        return picture_id;
    }

    public void setPicture_id(int picture_id) {
        this.picture_id = picture_id;
    }

    public String getImage_path() {
        return image_path;
    }

    public float getUser_id() {
        return user_id;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    public boolean IsImageLoaded(){
        return bitmap != null;
    }

    public Bitmap getBitMap() {
        return bitmap;
    }

    public Location getLocation() {
        return location;
    }

    public double getHeight() {
        return height;
    }

    public boolean isThisPicture(float user_id, float picture_id){
        return user_id==this.user_id && picture_id==this.picture_id;
    }
    @Override
    public String toString(){
        return String.format("Picture ID: %2d, Location: %s",picture_id, location.toString());
    }
}
