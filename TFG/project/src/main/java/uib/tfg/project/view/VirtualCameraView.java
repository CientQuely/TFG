package uib.tfg.project.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.location.Location;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Looper;
import android.widget.TextView;
import android.view.View;

import java.lang.Math;
import java.text.DecimalFormat;
import java.util.ArrayList;

import uib.tfg.project.model.Data.PictureObject;
import uib.tfg.project.model.representation.MatrixF4x4;
import uib.tfg.project.model.representation.Quaternion;
import uib.tfg.project.presenter.Presenter;

public class VirtualCameraView {
    private Presenter presenter;


    private GLSurfaceView virtualView;
    private GLRenderer myGLRenderer;

    private static boolean ROLL_ENABLED = false;

    public VirtualCameraView(View v, Presenter p){
        this.presenter = p;
        if(v != null) virtualView = (GLSurfaceView) v;

        virtualView.setZOrderOnTop(true);
        virtualView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        virtualView.setEGLContextClientVersion(2);
        myGLRenderer = new GLRenderer(presenter, this);
        virtualView.setRenderer(myGLRenderer);
        virtualView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        virtualView.getHolder().setFormat(PixelFormat.RGBA_8888);
    }

    public static double toDegrees(double  angle){
        return angle * ( 180.0 / Math.PI);
    }

    public double [] getPointedLocation(double distanceInMeters) {
        double [] userLocation = presenter.getUserLocationInMeters();
        double [] imageRelativeLocation = getImageRelativeLocationLatLongHeight(distanceInMeters);

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

    public static double [] getImageRelativeLocationLatLongHeight(Presenter p, double distanceInMeters) {
        double [] relativeLocation = new double [3];
        double [] vector = {0, 0, -distanceInMeters};
        relativeLocation[2] = p.getUserRotation().rotateVector(vector)[2]; // HEIGHT OR Y
        vector[2]=0;
        vector[0]=-distanceInMeters;
        relativeLocation[1] = p.getUserRotation().rotateVector(vector)[2]; // LATITUDE OR Z
        vector[0]=0;
        vector[1]=-distanceInMeters;
        relativeLocation[0] = p.getUserRotation().rotateVector(vector)[2]; // LONGITUDE OR X

        return relativeLocation;
    }

    public double [] getImageRelativeLocationLatLongHeight(double distanceInMeters) {
        return getImageRelativeLocationLatLongHeight(presenter, distanceInMeters);
    }

    public float [] getImageRotation(){
        double [] dv = getImageRelativeLocationLatLongHeight(1);
        return GLRenderer.getImageRotationMatrix((float)dv[1],  (float)dv[2], (float)dv[0]);
    }

    public void onPause(){
        virtualView.onPause();
    }

    public void onResume(){
        virtualView.onResume();
    }

    public PictureObject getNearestImageInRange(double range) {
       double [] user_location = presenter.getUserLocationInMeters();
       ArrayList<PictureObject> pictures = presenter.getNearestImages();

       int min_dist_id = -1;
       double min_dist = Double.MAX_VALUE;
       double distance;
        for (int i = 0; i < pictures.size(); i++) {
            double [] imagePosition = presenter.getPicturePosition(pictures.get(i));
            distance = euclideanDistance(user_location, imagePosition);

            if (distance < min_dist && distance < range){
                min_dist_id = i;
                min_dist = distance;
            }
        }

        if(min_dist_id != -1){
            return pictures.get(min_dist_id);
        }
        return null;
    }

    public static double euclideanDistance(double [] a, double [] b) {
        return Math.sqrt(Math.pow(a[0] - b[0], 2) + Math.pow(a[1] - b[1], 2) + Math.pow(a[2] - b[2], 2));
    }


    public static boolean isRollEnabled() {
        return ROLL_ENABLED;
    }

    public static void setRollEnabled(boolean rollEnabled) {
        ROLL_ENABLED = rollEnabled;
    }

}
