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
    private volatile boolean running = false;
    private static volatile int threadNumber = 1;
    private String TAG = "Presenter/PictureLoader";
    private static boolean DB_LOADED;
    private static final int BOXES_RANGE = 1;
    private static final float MIN_TIME = 3000; //in milliseconds
    private static Object lock = new Object();
    private volatile boolean thread_interrupted = false;

    public PictureLoader(Model model){
        this.model = model;
        DB_LOADED = false;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void run(){
        Looper.prepare();
        this.setName("PictureLoader"+threadNumber);
        threadNumber++;
        model.setUserObserver(this);
        running = true;
        try {
            model.loadDataBase();
            Log.i(TAG,"DB Loaded Correctly");
            DB_LOADED = true;
            Point box_loaded = model.loadNearBoxes();
            Log.i(TAG,"User box ["
                    + box_loaded.x + "," + box_loaded.y +"] bitmaps loaded correctly");
        } catch (ModelException.DB_Config_Exception e) {
            Log.e(TAG,"DB Could not be created/loaded");
            e.printStackTrace();
        } catch (ModelException.DB_File_Exception e) {
            Log.e(TAG,"DB Config file could not be created/loaded");
            e.printStackTrace();
        }

        while (!thread_interrupted){
            try {
                synchronized (lock){
                    lock.wait();
                }
                Point box_loaded = model.loadNearBoxes();
                Log.i(TAG,"User box ["
                        + box_loaded.x + "," + box_loaded.y +"] bitmaps loaded correctly");
            } catch (InterruptedException e) {
                e.getStackTrace();
            }
        }
    }

    public void stopPictureLoader(){
        this.thread_interrupted = true;
        model.removeUserObserver(this);
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        synchronized (lock){
            lock.notifyAll();
        }
    }
}
