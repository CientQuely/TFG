package uib.tfg.project.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.location.Location;
import android.util.Log;

import java.util.ArrayList;

import uib.tfg.project.model.Data.PictureObject;
import uib.tfg.project.model.Model;
import uib.tfg.project.model.ProjectModel;
import uib.tfg.project.model.representation.Quaternion;
import uib.tfg.project.view.View;


public class ProjectPresenter implements Presenter{

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
    public double getImageRemovalDistance() {
        return model.getImageRemovalDistance();
    }

    @Override
    public void setImageRemovalDistance(double newDistance) {
        model.setImageRemovalDistance(newDistance);
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
    public Bitmap getCurrentBitmap(){
        return model.getCurrentBitmap();
    }


    @Override
    public void populateImagesFromDB(){
        new Thread(new Runnable() {
            private Model model;
            public Runnable init(Model m) {
                this.model = m;
                return this;
            }
            @Override
            public void run() {
                try {
                    model.loadDataBase();
                } catch (InterruptedException e) {
                    Log.e("Model", "exception", e);
                }
                model.loadNearBoxes();
            }
        }.init(model)).start();
    }

    @Override
    public void deleteDataBase() {
        new Thread(new Runnable() {
            private Model model;
            public Runnable init(Model m) {
                this.model = m;
                return this;
            }
            @Override
            public void run() {
                model.cleanDataBase();
                model.cleanPictureHash();
                model.cleanPictureList();
            }
        }.init(model)).start();
    }

    @Override
    public void createPicture(double [] iPosition, float [] iRotation) {
        Location imgLocation = LocationService.metersToLocation(iPosition[0], iPosition[1]);
        new Thread(new Runnable() {
            private Model model;
            private Location l;
            private double height;
            private float [] rotation;
            public Runnable init(Location l, double height, float [] rotation, Model m) {
                this.l = l;
                this.height = height;
                this.rotation = rotation;
                this.model = m;
                return this;
            }
            @Override
            public void run() {

                model.createPicture(l, height, rotation);
            }
        }.init(imgLocation, iPosition[2], iRotation, model)).start();
    }

    @Override
    public void deletePicture(PictureObject pointed_picture){
        new Thread(new Runnable() {
            private Model model;
            private PictureObject po;
            public Runnable init(PictureObject po, Model m) {
                this.po = po;
                this.model = m;
                return this;
            }
            @Override
            public void run() {
                    model.deletePicture(po);
                    model.loadNearBoxes();
            }
        }.init(pointed_picture, model)).start();
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
    public float [] getPictureRotationMatrix(PictureObject po){
        return model.getRotation(po);
    }

    @Override
    public boolean pictureListModified(){
        return model.isPictureListModified();
    }

    @Override
    public float getPixelsPerCentimeterRatio() {
        return model.getPixelPerCentimeterRatio();
    }

    @Override
    public float getPixelsPerCentimeterRatio(PictureObject po) {
        return model.getPixelPerCentimeterRatio(po);
    }

    @Override
    public void setPixelsPerCentimeterRatio(float newRatio) {
        model.setPixelPerCentimeterRatio(newRatio);
    }

    @Override
    public void pictureListUpToDate(){
        model.setPictureListModified(false);
    }

    @Override
    public Model.GPS_MODE getCurrentGPSMode(){
        return model.getCurrentGPSMode();
    };

    @Override
    public void setCurrentGPSMode(Model.GPS_MODE currentMode){
        model.setCurrentGPSMode(currentMode);
    };
}
