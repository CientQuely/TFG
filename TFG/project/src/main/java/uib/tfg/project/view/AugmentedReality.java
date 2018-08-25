package uib.tfg.project.view;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;
import java.util.concurrent.Semaphore;
import android.util.Log;

import uib.tfg.project.ProjectView;
import uib.tfg.project.R;
import uib.tfg.project.presenter.Presenter;
import uib.tfg.project.presenter.ProjectPresenter;


public class AugmentedReality extends Activity implements View{

    private Presenter presenter;
    private CameraView cameraStream;
    private VirtualCameraView virtualStream;
    private final Semaphore mutex = new Semaphore(0);
    private static final String TAG = "View/AugmentedReality";


    public AugmentedReality(){
        super();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bitmap not_foung_img = BitmapFactory.decodeResource(this.getResources(), R.drawable.not_found_img);
        presenter = new ProjectPresenter(this, this, not_foung_img);
        //Se encarga de obtener todos los permisos
        if(!hasFeatures()){
            Toast.makeText(this, "Tu movil no posee todas las características para" +
                            "poder ejecutar esta app.",
                    Toast.LENGTH_LONG).show();
            goToMenu();
            return;
        }
        setContentView(R.layout.activity_augmented_reality);

        //Crea la vista de la camara
        cameraStream = new CameraView(this, this.findViewById(R.id.camera_view), "View/Camera/CameraView");
        virtualStream = new VirtualCameraView(this, this.findViewById(R.id.virtual_view),
                this.findViewById(R.id.debbugerText),presenter, "View/VirtualCameraView");

    }

    @Override
    public void onStart(){
        super.onStart();
        if(!Permits.hasAllPermits(this)) {
            getRemainingPermits();
            Log.i(TAG,"Waiting permission response...");
            return;
        }
    }
    private boolean hasFeatures() {
        PackageManager pm = this.getPackageManager();
        boolean hasAllFeatures = true;
        if(!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            Log.e("TAG","This device don't have Camera");
            hasAllFeatures = false;
        }

        if(!pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)){
            Log.e("TAG","This device don't have GPS system.");
            hasAllFeatures = false;
        }

        if(!pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE)){
            Log.e("TAG","This device don't have Gyroscope sensor.");
            hasAllFeatures = false;
        }

        if(!pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS)){
            Log.e("TAG","This device don't have Compass sensor.");
            hasAllFeatures = false;
        }

        if(!pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)){
            Log.e("TAG","This device don't have Accelerometer sensor.");
            hasAllFeatures = false;
        }

        Log.i(TAG, "Device has all features used by this app");
        return hasAllFeatures;
    }

    private void getRemainingPermits() {
        String [] features_to_request = Permits.getFeaturesToRequest();
        if(features_to_request != null) {
            ActivityCompat.requestPermissions(this, features_to_request, Permits.ALL_PERMITS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults){
        if(Permits.ALL_PERMITS_REQUEST == requestCode){
            Permits.updatePermits(permissions, grantResults);
        }

        if(Permits.hasAllPermits(this)){
            Toast.makeText(this, "Todos los permisos obtenidos !!",
                    Toast.LENGTH_LONG).show();

            //si es la primera vez que llamamos a la camara y aun no teniamos permisos
            if(!cameraStream.cameraAvailable()){
                cameraStream.start();
            }

        }else{
                Toast.makeText(this, "Esta app no puede funcionar si no se aceptan" +
                                "todos los permisos.",
                        Toast.LENGTH_LONG).show();
            goToMenu();
        }
        return;
    }

    private void goToMenu() {
        //vuelve
        Intent pv = new Intent(this, ProjectView.class);
        startActivity(pv);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(Permits.CAMERA_PERMIT && !cameraStream.cameraAvailable()){
            cameraStream.start();
        }
        if(Permits.GPS_PERMIT){
           presenter.initiateLocationService();
        }
        if(Permits.SENSORS_PERMIT){
            presenter.initiateSensorsService();
        }
        if(Permits.STORAGE_PERMIT){
            presenter.initiatePictureLoader();
        }
        if(Permits.hasAllPermits(this)){
            virtualStream.start();
        }
    }

    protected void onPause() {
        super.onPause();
        presenter.stopLocationService();
        presenter.stopSensorsService();
        presenter.stopPictureLoader();
        presenter.storeDataBase();
    }
}
