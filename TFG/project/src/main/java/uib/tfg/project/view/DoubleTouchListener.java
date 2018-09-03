package uib.tfg.project.view;

import android.view.MotionEvent;

public abstract class DoubleTouchListener implements android.view.View.OnTouchListener {

    private static final long DOUBLE_CLICK_TIME_DELTA = 300;//milliseconds
    long lastClickTime = 0;

    public abstract void onDoubleTouch(float x, float y);

    @Override
    public boolean onTouch(android.view.View v, MotionEvent event) {
        long clickTime = System.currentTimeMillis();
        if(event.getAction() == MotionEvent.ACTION_UP){
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA){
                onDoubleTouch(event.getX(), event.getY());
            }
            lastClickTime = clickTime;
        }
        return true;
    }
}