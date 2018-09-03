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
    private Vector4f forwardVector;

    private Object lock = new Object();

    public VirtualCameraView(Context cont, View v, View debugger, Presenter p, String TAG){
        appContext = cont;
        if(v != null) virtualView = (ImageView) v;
        if(debugger != null){
            debuggerText = (TextView) debugger;
            debuggerText.setTextColor(Color.RED);
        }
        this.TAG = TAG;
        this.presenter = p;
        forwardVector = new Vector4f();
        sensors = new DecimalFormat("#.##");
        location = new DecimalFormat("##.########");
    }

    public void updateForwardVector(){
        synchronized (lock){
            currentRotation.normalise();
            currentRotation.toAxisAngle(forwardVector);
        }
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
                Location actual = presenter.getUserLocation();
                currentRotation = presenter.getUserRotation();
                currentHeight = presenter.getUserHeight();
                updateForwardVector();
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
        text += "X: "+sensors.format(forwardVector.getX())+
                ", Y:"+sensors.format(forwardVector.getY())+
                ", Z:"+sensors.format(forwardVector.getZ());

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
