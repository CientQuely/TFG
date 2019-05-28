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
    private double user_height;
    private double imageCreationDistance = 2;
    public UserData(float user_id){
        currentBitmap = null;
        this.user_id = user_id;
        this.user_rotation = new Quaternion();
    }
    private long lastUpdateTime = 0;
    private long deltaWaitingTime = 3000; // 3 seconds

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
                        this.user_location, user_location)
                && deltaWaitingTimeFinished()){
            notifyPictureLoader();
        }
        this.user_location = user_location;
    }

    public boolean deltaWaitingTimeFinished(){
        long currentTime = System.currentTimeMillis();

        if (currentTime > deltaWaitingTime + lastUpdateTime) {
            lastUpdateTime = currentTime;
            return true;
        }
        return false;
    }

    public void notifyPictureLoader(){
        notifyObservers();
    }

    public void setRotation(Quaternion newRotation){
        if(newRotation != null){
            this.user_rotation = newRotation;
        }
    }

    public double getImageCreationDistance(){
        return imageCreationDistance;
    }

    public void setImageCreationDistance(double newDistance){
        imageCreationDistance = newDistance;
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
