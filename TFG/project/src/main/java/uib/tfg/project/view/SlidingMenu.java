package uib.tfg.project.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.lang.reflect.Field;

import uib.tfg.project.R;
import uib.tfg.project.presenter.Presenter;

public class SlidingMenu implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationView menuView;
    private Context appContext;
    private Activity main_activity;
    private Presenter presenter;
    private Toast menu_toast;
    private final static int SLIDE_MULTIPLIER = 6;
    public static final int PICK_IMAGE = 1;

    public SlidingMenu(Context c, View v, Presenter p) throws NoSuchFieldException, IllegalAccessException {
        appContext = c;
        main_activity = (Activity)c;
        menuView = (NavigationView) v;
        presenter = p;
        menuView.setNavigationItemSelectedListener(this);
        Display display = main_activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        menu_toast = Toast.makeText(appContext, "", Toast.LENGTH_SHORT);
        menu_toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL,width/4+20, 0);

        changeScrollDetectorSize(SLIDE_MULTIPLIER);
    }

    private void changeScrollDetectorSize(int multiplier) throws NoSuchFieldException, IllegalAccessException{
        DrawerLayout mDrawerLayout = main_activity.findViewById(R.id.drawer_layout);
        Field mDragger = mDrawerLayout.getClass().getDeclaredField(
                "mLeftDragger");
        mDragger.setAccessible(true);
        ViewDragHelper draggerObj = (ViewDragHelper) mDragger
                .get(mDrawerLayout);

        Field mEdgeSize = draggerObj.getClass().getDeclaredField(
                "mEdgeSize");
        mEdgeSize.setAccessible(true);
        int edge = mEdgeSize.getInt(draggerObj);

        mEdgeSize.setInt(draggerObj, edge * multiplier);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_select_image) {
            IntentToGallery();
        } else if (id == R.id.nav_debug_logs) {
            if(VirtualCameraView.debugLogsEnabled()){
                item.setTitle("Logs Disabled");
                item.setChecked(false);
                VirtualCameraView.enableLogs(false);
            }else{
                item.setTitle("Logs Enabled");
                item.setChecked(true);
                VirtualCameraView.enableLogs(true);
            }
        } else if (id == R.id.nav_debug_gps) {
            if(presenter.isLocationServiceEnabled()){
                item.setTitle("GPS Disabled");
                item.setChecked(false);
                presenter.stopLocationService();
            }else{
                item.setTitle("GPS Enabled");
                item.setChecked(true);
                presenter.initiateLocationService();
            }
        } else if (id == R.id.nav_debug_database) {
        } else {
        }
        return true;
    }

    private void IntentToGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        main_activity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    public void showMenuMessage(String text) {
        menu_toast.setText(text);
        menu_toast.show();
    }
}
