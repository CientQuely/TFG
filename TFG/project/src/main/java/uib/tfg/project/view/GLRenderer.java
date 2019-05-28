package uib.tfg.project.view;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

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
        final float bottom = 1;
        final float top = 1;
        final float left = -ratio;
        final float right = ratio;
        final float near = 0.01f;
        final float far = 100.0f;
        //Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
        Matrix.perspectiveM(mProjectionMatrix, 0, CameraView.cameraVerticalAngle-9, ratio, 0.01f, 100);
    }

    private void applyOrientation(){
        //Get Direction
        double [] centerVector = v.getImageRelativeLocation(1);
        // Set the camera position (View matrix)
        float userHeight = (float)presenter.getUserHeight();
        Matrix.setLookAtM(mViewMatrix, 0,
                0, userHeight, 0,
                //1f, 0, 0f,
                //0.4f, 0, 0.3f,
                (float)centerVector[0], userHeight + (float)centerVector[2]  , - (float) centerVector[1],
                0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if(stopDrawing) return;

        applyOrientation();

        ArrayList<PictureObject> poPictures = presenter.getNearestImages();

        //If picture list is modified
        if (presenter.pictureListModified()){
            //Create objects
            glPictures = new GLPicture[poPictures.size()];

            //Clean textures
            GLPicture.cleanTextures();

            for (int i = 0; i < glPictures.length; i++){
                glPictures[i] = createGLPictureFromPictureObject(gl, poPictures.get(i));
            }

            //Notify to model that our picture list is up to date
            presenter.pictureListUpToDate();

        }

        for( int j = 0; j < glPictures.length; j++){
            float [] relativePosition = getRelativePosition(poPictures.get(j)); // {1,0,0};

            glPictures[j].draw(mMVPMatrix, relativePosition);
        }

    }

    public static void setStopDrawing(boolean state){
           stopDrawing = state;
    }

    private GLPicture createGLPictureFromPictureObject(GL10 gl, PictureObject image){
        double [] rotation = presenter.getPictureRotation(image);
        Bitmap bitmap = presenter.getPictureBitmap(image);
        return new GLPicture(image.getImage_path(), rotation, bitmap);
    }

    private float [] getRelativePosition(PictureObject image){
        double [] imagePosition = presenter.getPicturePosition(image);
        double [] userPosition = presenter.getUserLocationInMeters();

        for (int i = 0; i < imagePosition.length - 1; i++){
            imagePosition[i] = imagePosition[i] - userPosition[i];
        }

        //Conversions of coordenates for opengl
        float [] relativePosition = new float [3];
        //OGL X = Real latitude
        relativePosition[0] = (float)imagePosition[0];

        //OGL Y = Real Height
        relativePosition[1] = (float)imagePosition[2];

        //OGL Z = - Real longitude
        relativePosition[2] = - (float)imagePosition[1];


        return relativePosition;
    }

    public static int loadShader(int type,String shaderCode){
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader,shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }
}
