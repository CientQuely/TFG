package uib.tfg.project.view;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;
import java.util.concurrent.Semaphore;
import android.util.Log;

import uib.tfg.project.R;
import uib.tfg.project.presenter.Presenter;
import uib.tfg.project.presenter.ProjectPresenter;
import uib.tfg.project.view.camera.CameraView;


public class AugmentedReality extends Activity implements View{

    private Presenter presentador;
    private CameraView vistaCamara;
    private final Semaphore mutex = new Semaphore(0);
    private String TAG = "View/AugmentedReality";
    protected static class Permissions {
        static public boolean CAMERA_PERMISSION = false;
        static public boolean GPS_PERMISSION = false;
        static public boolean STORAGE_PERMISSION = false;
        static public boolean SENSORS_PERMISSION = false;

        static final int CAMERA_REQUEST = 0;
        static final int GPS_REQUEST = 1;
        static final int STORAGE_REQUEST = 2;
        static final int SENSORS_REQUEST = 3;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presentador = new ProjectPresenter(this);

        //Se encarga de obtener todos los permisos
        if(!hasFeatures() && !hasAllPermissions()) {
             Toast.makeText(this, "Esta app no puede funcionar si no se aceptan" +
                                "todos los permisos.",
                        Toast.LENGTH_LONG).show();
             setContentView(R.layout.menu);
             return;
        }
        setContentView(R.layout.activity_augmented_reality);
        //Crea la vista de la camara
        vistaCamara = new CameraView(this, this.findViewById(R.id.camera_view), "View/Camera/CameraView");
    }

    private boolean hasFeatures() {
        return true;
    }
    private boolean hasAllPermissions() {
        try{
            //CAMERA PERMISSION
            if(!CameraView.hasCameraPermission(this)) {
                CameraView.requestCameraPermission(this, Permissions.CAMERA_REQUEST);
                mutex.acquire();
                if(!Permissions.CAMERA_PERMISSION){
                    Log.w(TAG,"Camera permission not granted");
                    return false;
                }
            }
        }catch(InterruptedException ie){
            Log.e(TAG,"Program interrupted while checking permissions");
        }
        return true;
    }


    protected void onPause() {
        super.onPause();
        //liberar componentes
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults){
        boolean hasPermissions = false;
        if(grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            hasPermissions = true;
        switch(requestCode){
            case Permissions.CAMERA_REQUEST:
                Permissions.CAMERA_PERMISSION = hasPermissions;
                break;
            case Permissions.GPS_REQUEST:
                Permissions.GPS_PERMISSION = hasPermissions;
                break;
            case Permissions.STORAGE_REQUEST:
                Permissions.STORAGE_PERMISSION = hasPermissions;
                break;
            case Permissions.SENSORS_REQUEST:
                Permissions.SENSORS_PERMISSION = hasPermissions;
                break;
        }
        mutex.release();
    }

    @Override
    public void onResume(){
        super.onResume();

        if(vistaCamara.cameraAvailable()){

        } else {
            vistaCamara.setCamSurfaceTextureListener();
        }
    }

}
