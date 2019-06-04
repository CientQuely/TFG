package uib.tfg.project.view;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import uib.tfg.project.model.representation.Matrix;

class GLPicture {


    private static HashMap<String, Integer> texture_hash = new HashMap<>();
    private static ArrayList<Integer> texturesUsedList = new ArrayList<>();

    private static final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;"+
                    "uniform mat4 mTranslation;"+
                    "uniform mat4 mRotation;"+
                    "attribute vec4 inputTextureCoordinate;" +
                    "varying vec2 v_TexCoord;"+
            "attribute vec4 vPosition;"+
                    "void main() {"+
                    "gl_Position = uMVPMatrix * (vPosition * mRotation * mTranslation);"+
                    "v_TexCoord = inputTextureCoordinate.xy;" +
                    "}";
    private static final String fragmentShaderCode =
            "varying vec2 v_TexCoord;"+
            "uniform sampler2D u_texture;" +
                    "void main() {" +
                    "gl_FragColor = texture2D(u_texture, v_TexCoord);" +
                    "}";

    private static final int COORDS_PER_POINT = 3;
    private float quadCoords[] = new float [12];

    //We will print it as 2 triangles
    private short drawOrder[] = { 0, 1, 2, 0, 2, 3 };


    float textureCoordinates[] = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,};

    private final int FLOAT_SIZE = 4;
    private final int vertexStride = COORDS_PER_POINT * FLOAT_SIZE;
    private final int textureStride = 2 * FLOAT_SIZE;
    private float color[] = {0.63f,0.76f,0.22f,1.0f};

    private int mProgram;

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;
    private FloatBuffer textureBuffer;

    private int mPositionHandle;
    private int textureHandle;

    private int [] texture_id = new int [1];
    private final String imgPath;
    private float [] imgRotation;

    public static void cleanTextures(){

        int [] textToDelete = new int [texturesUsedList.size()];

        for (int i = 0; i < texturesUsedList.size(); i++){
            textToDelete[i] = texturesUsedList.get(i);
        }

        GLES20.glDeleteTextures(textToDelete.length, textToDelete, 0);
        texturesUsedList.clear();
        texture_hash.clear();
    }

    public GLPicture(String imgPath, float [] imgRotation, Bitmap bitmap, float pixel_size){
        this.imgPath = imgPath;
        this.imgRotation = imgRotation;

        //Check if image texture already exists
        Integer textureInteger = texture_hash.get(imgPath);
        if (textureInteger == null) {
            textureInteger = loadTexture(bitmap);
            //Insert new element into hash
            texture_hash.put(imgPath, textureInteger);
            texturesUsedList.add(textureInteger);
        }
        texture_id[0] = textureInteger;

        //generate imageRelativeSize
        generateVertexImageSize(bitmap.getHeight(), bitmap.getWidth());
        resizeMatrix(pixel_size);

        //create buffer with vertex position
        createVertexDrawPositionBuffer();

        //create buffer with vertex draw order
        createVertexDrawOrderBuffer();

        //create texture coordinates buffer
        createTextureBuffer();

        // PROGRAM CREATION
        createProgram();
    }

    private void generateVertexImageSize(float height, float width){
        float mH = height / 2;
        float mW = width / 2;

        float [] sizeCoords = {
                -mW,mH,0, // top left
                -mW,-mH,0, // bottom left
                mW,-mH,0, // bottom right
                mW,mH,0, // top right
        };

        quadCoords = sizeCoords;
    }


    private void createVertexDrawPositionBuffer(){
        ByteBuffer bb = ByteBuffer.allocateDirect(
                quadCoords.length*FLOAT_SIZE);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(quadCoords);
        vertexBuffer.position(0);
    }

    //Create Vertex Position Draw Buffer
    // initialize byte buffer for the draw list
    private void createVertexDrawOrderBuffer(){
        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);
    }

    private void createTextureBuffer(){
        ByteBuffer bb = ByteBuffer.allocateDirect(
                textureCoordinates.length*FLOAT_SIZE);
        bb.order(ByteOrder.nativeOrder());
        textureBuffer = bb.asFloatBuffer();
        textureBuffer.put(textureCoordinates);
        textureBuffer.position(0);
    }

    private void createProgram(){
        int vertexShader = GLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER,
                vertexShaderCode
        );
        int fragmentShader = GLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode
        );

        //Create a program with the shape (vertexShader) and with the textures (fragmentShader)
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram,vertexShader);
        GLES20.glAttachShader(mProgram,fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }

    public static int loadTexture(Bitmap bitmap)
    {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0)
        {
            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            // Set wrapping mode
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        }

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

    private void resizeMatrix(float pixelSize){
        for (int i = 0; i < quadCoords.length; i++) {
            quadCoords[i] = quadCoords[i]*pixelSize;
        }
    }

    private void setVectorBuffer(float [] realPosition){
        vertexBuffer.clear();
        vertexBuffer.put(realPosition);
        vertexBuffer.position(0);
    }

    private float [] createTranslationMatrix(float x, float y, float z){
        float [] translationMatrix = new float [16];
        Matrix.setIdentityM(translationMatrix, 0);
        translationMatrix[3] = x;
        translationMatrix[7] = y;
        translationMatrix[11] = z;
        return translationMatrix;
    }


    public void draw(float [] myMVPMatrix, float [] position){
        setVectorBuffer(quadCoords);

        GLES20.glUseProgram(mProgram);

        //TRANSLATION
        int translationHandler = GLES20.glGetUniformLocation(mProgram, "mTranslation");
        float [] translationMatrix = createTranslationMatrix(position[0], position[1], position[2]);

        //ROTATION
        int rotationHandler = GLES20.glGetUniformLocation(mProgram, "mRotation");

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // get handle to shape's transformation matrix
        int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        textureHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");

        // Bind current texture
        GLES20.glActiveTexture ( GLES20.GL_TEXTURE0 );
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_id[0]);

        // SET RECTANGLE VERTEX
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_POINT,
                GLES20.GL_FLOAT,false,vertexStride ,vertexBuffer); // vertexStride

        //SET TEXTURE
        GLES20.glEnableVertexAttribArray(textureHandle);
        GLES20.glVertexAttribPointer(textureHandle, 2, GLES20.GL_FLOAT, false, textureStride, textureBuffer);

        // BIND ALL MATRIX
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, myMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(translationHandler, 1, false, translationMatrix, 0);
        GLES20.glUniformMatrix4fv(rotationHandler, 1, false, imgRotation, 0);

        // DRAW PICTURE
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

    }

}