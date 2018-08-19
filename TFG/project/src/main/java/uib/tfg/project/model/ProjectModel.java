package uib.tfg.project.model;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.support.v4.app.ActivityCompat;

import uib.tfg.project.model.Data.UserCurrentData;
import uib.tfg.project.presenter.Presenter;

/**
 * Created by Micky on 15/03/2018.
 */

public class ProjectModel implements Model{

    private Presenter presenter;
    UserCurrentData currentUserData;
    private PictureDataBase picturedb;
    public ProjectModel(Presenter p){
        this.presenter = p;
        currentUserData = new UserCurrentData("123");
    }

    @Override
    public Location getUserCurrentLocation() {
        return currentUserData.getUser_location();
    }

    @Override
    public float[] getUserRotation() {
        return currentUserData.get_Rotation();
    }

    @Override
    public float[] getUserAcceleration() {
        return currentUserData.get_Acceleration();
    }

    @Override
    public void setUserCurrentLocation(Location location) {
        currentUserData.setUser_location(location);
    }

    @Override
    public void setUserAcceleration(float[] new_acceleration) {
        currentUserData.setUser_acceleration(new_acceleration);
    }

    @Override
    public void setUserRotation(float[] new_rotation) {
        currentUserData.setUser_rotation(new_rotation);
    }
}
