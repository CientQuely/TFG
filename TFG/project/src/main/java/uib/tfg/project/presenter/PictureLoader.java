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
    private static volatile boolean thread_interrupted;

    public PictureLoader(Model model){
        this.model = model;
        DB_LOADED = false;
        thread_interrupted = false;
    }

    @Override
    public void run(){
        Looper.prepare();
        try {
            model.loadDataBase();
            DB_LOADED = true;
            model.loadNearBoxes();
        } catch (ModelException.DB_Config_Exception e) {
            Log.e(TAG,"DB Could not be created/loaded");
            e.printStackTrace();
        } catch (ModelException.DB_File_Exception e) {
            Log.e(TAG,"DB Config file could not be created/loaded");
            e.printStackTrace();
        }

        while (!thread_interrupted){
            try {
                this.wait();
                model.loadNearBoxes();
            } catch (InterruptedException e) {
                this.interrupt();
            }
        }
        thread_interrupted = false;
        Looper.loop();
    }

    public void stopPictureLoader(){
        thread_interrupted = true;
    }

    @Override
    public void update(Observable o, Object arg) {
        synchronized (o){
            notify();
        }
    }
}
