package uib.tfg.project.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.text.InputType;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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
        }else if(id == R.id.nav_user_height){
            create_input_height_dialog();
        }else if (id == R.id.nav_debug_logs) {
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
                item.setIcon(R.drawable.ic_menu_gps_disabled);
                presenter.stopLocationService();
            }else{
                item.setTitle("GPS Enabled");
                item.setChecked(true);
                item.setIcon(R.drawable.ic_menu_gps_enabled);
                presenter.initiateLocationService();
            }
        } else if (id == R.id.nav_debug_database) {
        } else {
        }
        return true;
    }

    private void create_input_height_dialog() {

        final AlertDialog.Builder dialog_builder = new AlertDialog.Builder(main_activity);
        dialog_builder.setTitle("Insert your height in meters");
        final EditText input = new EditText(main_activity);
        input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        String actual_height = Double.toString(presenter.getUserHeight());
        input.setText(actual_height);
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        input.getBackground().mutate().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_ATOP);
        dialog_builder.setView(input);
        dialog_builder.setIcon(R.drawable.ic_menu_height);

        dialog_builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try{
                    double height = Double.parseDouble(input.getText().toString());
                    if(height >= 0){
                        presenter.setUserHeight(height);
                    }else{
                        Toast.makeText(main_activity,
                                "Insert a positive height",Toast.LENGTH_LONG).show();

                    }
                }catch(Exception e){
                    Toast.makeText(main_activity,"Not valid height introduced" +
                            "\nExample: '1.75' meters",Toast.LENGTH_LONG).show();
                }
            }
        });
        dialog_builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog dialog = dialog_builder.create();
        dialog.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
            }
        });
        dialog.show();
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
