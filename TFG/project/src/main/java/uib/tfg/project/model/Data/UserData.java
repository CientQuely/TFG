package uib.tfg.project.model.Data;

import android.graphics.Bitmap;
import android.location.Location;

import java.util.Observable;
import java.util.Observer;

import uib.tfg.project.model.representation.Quaternion;

public class UserData extends Observable {
    private float user_id;
    private volatile Location user_location;
    private volatile Quaternion user_rotation;
    private volatile Bitmap currentBitmap;
    private volatile String bitmapPath;
    public final int X_AXIS = 0;
    public final int Y_AXIS = 1;
    public final int Z_AXIS = 2;
    private double user_height;
    public UserData(float user_id){
        currentBitmap = null;
        this.user_id = user_id;
        this.user_rotation = new Quaternion();
    }
    public float getUser_id() {
        return user_id;
    }

    public void setUser_id(float user_id) {
        this.user_id = user_id;
    }

    public Location getUser_location() {
        return user_location;
    }

    public void setUser_location(Location user_location) {
        if( this.user_location != null
                && HashPictureBox.pictureUserBoxChanged(
                        this.user_location, user_location)){
            notifyObservers();
        }
        this.user_location = user_location;
    }

    public void setRotation(Quaternion newRotation){
        if(newRotation != null){
            this.user_rotation = newRotation;
        }
    }

    public Quaternion getRotation(){
        return user_rotation;
    }


    public void setObserver(Observer observer){
        this.addObserver(observer);
    }
    public void removeObserver(Observer observer) {
        this.deleteObserver(observer);
    }

    public void setCurrentBitmap(String path, Bitmap currentBitmap) {
        this.bitmapPath = path;
        this.currentBitmap = currentBitmap;
    }

    public double getUserHeight() {
        return user_height;
    }

    public void setUserHeight(double height) {
        user_height = height;
    }

    public Bitmap getCurrentBitmap(){
        return currentBitmap;
    }

    public String getCurrentBitmapPath(){
        return bitmapPath;
    }
}
