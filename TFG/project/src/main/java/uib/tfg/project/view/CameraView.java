package uib.tfg.project.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Size;
import android.util.SizeF;
import android.view.Surface;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.content.Context.CAMERA_SERVICE;


public class CameraView extends Thread{
    private Context appContext;
    private String TAG;
    private static volatile int threadNumber = 1;
    private Size cameraPreviewSize;
    private volatile TextureView cameraTextureView;
    private String cameraId;
    private CameraDevice camera;
    private CameraManager camManager;
    private CameraCharacteristics camCharacts;
    //Peticion para capturar la cámara
    private CaptureRequest camPreviewCaptureRequest;
    //Metodo callback para construir la petición
    private CaptureRequest.Builder camPreviewRequestBuilder;
    //Clase encargada de obtener y capturar las imágenes de la cámara
    private CameraCaptureSession camCaptureSession;
    //Contructor que se inicializa al empezar a capturar la sessión

    public static  float cameraVerticalAngle = 52.1f;
    public static  float cameraHorizontalAngle = 66.2f;
    private TextureView.SurfaceTextureListener camSurfaceTextureListener =
            new TextureView.SurfaceTextureListener() {

                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    Log.i(TAG,"CameraTexture Available");
                    setupCamera(width, height);
                    openCamera();
                    transformImage(width, height);
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                    Log.i(TAG,"CameraTexture Changed");
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    if(camera != null){
                        camera.close();
                        camera = null;
                    }
                    Log.i(TAG,"CameraTexture Destroyed");
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                }
            };
    //metodos callback que usará la camara al abrirse, cerrarse o desconectarse
    CameraDevice.StateCallback deviceCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice c) {
            Log.d(TAG, "Camera opened");
            camera = c;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            Log.w(TAG, "Camera disconected");
            camera.close();
            camera = null;
            interrupt();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.e(TAG, "Error on camera: " + error);
            camera.close();
            camera = null;
            interrupt();

        }

    };
    private CameraCaptureSession.CaptureCallback camSessionCaptureCallback

            = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }
    };

    //CONSTRUCTOR
    public CameraView(Context cont, View panelView, String log_tag) {
        this.TAG = log_tag;
        appContext = cont;
        cameraTextureView = (TextureView) panelView;
    }

    /**
     * Comprueba los permisos y abre la cámara trasera del dispositivo
     * @param width largo de la area que se va a mostrar en la cámara
     * @param height altura de la View
     */
    @SuppressLint("MissingPermission")
    private void setupCamera(int width, int height) {
        //Comprobamos los permisos
        if (!Permits.CAMERA_PERMIT) {
            Log.e(TAG, "This app don't have permissions to obtain the camera");
            return;
        }
        camManager = (CameraManager) appContext.getSystemService(CAMERA_SERVICE);
        if (camManager == null) {
            Log.e(TAG, "Camera system service not found");
            return;
        }
        cameraId = obtainBackCameraID(camManager);
        if (cameraId == null) {
            Log.e(TAG, "Back camera not found on this device");
            return;  //Cámara trasera no encontrada
        }
        //Obtenemos el tamaño de preview
        StreamConfigurationMap map =
                camCharacts.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        cameraPreviewSize = getPreferredPreviewSize(map.getOutputSizes(SurfaceTexture.class),
                width, height);
        transformImage(width, height);
    }

    public void openCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED)
                return;
            camManager.openCamera(cameraId, deviceCallback, null);
        } catch (CameraAccessException e) {
            Log.e("CameraView", "Error", e);
        }
    }

    /**
     * Este método modifica la matriz del TextureView para solucionar la rotación de la cámara del
     * dispositivo y ajustar las dimensiones de la cámara a las del teléfono.
     * @param width tamaño del panel
     * @param height tamaño del panel
     */
    public void transformImage(int width, int height){
        if(cameraPreviewSize == null || cameraTextureView == null) return;
        Matrix tvm = new Matrix();
        int rotation = ((Activity)appContext).getWindowManager().getDefaultDisplay().getRotation();
        RectF textureRectF = new RectF(0, 0, width, height);
        RectF previewRectF = new RectF(0, 0, cameraPreviewSize.getHeight(), cameraPreviewSize.getWidth());
        float centerX = textureRectF.centerX();
        float centerY = textureRectF.centerY();
        if((rotation == Surface.ROTATION_90) || (rotation == Surface.ROTATION_270)){
            previewRectF.offset(centerX - previewRectF.centerX(),
                    centerY - previewRectF.centerY());
            tvm.setRectToRect(textureRectF, previewRectF, Matrix.ScaleToFit.FILL);
            float scale = Math.max((float)width / cameraPreviewSize.getWidth(),
                    (float)height / cameraPreviewSize.getHeight());
            tvm.postScale(scale, scale, centerX, centerY);
            tvm.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        cameraTextureView.setTransform(tvm);
    }
    /***
     * Obtiene la string con el identificador de la cámara trasera
     * @return id de la primera cámara trasera encontrada
     */
    private String obtainBackCameraID(CameraManager cameraM){
        try {
            String[] cameraIdList = cameraM.getCameraIdList();
            CameraCharacteristics cameraCharacts;
            Integer cameraPosition;
            //Para cada una de las cámaras
            for (String cameraId : cameraIdList) {
                cameraCharacts = cameraM.getCameraCharacteristics(cameraId);
                cameraPosition = cameraCharacts.get(CameraCharacteristics.LENS_FACING);
                if (cameraPosition != null
                        && cameraPosition.equals(CameraCharacteristics.LENS_FACING_BACK)) {
                    camCharacts = cameraCharacts;
                    getCameraAngle(camCharacts);

                    //Devolvemos la Id de la cámara trasera
                    return cameraId;
                }
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error: CameraAccessException:", e);
        }
        return null;
    }

    private void  getCameraAngle(CameraCharacteristics info) {

        SizeF sensorSize = info.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
        float[] focalLengths = info.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);


        if (focalLengths != null && focalLengths.length > 0) {
            cameraHorizontalAngle = (float) (2 * Math.atan(sensorSize.getWidth() / (2 * focalLengths[0])) * 180 / Math.PI);
            cameraVerticalAngle = (float) (2 * Math.atan(sensorSize.getHeight() / (2 * focalLengths[0])) * 180 / Math.PI);
        }
    }


    /**
     * Obtiene el tamaño correcto de la cámara
     * @param mapSize
     * @param width
     * @param height
     * @return
     */
    private Size getPreferredPreviewSize(Size[] mapSize, int width, int height){
        List<Size> sizeCollector = new ArrayList<>();
        for (Size option : mapSize){
            if(option.getWidth() < width &&
                option.getHeight() > height){
                sizeCollector.add(option);
            } else {
            if(option.getWidth() < height &&
                    option.getHeight() > width){
                sizeCollector.add(option);
            }
        }
        if(sizeCollector.size() > 0) {
                return Collections.min(sizeCollector, new Comparator<Size>(){
                    @Override
                    public int compare(Size o1, Size o2) {
                        return Long.signum(o1.getWidth() * o1.getHeight()
                                - o2.getWidth()*o2.getHeight());
                    }
                });
        }
        }
        return mapSize[0];
    }

    public boolean cameraAvailable(){
        return cameraTextureView.isAvailable();
    }

    @Override
    public void run(){
        Looper.prepare();
        this.setName("CameraView"+threadNumber);
        threadNumber++;
        cameraTextureView.setSurfaceTextureListener(camSurfaceTextureListener);
    }

    public boolean isAvailable(){
        if(cameraTextureView == null) return false;
        return cameraTextureView.isAvailable();
    }
    /**
     * Crea la preview de la cámara enlazando la TextureView con el CameraDevice
     */
    private void createCameraPreviewSession(){
        try {
            //Obtenemos la superficie de la cámara
            SurfaceTexture surfaceTexture = cameraTextureView.getSurfaceTexture();
            //Obtenemos el tamaño de la TextureView que se va a mostrar
            surfaceTexture.setDefaultBufferSize(cameraTextureView.getWidth(), cameraTextureView.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            //Solicitamos que la cámara empiece a mandarnos imágenes
            camPreviewRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //Enlazamos nuestra texture view con la cámara
            camPreviewRequestBuilder.addTarget(previewSurface);
            //En caso de querer guardar fotos esta linea nos las orienta
            camPreviewRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION,
                    camCharacts.get(CameraCharacteristics.SENSOR_ORIENTATION));
            camera.createCaptureSession(Arrays.asList(previewSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            if(camera == null){
                                return;
                            }
                            try{
                                camPreviewCaptureRequest = camPreviewRequestBuilder.build();
                                camCaptureSession = session;
                                camCaptureSession.setRepeatingRequest(
                                        camPreviewCaptureRequest,
                                        camSessionCaptureCallback,
                                        null
                                );
                                Log.i(TAG,"Capture Session configurada correctamente");
                            }catch(CameraAccessException e){
                                e.printStackTrace();
                            }
                        }
                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Toast.makeText(appContext, "Create camera session failes", Toast.LENGTH_SHORT);
                        }
                    }, null);
        } catch (CameraAccessException e){
            Log.e(TAG, "Error", e);
        }
    }

    public void stopCameraStream() {
    }
}