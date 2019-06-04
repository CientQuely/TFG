package uib.tfg.project.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;

import java.util.ArrayList;

import uib.tfg.project.model.Data.PictureObject;
import uib.tfg.project.model.Model;
import uib.tfg.project.model.representation.Quaternion;

/**
 * Created by Micky on 15/03/2018.
 */

public interface Presenter {

    int CREATE_DB = 0;
    int CREATE_PICTURE = 1;
    int DELETE_PICTURE = 2;
    int CLEAN_DB = 3;

    //SENSORS

    Model.GPS_MODE getCurrentGPSMode();
    void setCurrentGPSMode(Model.GPS_MODE currentMode);
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
    double getImageRemovalDistance();
    void setImageRemovalDistance(double newDistance);
    Bitmap getCurrentBitmap();


    //DATA STORAGE
    void stopDataBase();
    void startDataBase();

    void populateImagesFromDB();

    void deleteDataBase();
    void createPicture(double [] iPosition, float [] iRotation);
    void deletePicture(PictureObject pointed_picture);
    ArrayList<PictureObject> getNearestImages();

    //PICTURE

    Bitmap getPictureBitmap(PictureObject po);

    double[] getPicturePosition(PictureObject po);

    float [] getPictureRotationMatrix(PictureObject po);

    void pictureListUpToDate();

    boolean pictureListModified();

    float getPixelsPerCentimeterRatio();
    float getPixelsPerCentimeterRatio(PictureObject po);

    void setPixelsPerCentimeterRatio(float newRatio);
}
