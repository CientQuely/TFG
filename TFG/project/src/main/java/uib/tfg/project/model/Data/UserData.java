package uib.tfg.project.model.Data;

import android.graphics.Bitmap;
import android.location.Location;
import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import uib.tfg.project.model.Model;
import uib.tfg.project.model.representation.Quaternion;

public class UserData extends Observable {
    private float user_id;
    private volatile Location user_location;
    private volatile Quaternion user_rotation;
    private volatile Bitmap currentBitmap;
    private volatile String bitmapPath;
    private double user_height;
    private double imageCreationDistance = 2;
    private double imageRemovalDistance = 5;
    private LinkedList<Location> location_queue = new LinkedList<>();
    private float pixelPerCentimetreRatio = 10f;
    private int QUEUE_MAX_SIZE = 20;

    private Model.GPS_MODE currentMode = Model.GPS_MODE.LAST;

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

        addLocationToLocationQueue(user_location);

        Location normalized_location = getNormalizedLocation(currentMode);

        if( this.user_location != null
                && HashPictureBox.pictureUserBoxChanged(
                        this.user_location, normalized_location)
                && deltaWaitingTimeFinished()){
            notifyPictureLoader();
        }
        this.user_location = normalized_location;
    }

    private Location getNormalizedLocation(Model.GPS_MODE currentMode) {
        int size = location_queue.size();
        double longitude = 0;
        double latitude = 0;

        switch(currentMode){
            case LAST:
                return location_queue.getFirst();
            case AVERAGE:
                Location avg_location = new Location("");

                for (int i = 0; i < location_queue.size(); i++) {
                    Location current = location_queue.get(i);

                    longitude += current.getLongitude() / size;
                    latitude += current.getLatitude() / size;
                }

                avg_location.setLatitude(latitude);
                avg_location.setLongitude(longitude);
                return avg_location;

            case PONDERATION:
                Location pond_location = new Location("");

                size = location_queue.size();
                double [] ponderation = new double [size];
                double coefficient = 0;
                for (int i = 0; i < size; i++) {
                    ponderation[i] = size - i;
                    coefficient += ponderation[i];
                }

                for (int i = 0; i < size; i++) {
                    longitude += location_queue.get(i).getLongitude() * ponderation[i] / coefficient;
                    latitude += location_queue.get(i).getLatitude() * ponderation[i] / coefficient;
                }

                pond_location.setLatitude(latitude);
                pond_location.setLongitude(longitude);
                return pond_location;
        }
        return null;
    }

    private void addLocationToLocationQueue(Location user_location) {
        location_queue.addFirst(user_location);

        if(location_queue.size() > QUEUE_MAX_SIZE){
            location_queue.removeLast();
        }
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

    public double getImageRemovalDistance() {
        return imageRemovalDistance;
    }

    public void setImageRemovalDistance(double imageRemovalDistance) {
        this.imageRemovalDistance = imageRemovalDistance;
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

    public float getPixelPerCentimetreRatio() {
        return pixelPerCentimetreRatio;
    }

    public void setPixelPerCentimetreRatio(float pixelPerCentimetreRatio) {
        this.pixelPerCentimetreRatio = pixelPerCentimetreRatio;
    }

    public Model.GPS_MODE getCurrentGPSMode() {
        return currentMode;
    }

    public void setCurrentGPSMode(Model.GPS_MODE currentMode) {
        this.currentMode = currentMode;
        user_location = getNormalizedLocation(currentMode);
    }
}
