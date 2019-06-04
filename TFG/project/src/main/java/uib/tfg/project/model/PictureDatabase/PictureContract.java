package uib.tfg.project.model.PictureDatabase;

import android.provider.BaseColumns;

public class PictureContract {

    public static abstract class PictureEntry implements BaseColumns {
        public static final String TABLE_NAME = "picture";
        public static final String USER_ID = "user_id";
        public static final String IMG_PATH = "img_path";
        public static final String IMG_LOCATION = "location";
        public static final String IMG_ROTATION = "rotation";
        public static final String IMG_PIXEL_RATIO = "pixel_per_cm";

        public static final String TABLE_CREATION_QUERY =
                "CREATE TABLE "+ PictureEntry.TABLE_NAME + " ("
                + PictureEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + PictureEntry.IMG_PATH + " TEXT NOT NULL,"
                + PictureEntry.IMG_LOCATION + " TEXT NOT NULL,"
                + PictureEntry.IMG_ROTATION + " TEXT NOT NULL,"
                        + PictureEntry.IMG_PIXEL_RATIO + " FLOAT,"
                + PictureEntry.USER_ID + " FLOAT)";
    }
}
