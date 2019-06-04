package uib.tfg.project;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import uib.tfg.project.presenter.Presenter;
import uib.tfg.project.view.AugmentedReality;
import uib.tfg.project.view.Permits;
import uib.tfg.project.view.View;

public class ProjectView extends Activity {

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.menu);


        if(!hasFeatures()){
            Toast.makeText(this, "Your phone does not have all the features to use this app",
                    Toast.LENGTH_LONG).show();

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

        Log.i("Main Menu", "Device has all features used by this app");
        return hasAllFeatures;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults){
        if(Permits.ALL_PERMITS_REQUEST == requestCode){
            Permits.updatePermits(permissions, grantResults);
        }

        if(Permits.hasAllPermits(this)){
            createIntentAndStartAR();
        }else{
            Toast.makeText(this, "Esta app no puede funcionar si no se aceptan" +
                            "todos los permisos.",
                    Toast.LENGTH_LONG).show();

        }
    }

    public void onStartClick(android.view.View view) {
        if(!Permits.hasAllPermits(this)) {
            getRemainingPermits();
            Log.i("Main Menu","Please accept all permissions");
            return;
        }else{
            createIntentAndStartAR();
        }

    }

    private void createIntentAndStartAR(){
        Intent ar = new Intent(this, AugmentedReality.class);
        startActivity(ar);
    }

    private void getRemainingPermits() {
        String [] features_to_request = Permits.getFeaturesToRequest();
        if(features_to_request != null) {
            ActivityCompat.requestPermissions(this, features_to_request, Permits.ALL_PERMITS_REQUEST);
        }
    }
}
