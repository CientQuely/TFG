package uib.tfg.project.presenter;

import android.graphics.Point;
import android.os.Looper;
import android.util.Log;

import java.util.Observable;
import java.util.Observer;

import uib.tfg.project.model.Model;

public class PictureLoader extends Thread implements Observer {

    private Model model;
    private volatile boolean running = false;
    private static volatile int threadNumber = 1;
    private String TAG = "Presenter/PictureLoader";
    private static final int BOXES_RANGE = 1;
    private static final long MIN_TIME = 1000; //in milliseconds
    private static Object lock = new Object();
    private volatile boolean thread_interrupted = false;

    public PictureLoader(Model model){
        this.model = model;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void run(){
        Log.i(TAG,"Picture Loader Initiated");
        Looper.prepare();
        this.setName("PictureLoader"+threadNumber);
        threadNumber++;
        model.setUserObserver(this);
        running = true;

        while (!thread_interrupted){
            Log.i(TAG,"Initiating loading of picture box");
                Point box_loaded = model.loadNearBoxes();
                model.removeFarCacheImagesBitmap();
                Log.i(TAG,"User box ["
                        + box_loaded.x + "," + box_loaded.y +"] bitmaps loaded correctly");
            try {
                Thread.sleep(MIN_TIME);
                synchronized (lock){
                    lock.wait();
                }
            } catch (Exception e) {
                Log.e("PictureLoader","Error", e);
            }
        }
        Log.i(TAG,"Picture Loader Finished");
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
