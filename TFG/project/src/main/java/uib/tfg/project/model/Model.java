package uib.tfg.project.model;

import android.content.Context;
import android.graphics.Point;
import android.location.Location;

import uib.tfg.project.model.Data.PictureObject;

/**
 * Created by Micky on 15/03/2018.
 */

public interface Model {
    double INITIAL_RANGE_OF_VIEW = 50.0;
    Location getUserCurrentLocation();
    float [] getUserRotation();
    float [] getUserAcceleration();
    void setUserCurrentLocation(Location location);
    void setUserAcceleration(float[] new_acceleration);
    void setUserRotation(float[] new_rotation);
    void loadDataBase() throws ModelException.DB_Config_Exception, ModelException.DB_File_Exception;
    void closeDataBase() throws ModelException.DB_Config_Exception, ModelException.DB_File_Exception;
    void savePicture(Location location, double height, String img_path) throws InterruptedException;
    void deletePicture(PictureObject po) throws InterruptedException;
    Point getUserLocationBox();
}
