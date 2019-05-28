package uib.tfg.project.model.PictureDatabase;

import android.provider.BaseColumns;

public class PictureContract {

    public static abstract class PictureEntry implements BaseColumns {
        public static final String TABLE_NAME = "picture";
        public static final String USER_ID = "user_id";
        public static final String IMG_PATH = "img_path";
        public static final String IMG_LOCATION = "location";
        public static final String IMG_ROTATION = "rotation";
    }
}
