package uib.tfg.project.presenter;

import android.content.Context;
import android.location.Location;

/**
 * Created by Micky on 15/03/2018.
 */

public interface Presenter {
    void initiateLocationService();
    void stopLocationService();
    void initiateSensorsService();
    void stopSensorsService();
    Location getUserLocation();
    float [] getUserRotation();
    float [] getUserAcceleration();
    void initiatePictureLoader();
    void stopPictureLoader();
    void storeDataBase();
}
