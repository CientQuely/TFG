package uib.tfg.project.model;

import android.content.Context;
import android.location.Location;

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
}
