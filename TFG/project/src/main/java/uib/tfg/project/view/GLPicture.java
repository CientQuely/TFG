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
    private static float PIXEL_SIZE = 0.0005f;

    private static final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;"+
                    "attribute vec4 inputTextureCoordinate;" +
                    "varying vec2 v_TexCoord;"+
            "attribute vec4 vPosition;"+
                    "void main() {"+
                    "gl_Position = uMVPMatrix * vPosition;"+
                    "v_TexCoord = inputTextureCoordinate.xy;" +
                    "}";
    private static final String fragmentShaderCode =
            "varying vec2 v_TexCoord;"+
            "uniform sampler2D u_texture;" +
                    "void main() {" +
                    //"gl_FragColor=vec4(v_TexCoord.x,0,v_TexCoord.y,1);" +
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
    private double [] imgRotation;

    public static void cleanTextures(){

        int [] textToDelete = new int [texturesUsedList.size()];

        for (int i = 0; i < texturesUsedList.size(); i++){
            textToDelete[i] = texturesUsedList.get(i);
        }

        GLES20.glDeleteTextures(textToDelete.length, textToDelete, 0);
        texturesUsedList.clear();
        texture_hash.clear();
    }

    public GLPicture(String imgPath, double [] imgRotation, Bitmap bitmap){
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
        float mH = height * PIXEL_SIZE / 2;
        float mW = width * PIXEL_SIZE / 2;

        float [] sizeCoords = {
            0,mH,-mW, // top left
            0,-mH, -mW, // bottom left
            0,-mH, mW, // bottom right
            0,mH,mW, // top right
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

    private float [] movePicture(float x, float y, float z){
        float [] realPosition = {
                x + quadCoords[0], y + quadCoords[1], z + quadCoords[2],
                x + quadCoords[3], y + quadCoords[4], z + quadCoords[5],
                x + quadCoords[6], y + quadCoords[7], z + quadCoords[8],
                x + quadCoords[9], y + quadCoords[10], z + quadCoords[11],
        };
        return realPosition;
    }

    private void setVectorBuffer(float [] realPosition){
        vertexBuffer.clear();
        vertexBuffer.put(realPosition);
        vertexBuffer.position(0);
    }

    public void draw(float [] myMVPMatrix, float [] position){

        float [] realPosition = movePicture(position[0], position[1], position[2]);
        setVectorBuffer(realPosition);

        int mMVPMatrixHandle;

        GLES20.glUseProgram(mProgram);

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // get texture handle
        int textureUniformHandle = GLES20.glGetAttribLocation(mProgram, "u_tecture");
        textureHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");

        // Bind current texture
        GLES20.glActiveTexture ( GLES20.GL_TEXTURE0 );
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_id[0]);

        //Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(textureUniformHandle, 0);





        // SET RECTANGLE VERTEX
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_POINT,
                GLES20.GL_FLOAT,false,vertexStride ,vertexBuffer); // vertexStride

        //SET TEXTURE
        GLES20.glEnableVertexAttribArray(textureHandle);
        GLES20.glVertexAttribPointer(textureHandle, 2, GLES20.GL_FLOAT, false, textureStride, textureBuffer);

        // VIEW TRANSFORMATION
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, myMVPMatrix, 0);

        // DRAW PICTURE
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

    }

}