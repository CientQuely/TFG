package uib.tfg.project.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.widget.Toast;

import java.io.IOException;
import android.util.Log;

import uib.tfg.project.R;
import uib.tfg.project.model.Data.PictureObject;
import uib.tfg.project.presenter.Presenter;
import uib.tfg.project.presenter.ProjectPresenter;


public class AugmentedReality extends Activity implements View{

    private Presenter presenter;
    private CameraView cameraStream;
    private VirtualCameraView virtualStream;
    private LoggerText loggerStream;
    private SlidingMenu slidingMenu;
    private static final String TAG = "View/AugmentedReality";

    public AugmentedReality(){
        super();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bitmap not_foung_img = BitmapFactory.decodeResource(this.getResources(), R.drawable.not_found_img);

        presenter = new ProjectPresenter(this, this, not_foung_img);

        //DB Initiation
        presenter.startDataBase();
        presenter.populateImagesFromDB();

        setContentView(R.layout.activity_augmented_reality);

        try {
            slidingMenu = new SlidingMenu(this, findViewById(R.id.nav_view), presenter);

            findViewById(R.id.virtual_view).setOnTouchListener(new DoubleTouchListener() {

                @Override
                public void onDoubleTouch(float x, float y) {
                    onDoubleTouchScreen(x, y);
                }

            });

        } catch (Exception e) {
            Log.e(TAG, "Error", e);
        }

        //Crea la vista de la camara
        cameraStream = new CameraView(this, this.findViewById(R.id.camera_view), "View/Camera/CameraView");

        loggerStream = new LoggerText(this, presenter, this.findViewById(R.id.debbugerText));


        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull android.view.View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull android.view.View drawerView) {
                if(virtualStream != null){
                    virtualStream.hideVirtualCameraView();
                }
            }

            @Override
            public void onDrawerClosed(@NonNull android.view.View drawerView) {
                if(virtualStream != null){
                    virtualStream.unhideVirtualCameraView();
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onResume(){
        super.onResume();

        presenter.setContext(this);
        presenter.startDataBase();

        if(!cameraStream.isAvailable()){
            cameraStream.start();
        }

        if(! slidingMenu.GPS_BLOCKED){
            presenter.initiateLocationService();
        }

        presenter.initiateSensorsService();

        presenter.initiatePictureLoader();

        if (virtualStream == null){
            virtualStream = new VirtualCameraView(this.findViewById(R.id.virtual_view), presenter);
        }else{
            virtualStream.onResume();
        }

        loggerStream = new LoggerText(this, presenter, this.findViewById(R.id.debbugerText));
        loggerStream.start();
    }

    protected void onPause() {
        super.onPause();
        cameraStream.stopCameraStream();
        presenter.stopLocationService();
        presenter.stopSensorsService();
        presenter.stopPictureLoader();
        if (virtualStream != null) {
            virtualStream.onPause();
        }
        if (loggerStream.isRunning()) {
            loggerStream.stopLoggerText();
        }
        presenter.stopDataBase();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == SlidingMenu.PICK_IMAGE && data != null && resultCode != 0) {
                Uri selectedImage = data.getData();

                String path;
                try{
                    path = getRealPathFromURI(this,selectedImage);
                }catch(Exception e){
                    Log.e("AugmentedReality", "Error", e);

                    Toast.makeText(this, "Image could not be loaded due to an erroneous " +
                                    "format or a not allowed destination",
                            Toast.LENGTH_LONG).show();
                    return;
                }
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
                Log.e("Augmented Reality", "exception", e);
            }

        }else{
            slidingMenu.showMenuMessage("Image not obtained, please select another image");
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

    public void onDoubleTouchScreen(float x, float y) {
        Log.i("Main Thread Augmented Reality", " Double touch created");
        if(slidingMenu.INSERTION_MODE_ON){
            if(presenter.getCurrentBitmap() == null){
                Toast.makeText(this, "Image not selected, please select an image.",
                        Toast.LENGTH_LONG).show();
            }else{
                createImage();
            }
        }else{
            deleteImage();
        }
        Log.i("Main Thread Augmented Reality", " Double touch completed");
    }

    private synchronized void deleteImage(){
        double distance = presenter.getImageRemovalDistance();
        PictureObject pointed_picture = virtualStream.getNearestImageInRange(distance);
        if(pointed_picture == null) {
            Toast.makeText(this, "No image closer than "+ distance + " meter has been found for removal",
                    Toast.LENGTH_LONG).show();
        }else{
            presenter.deletePicture(pointed_picture);
            Toast.makeText(this, "Image deleted",
                    Toast.LENGTH_LONG).show();
        }
    }

    private synchronized void createImage(){
        double distance = presenter.getImageCreationDistance();
        double [] imageLocation = virtualStream.getPointedLocation(distance);
        float [] imageRotation = virtualStream.getImageRotation();
        presenter.createPicture(imageLocation, imageRotation);
        Toast.makeText(this, "Image created at "+distance+ " meters",
                Toast.LENGTH_SHORT).show();
    }
}

