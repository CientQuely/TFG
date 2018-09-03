package uib.tfg.project.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.view.GestureDetector;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import android.util.Log;

import uib.tfg.project.ProjectView;
import uib.tfg.project.R;
import uib.tfg.project.model.Data.PictureObject;
import uib.tfg.project.presenter.Presenter;
import uib.tfg.project.presenter.ProjectPresenter;


public class AugmentedReality extends Activity implements View{

    private Presenter presenter;
    private CameraView cameraStream;
    private VirtualCameraView virtualStream;
    private SlidingMenu slidingMenu;
    private final Semaphore mutex = new Semaphore(0);
    private static final String TAG = "View/AugmentedReality";
    private GestureDetector gestureDetector;

    public AugmentedReality(){
        super();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bitmap not_foung_img = BitmapFactory.decodeResource(this.getResources(), R.drawable.not_found_img);
        presenter = new ProjectPresenter(this, this, not_foung_img);
        if(!hasFeatures()){
            Toast.makeText(this, "Tu movil no posee todas las características para" +
                            "poder ejecutar esta app.",
                    Toast.LENGTH_LONG).show();
            goToMenu();
            return;
        }

        setContentView(R.layout.activity_augmented_reality);

        try {
            slidingMenu = new SlidingMenu(this, this.findViewById(R.id.nav_view), presenter);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        //Crea la vista de la camara
        cameraStream = new CameraView(this, this.findViewById(R.id.camera_view), "View/Camera/CameraView");

        findViewById(R.id.virtual_view).setOnTouchListener(new DoubleTouchListener() {

            @Override
            public void onDoubleTouch(float x, float y) {
                onDoubleTouchScreen(x, y);
            }

        });
    }

    @Override
    public void onStart(){
        super.onStart();
        if(!Permits.hasAllPermits(this)) {
            getRemainingPermits();
            Log.i(TAG,"Waiting permission response...");
            return;
        }
    }
    private boolean hasFeatures() {
        PackageManager pm = this.getPackageManager();
        boolean hasAllFeatures = true;
        if(!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            Log.e("TAG","This device don't have Camera");
            hasAllFeatures = false;
        }

        if(!pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)){
            Log.e("TAG","This device don't have GPS system.");
            hasAllFeatures = false;
        }

        if(!pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE)){
            Log.e("TAG","This device don't have Gyroscope sensor.");
            hasAllFeatures = false;
        }

        if(!pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS)){
            Log.e("TAG","This device don't have Compass sensor.");
            hasAllFeatures = false;
        }

        if(!pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)){
            Log.e("TAG","This device don't have Accelerometer sensor.");
            hasAllFeatures = false;
        }

        Log.i(TAG, "Device has all features used by this app");
        return hasAllFeatures;
    }

    private void getRemainingPermits() {
        String [] features_to_request = Permits.getFeaturesToRequest();
        if(features_to_request != null) {
            ActivityCompat.requestPermissions(this, features_to_request, Permits.ALL_PERMITS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults){
        if(Permits.ALL_PERMITS_REQUEST == requestCode){
            Permits.updatePermits(permissions, grantResults);
        }

        if(Permits.hasAllPermits(this)){
            Toast.makeText(this, "Todos los permisos obtenidos !!",
                    Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this, "Esta app no puede funcionar si no se aceptan" +
                            "todos los permisos.",
                    Toast.LENGTH_LONG).show();
            goToMenu();
        }

        if(Permits.CAMERA_PERMIT){
            //si es la primera vez que llamamos a la camara y aun no teniamos permisos
            if(!cameraStream.cameraAvailable()){
                cameraStream.start();
            }
        }
    }

    private void goToMenu() {
        Intent pv = new Intent(this, ProjectView.class);
        startActivity(pv);
    }

    @Override
    public void onResume(){
        super.onResume();
        presenter.setContext(this);
        if(!cameraStream.isAvailable()){
           cameraStream.start();
        }
        if(Permits.GPS_PERMIT){
           presenter.initiateLocationService();
        }
        if(Permits.SENSORS_PERMIT){
            presenter.initiateSensorsService();
        }
        if(Permits.STORAGE_PERMIT){
            presenter.initiatePictureLoader();
        }
        if(Permits.hasAllPermits(this)){
            virtualStream = new VirtualCameraView(this, this.findViewById(R.id.virtual_view),
                    this.findViewById(R.id.debbugerText),presenter, "View/VirtualCameraView");
            virtualStream.start();
        }
    }

    protected void onPause() {
        super.onPause();
        if(virtualStream != null && virtualStream.isRunning()){
            virtualStream.stopVirtualReality();
        }
        cameraStream.stopCameraStream();
        presenter.stopLocationService();
        presenter.stopSensorsService();
        presenter.stopPictureLoader();
        //presenter.storeDataBase();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == SlidingMenu.PICK_IMAGE && data != null && resultCode != 0) {
                Uri selectedImage = data.getData();
                String path = getRealPathFromURI(this,selectedImage);
            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.
                        Media.getBitmap(this.getContentResolver(), selectedImage);
                if( bitmap != null){
                    presenter.setUserCurrentBitmap(path, bitmap);
                    slidingMenu.showMenuMessage("Image loaded correctly");
                }else{
                    slidingMenu.showMenuMessage("Image could not be loaded");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static String getRealPathFromURI(Context context, Uri uri){
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

    private void onDoubleTouchScreen(float x, float y) {
        PictureObject pointed_picture = virtualStream.findPointedPicture();
        if(pointed_picture == null){
            try {
                presenter.deletePicture(pointed_picture);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return;
        }
        if(presenter.getCurrentBitmap() == null){
            Toast.makeText(this, "Image not selected, please select an image.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Location new_location = virtualStream.findPointedLocation();
        float new_height = virtualStream.obtainPictureHeight();
        presenter.createPicture(new_location, new_height);

    }

}
