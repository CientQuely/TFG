package uib.tfg.project.presenter;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import uib.tfg.project.model.Model;

public class LocationService{

    private Context appContext;
    private String TAG;
    private Model model;
    private static volatile int threadNumber = 1;
    private final long INIT_TIME = 100; // in milliseconds
    private final float INIT_DIST = 1; // in meters
    private final int HALF_MINUTE = 1000 * 30;
    private volatile boolean running = false;
    private boolean GPS_ENABLED = false;
    private volatile LocationManager locationManager;
    private volatile boolean finish = false;
    Object lock = new Object();
    private volatile LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            if(isBetterLocation(location, model.getUserCurrentLocation())){
                model.setUserCurrentLocation(location);
                Log.d(TAG, "Actual location is Latitude: "+location.getLatitude()+
                        " , and Longitude: "+location.getLongitude()+ ".");
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                if (status == LocationProvider.OUT_OF_SERVICE) {
                    GPS_ENABLED = false;
                } else {
                    GPS_ENABLED = true;
                }
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            if(provider.equals(LocationManager.GPS_PROVIDER)){
                GPS_ENABLED = true;
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            if(provider.equals(LocationManager.GPS_PROVIDER)){
                GPS_ENABLED = false;
            }
        }

    };

    public LocationService(Context appContext, Model model, String TAG) {
        this.appContext = appContext;
        this.model = model;
        this.TAG = TAG;
    }

    private boolean isBetterLocation(Location location, Location currentBestLocation){
        if(currentBestLocation == null) {
            return true;
        }

        long time = location.getTime() - currentBestLocation.getTime();
        //El gps lleva mucho tiempo sin actualizarse
        boolean isNewer  = time > 0;
        if (time > HALF_MINUTE) return true;
        //Nuevas coordenadas obtenidas muy antiguas
        if (time < -HALF_MINUTE) return false;

        //Controlamos la exactitud de los datos
        float accuracy = location.getAccuracy() - currentBestLocation.getAccuracy();
        boolean isLessAccurate = accuracy > 0;
        boolean isMoreAccurate = accuracy < 0;
        boolean isSignificantlyLessAccurate = accuracy > 10;

        boolean isFromSameProvider = isSameProvider(location.getProvider()
                ,currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    private void initiateGPSListener() {
        locationManager = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);

        //Create accuracy criteria
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAltitudeRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setBearingRequired(false);

        //API level 9 and up
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

        if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if(model.getUserCurrentLocation() == null){
                String bestProvider = locationManager.getBestProvider(criteria,false);
                model.setUserCurrentLocation(locationManager.getLastKnownLocation(bestProvider));
            }
            locationManager.requestLocationUpdates(INIT_TIME, INIT_DIST, criteria, locationListener, null);
        }

    }

    public boolean isRunning() {
        return running;
    }

    public void start(){
        synchronized (lock){
            if(!running){
                running = true;
                initiateGPSListener();
            }
        }
    }


    public void stopLocationService() {
        synchronized (lock){
            if(running){
                locationManager.removeUpdates(locationListener);
                running = false;
            }
        }

    }
}
