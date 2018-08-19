package uib.tfg.project.presenter;

import android.content.Context;
import android.location.Location;

import uib.tfg.project.model.Model;
import uib.tfg.project.model.ProjectModel;
import uib.tfg.project.view.AugmentedReality;
import uib.tfg.project.view.View;


public class ProjectPresenter extends Thread implements Presenter{

    private Model model;
    private View view;
    private Context appContext;
    private LocationService locationService;
    private SensorsService sensorService;

    public ProjectPresenter (View v, Context appContext){
        this.view = v;
        this.model = new ProjectModel(this);
        this.locationService = new LocationService(appContext, model,"Presenter/LocationService: ");
        this.sensorService = new SensorsService(appContext, model, "Presenter/SensorService: ");

    }

    @Override
    public void run(){

    }

    @Override
    public void initiateLocationService() {
        if(!locationService.isRunning()) locationService.start();
    }

    @Override
    public void stopLocationService() {
        if(locationService.isRunning()) locationService.stopLocationService();
    }

    @Override
    public void initiateSensorsService(){
        if(!sensorService.isRunning()) sensorService.start();
    }

    @Override
    public void stopSensorsService(){
        if(sensorService.isRunning()) sensorService.stopSensorsService();
    }
    @Override
    public Location getUserLocation() {
        return model.getUserCurrentLocation();
    }

    @Override
    public float[] getUserRotation() {
        return model.getUserRotation();
    }

    @Override
    public float[] getUserAcceleration() {
        return model.getUserAcceleration();
    }
}
