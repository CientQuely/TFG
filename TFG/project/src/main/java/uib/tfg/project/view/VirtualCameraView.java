package uib.tfg.project.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.location.Location;
import android.opengl.GLSurfaceView;
import android.os.Looper;
import android.widget.TextView;
import android.view.View;

import java.lang.Math;
import java.text.DecimalFormat;
import java.util.ArrayList;

import uib.tfg.project.model.Data.PictureObject;
import uib.tfg.project.model.representation.Quaternion;
import uib.tfg.project.presenter.Presenter;

public class VirtualCameraView {
    private Context appContext;
    private static volatile boolean logs_enabled = false;
    private Presenter presenter;


    private GLSurfaceView virtualView;
    private GLRenderer myGLRenderer;

    public VirtualCameraView(Context cont, View v, Presenter p, String TAG){
        appContext = cont;
        this.presenter = p;
        if(v != null) virtualView = (GLSurfaceView) v;

        virtualView.setZOrderOnTop(true);
        virtualView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        virtualView.getHolder().setFormat(PixelFormat.RGBA_8888);

        virtualView.setEGLContextClientVersion(2);
        myGLRenderer = new GLRenderer(presenter, this);
        virtualView.setRenderer(myGLRenderer);
        virtualView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    public static double toDegrees(double  angle){
        return angle * ( 180.0 / Math.PI);
    }

    public PictureObject findPointedPicture() {
        return null;
    }

    public double [] getPointedLocation(double distanceInMeters) {
        double [] userLocation = presenter.getUserLocationInMeters();
        double [] imageRelativeLocation = getImageRelativeLocation(distanceInMeters);

        int i = 0;
        double [] imageLocation = new double [userLocation.length];
        while (i < userLocation.length){
            imageLocation[i] = userLocation[i] + imageRelativeLocation[i];
            i++;
        }
        return imageLocation;
    }

    public void hideVirtualCameraView(){
        GLRenderer.setStopDrawing(true);
    }

    public void unhideVirtualCameraView(){

        GLRenderer.setStopDrawing(false);
    }

    public static double [] getImageRelativeLocation(Presenter p, double distanceInMeters) {
        double [] eulerAngle = p.getUserRotation().toEuler();

        double [] relativeLocation = new double [3];

        double altitude = Math.abs(distanceInMeters * Math.cos(eulerAngle[0]));
        if (eulerAngle[0] > (- Math.PI / 2) && eulerAngle[0] < (Math.PI / 2)) {
            altitude = - altitude;
        }
        relativeLocation[2] = altitude;

        double latAndLongDistance = Math.abs(distanceInMeters * Math.sin(eulerAngle[0]));

        double latitude = latAndLongDistance * Math.cos(eulerAngle[1]);
        if (eulerAngle[0] < 0) { latitude = -latitude; }
        relativeLocation[0] = latitude;

        double longitude = latAndLongDistance * Math.sin(eulerAngle[1]);
        if (eulerAngle[0] > 0) { longitude = -longitude; }
        relativeLocation[1] = longitude;

        return relativeLocation;
    }

    public double [] getImageRelativeLocation(double distanceInMeters) {
        double [] eulerAngle = presenter.getUserRotation().toEuler();

        double [] relativeLocation = new double [3];

        double altitude = Math.abs(distanceInMeters * Math.cos(eulerAngle[0]));
        if (eulerAngle[0] > (- Math.PI / 2) && eulerAngle[0] < (Math.PI / 2)) {
            altitude = - altitude;
        }
        relativeLocation[2] = altitude;

        double latAndLongDistance = Math.abs(distanceInMeters * Math.sin(eulerAngle[0]));

        double latitude = latAndLongDistance * Math.cos(eulerAngle[1]);
        if (eulerAngle[0] < 0) { latitude = -latitude; }
        relativeLocation[0] = latitude;

        double longitude = latAndLongDistance * Math.sin(eulerAngle[1]);
        if (eulerAngle[0] > 0) { longitude = -longitude; }
        relativeLocation[1] = longitude;

        return relativeLocation;
    }

    public double [] getImageRotation(){
        double [] userRotation = presenter.getUserRotation().toEuler();

        for (int i = 0; i < userRotation.length; i++){
            userRotation[i] = - userRotation[i];
        }
        return userRotation;
    }

    public void onPause(){
        virtualView.onPause();
    }

    public void onResume(){
        virtualView.onResume();
    }
}
