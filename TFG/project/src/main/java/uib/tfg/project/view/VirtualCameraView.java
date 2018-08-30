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
    private final int X_AXIS = 0;
    private final int Y_AXIS = 1;
    private final int Z_AXIS = 2;
    private static DecimalFormat sensors;
    private static DecimalFormat location;

    public VirtualCameraView(Context cont, View v, View debugger, Presenter p, String TAG){
        appContext = cont;
        if(v != null) virtualView = (ImageView) v;
        if(debugger != null){
            debuggerText = (TextView) debugger;
            debuggerText.setTextColor(Color.RED);
        }
        this.TAG = TAG;
        this.presenter = p;
        sensors = new DecimalFormat("#.####");
        location = new DecimalFormat("##.########");
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
            Location actual = presenter.getUserLocation();
            float [] rotation = presenter.getUserRotation();
            float [] acceleration = presenter.getUserAcceleration();
            try{
                String text = "";
                if(logs_enabled){
                    text = print_debug_logs(actual, rotation, acceleration);
                }
                debuggerText.setText(text);
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private String print_debug_logs(Location actual, float[] rotation, float[] acceleration) {
        String text = "";
        if(actual != null){
            text = "Lat: "+  location.format(actual.getLatitude())
                    + ", Long: " + location.format(actual.getLongitude())+"\n";
        }
        text += "Rot_X: "+ sensors.format(rotation[X_AXIS])
                +", Rot_Y: "+ sensors.format(rotation[Y_AXIS])
                +", Rot_Z: "+sensors.format(rotation[Z_AXIS])+"\n";
        text += "Acc_X: "+ sensors.format(acceleration[X_AXIS])
                +", Acc_Y: "+ sensors.format(acceleration[Y_AXIS])
                +", Acc_Z: "+ sensors.format(acceleration[Z_AXIS]);

        return text;
    }

    public boolean isRunning(){
        return running;
    }

    public void stopVirtualReality(){
        finish = true;
    }

    public static void enableLogs(boolean state){
        logs_enabled = state;
    }

    public static boolean debugLogsEnabled(){
        return logs_enabled;
    }
}
