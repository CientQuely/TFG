package uib.tfg.project.model.Data;

import android.graphics.Bitmap;
import java.util.HashMap;

public class ImageCache {
    private HashMap<String,Bitmap> img_cache;
    private final static float INIT_DT_SIZE = 200;

    public ImageCache(){
        img_cache = new HashMap<>();
    }

    public Bitmap getCachedImage(String key){
        return img_cache.get(key);
    }

    public Bitmap cacheImage(String key,Bitmap bm){
        return img_cache.put(key, bm);
    }

    public Bitmap deleteCachedImage(String key){
        return img_cache.remove(key);
    }
}
