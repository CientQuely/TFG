package uib.tfg.project.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.FrameLayout;
import android.widget.Toast;

import uib.tfg.project.R;
import uib.tfg.project.presenter.Presenter;
import uib.tfg.project.presenter.ProjectPresenter;
import uib.tfg.project.view.view_exception.CAMERA_NOT_FOUND_EXCEPTION;

public class AugmentedReality extends Activity implements View{

    private Presenter presentador;


    private Camera camara;
    private uib.tfg.project.view.CameraView vistaCamara;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!obtainPermission()){
            Toast.makeText(this, "Esta app no puede funcionar si no se aceptan" +
                            "todos los permisos.",
                    Toast.LENGTH_LONG).show();
        }

        try {
            createCameraView();

        } catch (CAMERA_NOT_FOUND_EXCEPTION c) {
            Toast.makeText(this, "Error: Camera not found in this device",
                    Toast.LENGTH_LONG).show();
        }



        setContentView(R.layout.activity_augmented_reality);

        FrameLayout frame = findViewById(R.id.camera_view);
        frame.addView(vistaCamara);

        presentador = new ProjectPresenter(this);
    }

    private boolean obtainPermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){

            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {

            }else{

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, 0
                );

            }
        }
        return true;
    }

    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    private void createCameraView() throws CAMERA_NOT_FOUND_EXCEPTION{

        if(!checkCamera(getApplicationContext())) throw new CAMERA_NOT_FOUND_EXCEPTION();

        camara = obtainCamera();
        if(camara == null ){
            Toast.makeText(this, "Error: Camera is in use by other program",
                    Toast.LENGTH_LONG).show();
        }

        vistaCamara = new CameraView(this, camara);
    }

    /** Comprueba que la cámara existe*/
    private boolean checkCamera(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // tiene cámara
            return true;
        } else {
            return false;
        }
    }

    /** Obtiene una instancia de la cámara */
    public static Camera obtainCamera() throws RuntimeException{
        Camera c = null;
        try {
            //Obtiene el control de la cámara trasera del dispositivo
            c = Camera.open(0);
        }
        catch (RuntimeException re){
        }
        return c; // returns null if camera is unavailable
    }

    /**
     * Libera la cámara del control de esta app
     */
    private void releaseCamera(){
        if (camara != null){
            camara.release();
            camara = null;
        }
    }

}
