package uib.tfg.project.view;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import java.text.DecimalFormat;

import uib.tfg.project.model.Data.PictureObject;
import uib.tfg.project.model.representation.Matrix;
import uib.tfg.project.model.representation.MatrixF4x4;
import uib.tfg.project.model.representation.Quaternion;
import uib.tfg.project.model.representation.Vector3f;
import uib.tfg.project.model.representation.Vector4f;
import uib.tfg.project.presenter.Presenter;

public class VirtualCameraView extends Thread {
    private Context appContext;
    private ImageView virtualView;
    private volatile boolean running = false;
    private volatile boolean finish = false;
    private static volatile int threadNumber = 0;
    private static volatile boolean logs_enabled = false;
    private TextView debuggerText;
    private Presenter presenter;
    private String TAG;
    private static DecimalFormat sensors;
    private static DecimalFormat location;

    private Quaternion currentRotation;
    private Location currentLocation;
    private double currentHeight;
    private Object lock = new Object();
    Quaternion direction_base_vector;
    Quaternion direction_vector;

    public VirtualCameraView(Context cont, View v, View debugger, Presenter p, String TAG){
        appContext = cont;
        if(v != null) virtualView = (ImageView) v;
        if(debugger != null){
            debuggerText = (TextView) debugger;
            debuggerText.setTextColor(Color.RED);
        }
        this.TAG = TAG;
        this.presenter = p;
        sensors = new DecimalFormat("#.##");
        location = new DecimalFormat("##.########");
        direction_base_vector = new Quaternion();
        direction_base_vector.setXYZW(0,0 ,1,0);
        direction_vector = new Quaternion();
    }

    public void updateDirectionVector(){
        currentRotation.normalise();
            direction_vector = currentRotation.rotateQuaternionVector(direction_base_vector);
    }


    @Override
    public void run(){
        running = true;
        Looper.prepare();
        this.setName("VirtualCameraView"+threadNumber);
        threadNumber++;
        startVirtualReality();
    }


    public void startVirtualReality(){
        while(!finish){
            synchronized (lock){
                currentLocation = presenter.getUserLocation();
                currentRotation = presenter.getUserRotation();
                currentHeight = presenter.getUserHeight();
                updateDirectionVector();
            }
            try{
                if(logs_enabled) print_debug_logs();

                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        running = false;
    }

    public void stopVirtualReality(){
        finish = true;
    }

    private void print_debug_logs() {
        String text = "";
        if(currentLocation != null){
            text = "Lat: "+  location.format(currentLocation.getLatitude())
                    + ", Long: " + location.format(currentLocation.getLongitude())+"\n";
        }
        text += "Parsed Direction:";
        text += "X: "+sensors.format(direction_vector.getX())+
                ", Y:"+sensors.format(direction_vector.getY())+
                ", Z:"+sensors.format(direction_vector.getZ())+
                ", W:"+sensors.format(direction_vector.getW())+"\n";
        text += "Quaternion:";
        text += "X: "+sensors.format(currentRotation.getX())+
                ", Y:"+sensors.format(currentRotation.getY())+
                ", Z:"+sensors.format(currentRotation.getZ())+
                ", W:"+sensors.format(currentRotation.getW())+"\n";

        debuggerText.setText(text);
    }

    public boolean isRunning(){
        return running;
    }


    public static void enableLogs(boolean state){
        logs_enabled = state;
    }

    public static boolean debugLogsEnabled(){
        return logs_enabled;
    }

    public PictureObject findPointedPicture() {
        return null;
    }

    public Location findPointedLocation() {
        Location pointed_location = new Location("");
        pointed_location.setLatitude(-1);
        pointed_location.setLongitude(-1);


        return pointed_location;
    }

    public float obtainPictureHeight() {

        return -1;
    }


}
