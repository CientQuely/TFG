package uib.tfg.project.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;

import uib.tfg.project.model.Data.PictureObject;
import uib.tfg.project.model.representation.Quaternion;

/**
 * Created by Micky on 15/03/2018.
 */

public interface Presenter {
    //SENSORS

    Location getUserLocation();
    Quaternion getUserRotation();
    boolean isLocationServiceEnabled();

    void initiateLocationService();
    void stopLocationService();
    void initiateSensorsService();
    void stopSensorsService();


    void initiatePictureLoader();
    void stopPictureLoader();

    void setContext(Context c);

    //USER
    void setUserCurrentBitmap(String path, Bitmap bitmap);
    double getUserHeight();
    void setUserHeight(double height);
    Bitmap getCurrentBitmap();


    //DATA STORAGE
    void deleteDataBase();
    void storeDataBase();
    void createPicture(Location new_location, float new_height);
    void deletePicture(PictureObject pointed_picture) throws InterruptedException;
}
