package uib.tfg.project.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class FileIO {
    protected static final String TAG="model/FileIO";

    protected static Bitmap openImageBitmap(String image_path){
        Log.i(TAG, "Loading image with path: "+image_path);
        Bitmap bMap = BitmapFactory.decodeFile(image_path);
        if (bMap == null){
            Log.i(TAG, "\t Image not found");
        }
        return bMap;
    }
}
