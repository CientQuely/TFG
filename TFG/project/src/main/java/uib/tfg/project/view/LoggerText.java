package uib.tfg.project.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import uib.tfg.project.model.Data.PictureObject;
import uib.tfg.project.model.representation.Quaternion;
import uib.tfg.project.presenter.Presenter;

public class LoggerText extends Thread{

    private Presenter presenter;

    private TextView debuggerText;
    private static DecimalFormat sensors;
    private static DecimalFormat location;

    private Object lock = new Object();
    private Quaternion currentRotation;
    private Location currentLocation;
    private ArrayList<PictureObject> pictureList;

    private static volatile boolean logs_enabled = false;
    private volatile boolean finish = false;
    private volatile boolean running = false;
    private int threadNumber;
    private Activity main_activity;

    public LoggerText(Activity c, Presenter presenter, View debugger) {
        this.main_activity = c;
        this.presenter = presenter;
        initiateDebuggerText(debugger);
    }

    private void  initiateDebuggerText(View debugger){
        if(debugger != null){
            debuggerText = (TextView) debugger;
            debuggerText.setTextColor(Color.RED);
        }

        sensors = new DecimalFormat("#.##");
        location = new DecimalFormat("##.########");
    }


    public void startLoggerService(){
        while(!finish){
            synchronized (lock){
                //Change camera view direction
                currentRotation = presenter.getUserRotation();
                currentLocation = presenter.getUserLocation();
                pictureList = presenter.getNearestImages();
            }
            try{
                print_debug_logs();

                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        running = false;
    }

    @Override
    public void run(){
        running = true;
        Looper.prepare();
        this.setName("LoggerText"+threadNumber);
        threadNumber++;
        startLoggerService();
    }


    public void stopLoggerText(){
        finish = true;
    }

    private void print_debug_logs() {
        String text = "";
        if (! logs_enabled){
            debuggerText.setText(text);
            return;
        }

        if (currentLocation != null) {
            text = "Lat: " + location.format(currentLocation.getLatitude())
                    + ", Long: " + location.format(currentLocation.getLongitude()) + "\n";
        }

        double[] angle = currentRotation.toEuler();

        text += "Angle:" +
                ", pitch:" + sensors.format(toDegrees(angle[0])) +
                ", yaw:" + sensors.format(toDegrees(angle[1])) +
                ", roll:" + sensors.format(toDegrees(angle[2]));

        double[] rl = VirtualCameraView.getImageRelativeLocation(presenter, 1);
        text += "\nRL= " +
                " Alt: " + sensors.format(rl[2]) +
                " ,lat: " + sensors.format(rl[0]) +
                " ,long: " + sensors.format(rl[1]);

        text += "\n   PICTURES \n Number: " + pictureList.size();

        debuggerText.setText(text);
    }

    public double toDegrees (double value){
            return VirtualCameraView.toDegrees(value);
    }


    public static void enableLogs(boolean state){
        logs_enabled = state;
    }

    public static boolean debugLogsEnabled(){
        return logs_enabled;
    }

    public boolean isRunning(){
            return running;
    }
}

