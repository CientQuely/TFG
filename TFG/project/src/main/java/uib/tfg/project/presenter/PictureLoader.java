package uib.tfg.project.presenter;

import android.graphics.Point;
import android.os.Looper;
import android.util.Log;

import java.util.Observable;
import java.util.Observer;

import uib.tfg.project.model.Model;
import uib.tfg.project.model.ModelException;

public class PictureLoader extends Thread implements Observer {

    private Model model;
    private volatile static boolean running = false;
    private String TAG = "Presenter/PictureLoader";
    private static boolean DB_LOADED;
    private static final int BOXES_RANGE = 1;
    private static final float MIN_TIME = 3000; //in milliseconds
    private int last_update =

    PictureLoader(Model m){
        this.model = m;
        DB_LOADED = false;
    }

    @Override
    public void run(){
        Looper.prepare();
        try {
            model.loadDataBase();
            DB_LOADED = true;
            load_near_boxes();
        } catch (ModelException.DB_Config_Exception e) {
            Log.e(TAG,"DB Could not be created/loaded");
            e.printStackTrace();
        } catch (ModelException.DB_File_Exception e) {
            Log.e(TAG,"DB Config file could not be created/loaded");
            e.printStackTrace();
        }

        Looper.loop();
    }



    private void clearFarBoxes(float old_box , float old_box){
        Point user_box = model.getUserLocationBox();
    }

    public void stopPictureLoader(){
        inte
    }

    @Override
    public void update(Observable o, Object arg) {
        loadNearBoxes();
        clearFarBoxes();
    }
}
