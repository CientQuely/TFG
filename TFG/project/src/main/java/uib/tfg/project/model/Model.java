package uib.tfg.project.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;

import java.util.Observer;

import uib.tfg.project.model.Data.PictureObject;
import uib.tfg.project.model.representation.Quaternion;

/**
 * Created by Micky on 15/03/2018.
 */

public interface Model {
    double INITIAL_RANGE_OF_VIEW = 50.0;
    Location getUserCurrentLocation();


    //SENSORS
    Quaternion getUserRotation();
    void setUserRotation(Quaternion newRotation);
    void setUserCurrentLocation(Location location);

    //USER
    double getUserHeight();
    void setUserHeight(double height);
    void setCurrentUserBitmap(String path, Bitmap bitmap);
    Bitmap getCurrentBitmap();

    //DATA STORAGE
    void loadDataBase() throws ModelException.DB_Config_Exception, ModelException.DB_File_Exception;
    void closeDataBase() throws ModelException.DB_Config_Exception, ModelException.DB_File_Exception;
    void savePicture(Location location, double height, String img_path, Bitmap bitmap) throws InterruptedException;
    void deletePicture(PictureObject po) throws InterruptedException;
    void deleteDataBase();
    void createPicture(Location new_location, float new_height);
    Point loadNearBoxes();

    //OBSERVER
    void setUserObserver(Observer o);
    void removeUserObserver(Observer o);
}
