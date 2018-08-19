package uib.tfg.project.model.Data;

import android.graphics.Bitmap;
import android.location.Location;

import java.io.Serializable;
import java.util.Vector;

public class PictureObject implements Serializable{
    private int picture_id;
    private int user_id;
    private String image_path;
    private Bitmap image_bitmap;
    private Location image_location;
    private Vector normal_vector;

    public PictureObject(String img_path, int user_id, Location img_location, Vector normal_vector){
            this.image_path = img_path;
            this.image_location = img_location;
            this.normal_vector = normal_vector;
            this.user_id = user_id;
    }

    public void loadImageBitmap(){

    }

    public int getPicture_id() {
        return picture_id;
    }

    public void setPicture_id(int picture_id) {
        this.picture_id = picture_id;
    }

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    public boolean IsImageOpen(){
        return image_bitmap != null;
    }
    public Bitmap getImage_bitmap() {
        return image_bitmap;
    }

    public Location getImage_location() {
        return image_location;
    }

    public Vector getNormal_vector() {
        return normal_vector;
    }

    @Override
    public String toString(){
        return String.format("Picture ID: %2d, Location: %s",picture_id, image_location.toString());
    }
}
