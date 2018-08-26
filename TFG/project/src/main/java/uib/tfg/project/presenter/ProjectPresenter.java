package uib.tfg.project.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;

import uib.tfg.project.model.Model;
import uib.tfg.project.model.ModelException;
import uib.tfg.project.model.ProjectModel;
import uib.tfg.project.view.AugmentedReality;
import uib.tfg.project.view.View;


public class ProjectPresenter extends Thread implements Presenter{

    private Model model;
    private View view;
    private Context appContext;
    private LocationService locationService;
    private SensorsService sensorService;
    private PictureLoader pictureLoader;
    private final static String TAG = "Presenter";
    public ProjectPresenter (View v, Context appContext, Bitmap not_found_img){
        this.view = v;
        try {
            this.model = new ProjectModel(this, not_found_img);
        } catch (ModelException.Bitmap_Not_Found_Exception e) {
            Log.e(TAG, "Bitmap NOT_FOUND_IMG deleted");
            e.printStackTrace();
        }
        this.locationService = new LocationService(appContext, model,"Presenter/LocationService: ");
        this.sensorService = new SensorsService(appContext, model, "Presenter/SensorService: ");
        pictureLoader = new PictureLoader(model);

    }

    @Override
    public void run(){

    }

    @Override
    public void initiateLocationService() {
        if(locationService.getState() == Thread.State.NEW){
            locationService.start();
        }
    }

    @Override
    public void stopLocationService() {
        if(locationService.isRunning()){
            locationService.stopLocationService();
            locationService = new LocationService(appContext, model, TAG);
        }
    }

    @Override
    public void initiateSensorsService(){
        if(sensorService.getState() == Thread.State.NEW){
            sensorService.start();
        }
    }

    @Override
    public void stopSensorsService(){
        if(sensorService.isRunning()){
            sensorService.stopSensorsService();
            sensorService = new SensorsService(appContext, model, TAG);
        }
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

    @Override
    public void initiatePictureLoader() {
        if(pictureLoader.getState() == Thread.State.NEW){
            pictureLoader.start();
        }
    }

    @Override
    public void stopPictureLoader() {
        if(pictureLoader.isRunning()){
            pictureLoader.stopPictureLoader();
            pictureLoader = new PictureLoader(model);
        }
    }
    @Override
    public void storeDataBase() {
        try {
            model.closeDataBase();
        } catch (ModelException.DB_Config_Exception e) {
            Log.e(TAG, "Error: StoreDataBase - DB Config file not created");
            e.printStackTrace();
        } catch (ModelException.DB_File_Exception e) {
            Log.e(TAG, "Error: StoreDataBase - DB file not created");
            e.printStackTrace();
        }
    }

    @Override
    public void setContext(Context c) {
        appContext = c;
    }
}
