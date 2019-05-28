package uib.tfg.project.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;

import java.util.ArrayList;

import uib.tfg.project.model.Data.PictureObject;
import uib.tfg.project.model.representation.Quaternion;

/**
 * Created by Micky on 15/03/2018.
 */

public interface Presenter {
    //SENSORS

    double [] getUserLocationInMeters();
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
    Location getUserLocation();
    Quaternion getUserRotation();
    double getImageCreationDistance();
    void setImageCreationDistance(double newDistance);
    Bitmap getCurrentBitmap();


    //DATA STORAGE
    void stopDataBase();
    void startDataBase();
    void deleteDataBase();
    void createPicture(double [] iPosition, double [] iRotation);
    void deletePicture(PictureObject pointed_picture) throws InterruptedException;
    ArrayList<PictureObject> getNearestImages();

    //PICTURE

    Bitmap getPictureBitmap(PictureObject po);

    double[] getPicturePosition(PictureObject po);

    double [] getPictureRotation(PictureObject po);

    void pictureListUpToDate();

    boolean pictureListModified();
}
