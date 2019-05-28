package uib.tfg.project.model;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;

import java.util.ArrayList;
import java.util.Observer;

import uib.tfg.project.model.Data.PictureObject;
import uib.tfg.project.model.representation.Quaternion;

/**
 * Created by Micky on 15/03/2018.
 */

public interface Model {

    //SENSORS
    Quaternion getUserRotation();

    void cleanPictureHash() throws InterruptedException;

    void cleanPictureList();

    Location getUserCurrentLocation();
    void setUserRotation(Quaternion newRotation);
    void setUserCurrentLocation(Location location);
    void setImageCreationDistance(double newDistance);
    double getUserHeight();
    void setUserHeight(double height);
    void setCurrentUserBitmap(String path, Bitmap bitmap);
    Bitmap getCurrentBitmap();

    //USER
    double getImageCreationDistance();

    //PICTURE OBJECT
    Bitmap getImageBitmap(PictureObject po);
    Location getImageLocation(PictureObject po);
    double getHeight(PictureObject po);
    double [] getRotation(PictureObject po);


    //PICTURE LIST
    ArrayList<PictureObject> getImageList();
    //Can't only be used for one thread OpenGL
    boolean isPictureListModified();
    void setPictureListModified(boolean value);

    //DB
    void startDataBase();
    void closeDataBase();
    void cleanDataBase();
    void loadDataBase() throws InterruptedException;

    //DB / HASH
    void deletePicture(PictureObject po) throws InterruptedException;
    void createPicture(Location picLocation, double height, double [] picRotation);
    Point loadNearBoxes();
    void removeFarCacheImagesBitmap();

    //OBSERVER
    void setUserObserver(Observer o);
    void removeUserObserver(Observer o);
}
