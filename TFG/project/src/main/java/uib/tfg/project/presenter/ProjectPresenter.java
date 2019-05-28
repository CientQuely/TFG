package uib.tfg.project.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.location.Location;

import java.util.ArrayList;

import uib.tfg.project.model.Data.PictureObject;
import uib.tfg.project.model.Model;
import uib.tfg.project.model.ProjectModel;
import uib.tfg.project.model.representation.Quaternion;
import uib.tfg.project.view.View;


public class ProjectPresenter extends Thread implements Presenter{

    private Model model;
    private View view;
    private Context appContext;
    private LocationService locationService;
    //private SensorsService sensorService;
    private PictureLoader pictureLoader;
    private OrientationSensorProvider sensorService;
    private final static String TAG = "Presenter";
    public ProjectPresenter (View v, Context appContext, Bitmap not_found_img){
        this.view = v;

        this.model = new ProjectModel(this, not_found_img, appContext);
        this.locationService = new LocationService(appContext, model,"Presenter/LocationService: ");
        //this.sensorService = new SensorsService(appContext, model, "Presenter/SensorService: ");
        SensorManager sensorsManager = (SensorManager) appContext.getSystemService(Context.SENSOR_SERVICE);
        sensorService = new OrientationSensorProvider(sensorsManager, model);
        pictureLoader = new PictureLoader(model);

    }

    @Override
    public void run(){

    }

    @Override
    public void initiateLocationService() {
        if(!locationService.isRunning()){
            locationService.start();
        }
    }

    @Override
    public void stopLocationService() {
        if(locationService.isRunning()){
            locationService.stopLocationService();
        }
    }

    @Override
    public void initiateSensorsService(){
        if(!sensorService.isRunning()){
            sensorService.start();
        }
    }

    @Override
    public void stopSensorsService(){
        if(sensorService.isRunning()){
            sensorService.stop();
        }
    }
    @Override
    public double [] getUserLocationInMeters() {
        double [] user_position = new double [3];
        Location user_location = model.getUserCurrentLocation();
        double [] locationInMeters = LocationService.locationToMeters(user_location);
        user_position[0] = locationInMeters[0];
        user_position[1] = locationInMeters[1];
        user_position[2] = getUserHeight();

        return user_position;
    }

    @Override
    public Location getUserLocation() {
        return model.getUserCurrentLocation();
    }

    @Override
    public Quaternion getUserRotation() {
        return model.getUserRotation();
    }

    @Override
    public double getImageCreationDistance() {
        return model.getImageCreationDistance();
    }

    @Override
    public void setImageCreationDistance(double newDistance) {
        model.setImageCreationDistance(newDistance);
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
            pictureLoader.interrupt();
            pictureLoader = new PictureLoader(model);
        }
    }
    @Override
    public void stopDataBase() {
        model.closeDataBase();
    }

    @Override
    public void startDataBase() {
        model.startDataBase();
    }

    @Override
    public void setContext(Context c) {
        appContext = c;
    }

    @Override
    public boolean isLocationServiceEnabled() {
        return locationService.isRunning();
    }

    @Override
    public void setUserCurrentBitmap(String path, Bitmap bitmap) {
        model.setCurrentUserBitmap(path, bitmap);
    }

    @Override
    public double getUserHeight() {
        return model.getUserHeight();
    }

    @Override
    public void setUserHeight(double height) {
        model.setUserHeight(height);
    }

    @Override
    public void deleteDataBase() {
        model.cleanDataBase();
        try {
            model.cleanPictureHash();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        model.cleanPictureList();
    }

    @Override
    public Bitmap getCurrentBitmap(){
        return model.getCurrentBitmap();
    }

    @Override
    public void createPicture(double [] iPosition, double [] iRotation) {
        Location imgLocation = LocationService.metersToLocation(iPosition[0], iPosition[1]);
        model.createPicture(imgLocation, iPosition[2],  iRotation);
    }

    @Override
    public void deletePicture(PictureObject pointed_picture) throws InterruptedException {
        model.deletePicture(pointed_picture);
    }

    @Override
    public ArrayList<PictureObject> getNearestImages() {
        return model.getImageList();
    }

    @Override
    public Bitmap getPictureBitmap(PictureObject po){
        return model.getImageBitmap(po);
    }

    @Override
    public double[] getPicturePosition(PictureObject po){
        Location imgLocation = model.getImageLocation(po);
        double [] imgPosition = new double [3];
        double [] locationInMeters = LocationService.locationToMeters(imgLocation);
        double height = model.getHeight(po);

        imgPosition[0] = locationInMeters[0];
        imgPosition[1] = locationInMeters[1];
        imgPosition[2] = height;

        return imgPosition;
    }

    @Override
    public double [] getPictureRotation(PictureObject po){
        return model.getRotation(po);
    }

    @Override
    public boolean pictureListModified(){
        return model.isPictureListModified();
    }

    @Override
    public void pictureListUpToDate(){
        model.setPictureListModified(false);
    }

}
