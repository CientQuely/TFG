package uib.tfg.project.view;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.support.v7.view.menu.MenuView;
import android.util.Log;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import uib.tfg.project.model.Data.PictureObject;
import uib.tfg.project.presenter.Presenter;

public class GLRenderer implements GLSurfaceView.Renderer {
    Presenter presenter;
    VirtualCameraView v;

    GLPicture [] glPictures;

    private volatile static boolean stopDrawing = false;
    private static float CentToMeters = 0.01f;
    private static float TO_DEGREES = 180 / (float)Math.PI;
    private ArrayList<PictureObject> poPictures;
    public GLRenderer(Presenter presenter, VirtualCameraView virtualCameraView) {
        this.presenter = presenter;
        this.v = virtualCameraView;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        // GLES20.glHint(GLES20.GL_F, GLES20.GL_NICEST);
        GLES20.glClearColor(0.80f,0,0,0f);
        // GLES20.glClearDepthf(1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_LEQUAL);

        GLES20.glEnable(GLES20.GL_BLEND );
        GLES20.glBlendFunc( GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA );

        GLES20.glDisable(GL10.GL_DITHER);
    }

    private float [] mProjectionMatrix = new float [16];
    private float [] mViewMatrix = new float [16];
    private float [] mMVPMatrix = new float [16];

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        // for a fixed camera, set the projection too
        GLES20.glClearColor(0f,0,0,0f);
        final float ratio = (float) width / height;

        Matrix.setIdentityM(mProjectionMatrix, 0);
        Matrix.perspectiveM(mProjectionMatrix, 0, CameraView.cameraVerticalAngle-15
                , ratio, 0.01f, 100);
    }

    private float [] getOpenGLOrientedDirectionPoint(){
        float [] openGLRotation = new float [3];

        double [] cameraRotation = v.getImageRelativeLocationLatLongHeight(1);

        //double [] cameraRotation = {0, -3, 0};
        openGLRotation[0] = (float)cameraRotation[1]; // Z = latitude
        openGLRotation[1] = (float)cameraRotation[2]; // Y = height
        openGLRotation[2] = -(float)cameraRotation[0]; // X = longitude

        return openGLRotation;
    }

    private void applyOrientation(){
        //Get Direction Point
        float [] centerVector = getOpenGLOrientedDirectionPoint();

        // Set the camera position (View matrix)
        float userHeight = (float)presenter.getUserHeight();


        Matrix.setIdentityM(mViewMatrix,0);
        Matrix.setLookAtM(mViewMatrix, 0,
                0, userHeight, 0,
                centerVector[0], userHeight + centerVector[1]  , centerVector[2],
                0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        if(VirtualCameraView.isRollEnabled()){
            float rollRotation = (float)presenter.getUserRotation().getNormalizedRoll() * TO_DEGREES;
            Matrix.rotateM(mMVPMatrix, 0, rollRotation, centerVector[0], centerVector[1], centerVector[2]);
        }

    }


    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if(stopDrawing) return;

        applyOrientation();

        //If picture list is modified
        if (presenter.pictureListModified()){

            poPictures = presenter.getNearestImages();
            int size = poPictures.size();

            Log.i( "GL RENDERER", "Updating all pictures! "+ size);
            //Create objects
            if(size > 0){
                glPictures = new GLPicture[poPictures.size()];
            }else {
                glPictures = null;
            }

            //Clean textures
            GLPicture.cleanTextures();

            if(glPictures != null){
                for (int i = 0; i < glPictures.length; i++){
                    glPictures[i] = createGLPictureFromPictureObject(gl, poPictures.get(i));
                }

            }

            //Notify to model that our picture list is up to date
            presenter.pictureListUpToDate();

            Log.i( "GL RENDERER", "Update finished!");
        }

        if(glPictures != null){
            for( int j = 0; j < glPictures.length; j++){
                float [] relativePosition =  getRelativePosition(poPictures.get(j));
                glPictures[j].draw(mMVPMatrix, relativePosition);
            }
        }

    }

    public static void setStopDrawing(boolean state){
           stopDrawing = state;
    }

    private GLPicture createGLPictureFromPictureObject(GL10 gl, PictureObject image){
        float pixelSize = CentToMeters/presenter.getPixelsPerCentimeterRatio(image);
        float [] rotationMatrix = presenter.getPictureRotationMatrix(image);
        Bitmap bitmap = presenter.getPictureBitmap(image);
        return new GLPicture(image.getImage_path(), rotationMatrix, bitmap, pixelSize);
    }

    private float [] getRelativePosition(PictureObject image){
        double [] imagePosition = presenter.getPicturePosition(image);
        double [] userPosition = presenter.getUserLocationInMeters().clone();

        for (int i = 0; i < imagePosition.length - 1; i++){
            imagePosition[i] = imagePosition[i] - userPosition[i];
        }

        //Conversions of coordenates for opengl
        float [] relativePosition = new float [3];
        //OGL X = Real longitude
        relativePosition[0] = (float)imagePosition[1];

        //OGL Y = Real Height
        relativePosition[1] = (float)imagePosition[2];

        //OGL Z = latitude
        relativePosition[2] = -(float)imagePosition[0];


        return relativePosition;
    }

    public static float [] getImageRotationMatrix( float x, float y, float z){
        float [] rotationMatrix = new float [16];
        Matrix.setLookAtM(rotationMatrix, 0,
                -x, -y, z,
                0, 0, 0,
                0, 1, 0);
        return rotationMatrix;
    }

    public static int loadShader(int type,String shaderCode){
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader,shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }
}
