package uib.tfg.project.view;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import uib.tfg.project.presenter.Presenter;

public class VirtualCameraView extends Thread {
    private Context appContext;
    private ImageView virtualView;
    private volatile boolean running = false;
    private TextView debuggerText;
    private Presenter presenter;
    private String TAG;
    private final int X_AXIS = 0;
    private final int Y_AXIS = 1;
    private final int Z_AXIS = 2;

    public VirtualCameraView(Context cont, View v, View debugger, Presenter p, String TAG){
        appContext = cont;
        if(v != null) virtualView = (ImageView) v;
        if(debugger != null){
            debuggerText = (TextView) debugger;
            debuggerText.setTextColor(Color.RED);
        }
        this.TAG = TAG;
        this.presenter = p;
    }

    @Override
    public void run(){
        running = true;
        Looper.prepare();
        startVirtualReality();
    }


    public void startVirtualReality(){
        while(!interrupted()){
            Location actual = presenter.getUserLocation();
            float [] rotation = presenter.getUserRotation();
            float [] acceleration = presenter.getUserAcceleration();
            try{
                if(actual != null){
                    String text = "Lat: "+  actual.getLatitude() + ", Long: " + actual.getLongitude()+"\n";
                    text += "Rot_X: "+ rotation[X_AXIS]+", Rot_Y: "+ rotation[Y_AXIS]+", Rot_Z: "+rotation[Z_AXIS]+"\n";
                    text += "Acc_X: "+ acceleration[X_AXIS]+", Acc_Y: "+ acceleration[Y_AXIS]+", Acc_Z: "+acceleration[Z_AXIS];
                    debuggerText.setText(text);
                }
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopVirtualReality(){
        interrupt();
    }
}
