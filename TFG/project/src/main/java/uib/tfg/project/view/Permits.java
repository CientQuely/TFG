package uib.tfg.project.view;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import java.util.ArrayList;

public class Permits {
        static public String TAG = "Aplication-Permits";
        static public boolean CAMERA_PERMIT = false;
        static public boolean GPS_PERMIT = false;
        static public boolean STORAGE_PERMIT = false;
        static public boolean SENSORS_PERMIT = false;

        static public final int ALL_PERMITS_REQUEST = 1;

        static public final String CAMERA_MANIFEST = Manifest.permission.CAMERA;
        static public final String GPS_MANIFEST = Manifest.permission.ACCESS_FINE_LOCATION;
        static public final String SENSORS_MANIFEST = Manifest.permission.BODY_SENSORS;
        static public final String STORAGE_MANIFEST = Manifest.permission.WRITE_EXTERNAL_STORAGE;


        public static boolean hasCameraPermission(Context cont){
            return ActivityCompat.checkSelfPermission(cont, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED;
        }

        public static boolean hasGPSPermission(Context cont){
            return ContextCompat.checkSelfPermission(cont, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED;
        }

        public static boolean hasSensorsPermission(Context cont){
            return ContextCompat.checkSelfPermission(cont, Manifest.permission.BODY_SENSORS)
                    == PackageManager.PERMISSION_GRANTED;
        }


        public static boolean hasStoragePermission(Context cont){
            return ContextCompat.checkSelfPermission(cont, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }

        public static boolean hasAllPermits(Context cont) {
            boolean AllPermissions = true;
            //CAMERA PERMISSION
            if(!CAMERA_PERMIT ) {
                if(hasCameraPermission(cont)){
                    Permits.CAMERA_PERMIT = true;
                }else{
                    AllPermissions = false;
                }
            }
            //GPS PERMISSION
            if(!GPS_PERMIT) {
                if(hasGPSPermission(cont)){
                    Permits.GPS_PERMIT = true;
                }else{
                    AllPermissions = false;
                }
            }
            //SENSORS PERMISSION
            if(!SENSORS_PERMIT) {
                if(hasSensorsPermission(cont)){
                    Permits.SENSORS_PERMIT = true;
                }else{
                    AllPermissions = false;
                }
            }
            //STORAGE PERMISSION
            if(!STORAGE_PERMIT) {
                if(hasStoragePermission(cont)){
                    Permits.STORAGE_PERMIT = true;
                }else{
                    AllPermissions = false;
                }
            }
            if(AllPermissions) Log.i(TAG, "Device has all permissions");
            return AllPermissions;
        }

        public static String[] getFeaturesToRequest(){
            ArrayList<String> wplist = new ArrayList<String>();
            int size = 0;
            if(!CAMERA_PERMIT){ wplist.add(CAMERA_MANIFEST); size++; }
            if(!GPS_PERMIT){ wplist.add(GPS_MANIFEST); size++;}
            if(!SENSORS_PERMIT){ wplist.add(SENSORS_MANIFEST); size ++;}
            if(!STORAGE_PERMIT){ wplist.add(STORAGE_MANIFEST); size++;}
            if(size > 0){ return wplist.toArray(new String[size]); }
            return null;
        }

        public static void updatePermits(String[] permissions, int[] grantResults) {

            for (int actual_permit = 0; permissions.length > actual_permit; actual_permit++){
                if(grantResults[actual_permit] == PackageManager.PERMISSION_GRANTED){
                    switch (permissions[actual_permit]){
                        case Permits.CAMERA_MANIFEST:
                            CAMERA_PERMIT = true;
                            break;
                        case Permits.GPS_MANIFEST:
                            GPS_PERMIT = true;
                            break;
                        case Permits.SENSORS_MANIFEST:
                            SENSORS_PERMIT = true;
                            break;
                        case Permits.STORAGE_MANIFEST:
                            STORAGE_PERMIT = true;
                            break;
                        default:
                            Log.e(TAG,"Permit not found in updatePermits method.");
                    }
                }
            }
        }
    }

