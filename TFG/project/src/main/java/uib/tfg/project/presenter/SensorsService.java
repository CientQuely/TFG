package uib.tfg.project.presenter;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import uib.tfg.project.model.Model;

@Deprecated
public class SensorsService {
    private Context appContext;
    private String TAG;
    private Model model;
    private final long INIT_TIME = 100; // in milliseconds
    private SensorManager sensorsManager;
    private volatile boolean running = false;
    private Sensor gyroscope;
    private Sensor accelerometer;
    private Sensor magnetic;

    private float[] mGyroscope;
    private float[] mGravity;
    private float[] mGeomagnetic;

    private SensorEventListener gyroscopeListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE)
                mGravity = event.values;

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                mGyroscope = event.values;

            if (mGyroscope != null && mGeomagnetic != null) {
                float R[] = new float[9];
                float I[] = new float[9];

                boolean success = SensorManager.getRotationMatrix(R, I, mGyroscope, mGeomagnetic);
                if (success) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    //model.setUserRotation(orientation);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    private SensorEventListener rotationListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                mGravity = event.values;

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                mGeomagnetic = event.values;

            if (mGravity != null && mGeomagnetic != null) {
                float R[] = new float[9];
                float I[] = new float[9];

                boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                if (success) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    //model.setUserAcceleration(orientation);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public SensorsService(Context appContext, Model model, String TAG) {
        this.appContext = appContext;
        this.model = model;
        this.TAG = TAG;
    }


    private void initiateSensorsListener() {
        sensorsManager = (SensorManager) appContext.getSystemService(Context.SENSOR_SERVICE);
        gyroscope = sensorsManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelerometer = sensorsManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetic =sensorsManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.BODY_SENSORS)
                == PackageManager.PERMISSION_GRANTED) {
            sensorsManager.registerListener(gyroscopeListener, gyroscope,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_STATUS_ACCURACY_HIGH );
            sensorsManager.registerListener(gyroscopeListener, magnetic,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_STATUS_ACCURACY_HIGH  );
            sensorsManager.registerListener(rotationListener, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_STATUS_ACCURACY_HIGH  );
            sensorsManager.registerListener(rotationListener, magnetic,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_STATUS_ACCURACY_HIGH  );
        }else{
            Log.e(TAG,"LocationService don't have permits to obtain GPS data");
        }

    }

    public boolean isRunning() {
        return running;
    }

    public void start(){
        if(!running){
            running = true;
            initiateSensorsListener();
        }
    }

    public void stop() {
        if(running){
            sensorsManager.unregisterListener(gyroscopeListener);
            sensorsManager.unregisterListener(rotationListener);
            running = false;
        }
    }

}
