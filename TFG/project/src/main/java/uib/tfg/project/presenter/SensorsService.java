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

public class SensorsService extends Thread {
    private Context appContext;
    private String TAG;
    private Model model;
    private final long INIT_TIME = 100; // in milliseconds
    private static volatile boolean running;
    private SensorManager sensorsManager;
    private Sensor gyroscope;
    private Sensor accelerometer;
    private SensorEventListener gyroscopeListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            model.setUserRotation(event.values);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    private SensorEventListener accelerometerListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            model.setUserAcceleration(event.values);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public SensorsService(Context appContext, Model model, String TAG) {
        this.appContext = appContext;
        this.model = model;
        this.TAG = TAG;
        running = false;
    }

    public boolean isRunning(){
        return running;
    }


    private void initiateSensorsListener() {
        sensorsManager = (SensorManager) appContext.getSystemService(Context.SENSOR_SERVICE);
        gyroscope = sensorsManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelerometer = sensorsManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.BODY_SENSORS)
                == PackageManager.PERMISSION_GRANTED) {
            sensorsManager.registerListener(gyroscopeListener, gyroscope,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_STATUS_ACCURACY_HIGH );
            sensorsManager.registerListener(accelerometerListener, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_STATUS_ACCURACY_HIGH  );
        }else{
            Log.e(TAG,"LocationService don't have permits to obtain GPS data");
        }

    }

    @Override
    public void run(){
        Looper.prepare();
        running = true;
        initiateSensorsListener();
        Looper.loop();
    }

    public void stopSensorsService() {
        sensorsManager.unregisterListener(gyroscopeListener);
        sensorsManager.unregisterListener(accelerometerListener);
        running = false;
        this.interrupt();
    }

}
