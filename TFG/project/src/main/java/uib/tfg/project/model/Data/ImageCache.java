package uib.tfg.project.model.Data;

import android.graphics.Bitmap;
import java.util.HashMap;

public class ImageCache {
    private HashMap<String,Bitmap> img_cache;
    private final static float INIT_DT_SIZE = 200;
    public static final String NOT_FOUND_IMAGE = "NOT_FOUND_IMAGE";

    public Bitmap getNotFoundImage() {
        return not_found_image;
    }

    private Bitmap not_found_image;
    public ImageCache(Bitmap not_found_bitmap){
        img_cache = new HashMap<>();
        cacheImage(NOT_FOUND_IMAGE, not_found_bitmap);
        this.not_found_image = not_found_bitmap;
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
