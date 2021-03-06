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
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import uib.tfg.project.R;
import uib.tfg.project.model.Model;
import uib.tfg.project.presenter.Presenter;

public class SlidingMenu implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationView menuView;
    private Context appContext;
    private Activity main_activity;
    private Presenter presenter;
    private Toast menu_toast;
    private final static int SLIDE_MULTIPLIER = 6;
    public static final int PICK_IMAGE = 1;
    public boolean INSERTION_MODE_ON = true;
    public boolean GPS_BLOCKED = false;
    public static String creationTitle = "Set Creation Distance";
    public static String removalTitle = "Set Removal Distance";


    public SlidingMenu(Context c, View v, Presenter p) throws NoSuchFieldException, IllegalAccessException {
        appContext = c;
        main_activity = (Activity)c;
        menuView = (NavigationView) v;
        menuView.setItemIconTintList(null);
        presenter = p;
        menuView.setNavigationItemSelectedListener(this);
        Display display = main_activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        menu_toast = Toast.makeText(appContext, "", Toast.LENGTH_SHORT);
        menu_toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL,width/4+20, 0);
        changeScrollDetectorSize(SLIDE_MULTIPLIER);

        double truncate_height = Math.floor(presenter.getUserHeight() * 100) / 100;
        menuView.getMenu().findItem(R.id.nav_actual_height)
                .setTitle("     Actual: "+truncate_height+" meters");

        double default_creation_distance = Math.floor(presenter.getImageCreationDistance() * 100) / 100;
        menuView.getMenu().findItem(R.id.nav_actual_distance).setTitle("     Actual: "+default_creation_distance+" meters");

        double default_pixel_ratio = Math.floor(presenter.getPixelsPerCentimeterRatio() * 100) / 100;
        menuView.getMenu().findItem(R.id.nav_actual_pixel_ratio).setTitle("     "+default_pixel_ratio+" pixels per cm");
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
        Log.i("SlidingMenu", "Menu item selected");
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //Switch deforme
        if (id == R.id.nav_select_image) {
            IntentToGallery();
        } else if (id == R.id.nav_user_height) {
            create_input_height_dialog();
        } else if (id == R.id.nav_debug_logs) {
            if (LoggerText.debugLogsEnabled()) {
                item.setTitle("Logs Disabled");
                item.setChecked(false);
                LoggerText.enableLogs(false);
            } else {
                item.setTitle("Logs Enabled");
                item.setChecked(true);
                LoggerText.enableLogs(true);
            }
        } else if (id == R.id.nav_debug_gps) {
            if (presenter.isLocationServiceEnabled()) {
                item.setTitle("GPS Disabled");
                item.setChecked(false);
                item.setIcon(R.drawable.ic_menu_gps_disabled);
                presenter.stopLocationService();
                GPS_BLOCKED = true;
            } else {
                item.setTitle("GPS Enabled");
                item.setChecked(true);
                item.setIcon(R.drawable.ic_menu_gps_enabled);
                presenter.initiateLocationService();
                GPS_BLOCKED = false;
            }
        } else if (id == R.id.nav_debug_database) {
            create_database_alert_dialog();
        } else if (id == R.id.nav_mode) {
            MenuItem model_distanceItem = menuView.getMenu().findItem(R.id.nav_model_distance);
            MenuItem actual_distanceItem = menuView.getMenu().findItem(R.id.nav_actual_distance);
            if (INSERTION_MODE_ON) {
                item.setTitle("REMOVE");
                item.setIcon(R.drawable.ic_menu_remove_mode);
                INSERTION_MODE_ON = false;

                model_distanceItem.setTitle(removalTitle);
                model_distanceItem.setIcon(R.drawable.ic_menu_distance_remove);

                double default_removal_distance = Math.floor(presenter.getImageRemovalDistance() * 100) / 100;
                actual_distanceItem.setTitle("     Actual: " + default_removal_distance + " meters");

                Toast.makeText(main_activity,
                        "Double click to remove nearest image",
                        Toast.LENGTH_LONG).show();
            } else {
                item.setTitle("ADD");
                item.setIcon(R.drawable.ic_menu_add_mode);
                INSERTION_MODE_ON = true;

                model_distanceItem.setTitle(creationTitle);
                model_distanceItem.setIcon(R.drawable.ic_menu_distance_add);

                double default_creation_distance = Math.floor(presenter.getImageCreationDistance() * 100) / 100;
                actual_distanceItem.setTitle("     Actual: " + default_creation_distance + " meters");

                Toast.makeText(main_activity,"Double click to put an image in front of the camera",
                        Toast.LENGTH_LONG).show();
            }
        } else if (id == R.id.nav_model_distance) {
            if (INSERTION_MODE_ON) {
                create_input_distance_dialog("Insert new creation distance in meters");
            } else {
                create_input_distance_dialog("Insert new removal distance in meters");
            }
        } else if (id == R.id.nav_pixel_ratio) {
            create_input_pixel_ratio("Insert new pixels per centimeter ratio");
        } else if (id == R.id.nav_virtual_rotation) {
            if (VirtualCameraView.isRollEnabled()) {
                item.setTitle("Rotation Disabled");
                item.setChecked(false);
                item.setIcon(R.drawable.ic_screen_rotation_closed);
                VirtualCameraView.setRollEnabled(false);
            } else {
                item.setTitle("Rotation Enabled");
                item.setChecked(true);
                item.setIcon(R.drawable.ic_screen_rotation_open);
                VirtualCameraView.setRollEnabled(true);
            }
        } else if (id == R.id.nav_gps_type){
            Model.GPS_MODE mode = presenter.getCurrentGPSMode();
            switch(mode){
                case LAST:
                    item.setTitle("GPS Mode: AVERAGE");
                    presenter.setCurrentGPSMode(Model.GPS_MODE.AVERAGE);
                    break;
                case AVERAGE:
                    item.setTitle("GPS Mode: PONDERED");
                    presenter.setCurrentGPSMode(Model.GPS_MODE.PONDERATION);
                    break;
                case PONDERATION:
                    item.setTitle("GPS Mode: LAST");
                    presenter.setCurrentGPSMode(Model.GPS_MODE.LAST);
                    break;
            }
        }
        Log.i("SlidingMenu", "Menu item updated correctly");
        return true;
    }

    private void create_database_alert_dialog() {
        final AlertDialog.Builder dialog_builder = new AlertDialog.Builder(main_activity);
        dialog_builder.setTitle("Are you sure you want to delete the database?");
        dialog_builder.setIcon(R.drawable.ic_menu_height);

        dialog_builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try{
                    presenter.deleteDataBase();
                    Toast.makeText(main_activity,
                            "Database deleted correctly",
                            Toast.LENGTH_LONG).show();
                }catch(Exception e){
                    Toast.makeText(main_activity,
                            "Error was produced when removing your database",
                            Toast.LENGTH_LONG).show();
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
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GRAY);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
            }
        });
        dialog.show();
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
                        double truncate_height = Math.floor(height * 100) / 100;
                        menuView.getMenu().findItem(R.id.nav_actual_height)
                                .setTitle("     Actual: "+truncate_height+" meters");
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
    private void create_input_distance_dialog(String message) {

        final AlertDialog.Builder dialog_builder = new AlertDialog.Builder(main_activity);
        dialog_builder.setTitle(message);
        final EditText input = new EditText(main_activity);
        input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        String init_distance = Double.toString(0.0);
        input.setText(init_distance);
        input.requestFocus();
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        input.getBackground().mutate().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_ATOP);
        dialog_builder.setView(input);
        dialog_builder.setIcon(R.drawable.ic_menu_height);

        dialog_builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try{
                    double distance = Double.parseDouble(input.getText().toString());
                    if(distance >= 0){
                        if(INSERTION_MODE_ON){
                            presenter.setImageCreationDistance(distance);
                        }else{
                            presenter.setImageRemovalDistance(distance);
                        }
                        double truncate_distance = Math.floor(distance * 100) / 100;
                        menuView.getMenu().findItem(R.id.nav_actual_distance)
                                .setTitle("     Actual: "+truncate_distance+" meters");
                    }else{
                        Toast.makeText(main_activity,
                                "Insert a positive distance",Toast.LENGTH_LONG).show();

                    }
                }catch(Exception e){
                    Toast.makeText(main_activity,"Not valid distance introduced" +
                            "\nExample: '3.0' meters",Toast.LENGTH_LONG).show();
                }
            }
        });
        dialog_builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
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

    private void create_input_pixel_ratio(String message) {

        final AlertDialog.Builder dialog_builder = new AlertDialog.Builder(main_activity);
        dialog_builder.setTitle(message);
        final EditText input = new EditText(main_activity);
        input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        String actual_pixel_ratio = Double.toString(0);
        input.setText(actual_pixel_ratio);
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        input.getBackground().mutate().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_ATOP);
        dialog_builder.setView(input);
        dialog_builder.setIcon(R.drawable.ic_menu_height);

        dialog_builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try{
                    float ratio = Float.parseFloat(input.getText().toString());
                    if(ratio > 0){
                        presenter.setPixelsPerCentimeterRatio(ratio);
                        double truncate_distance = Math.floor(ratio * 100) / 100;
                        menuView.getMenu().findItem(R.id.nav_actual_pixel_ratio)
                                .setTitle("     "+truncate_distance+" pixels per cm");
                    }else{
                        Toast.makeText(main_activity,
                                "Insert a positive ratio greater than 0",Toast.LENGTH_LONG).show();

                    }
                }catch(Exception e){
                    Toast.makeText(main_activity,"Not valid ratio introduced" +
                            "\nExample: '5.5' pixels per cm",Toast.LENGTH_LONG).show();
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
