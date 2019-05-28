package uib.tfg.project.model.PictureDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import uib.tfg.project.model.PictureDatabase.PictureContract.PictureEntry;
import uib.tfg.project.model.Data.PictureObject;

public class PictureDB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Picture.db";
    private static final int DATABASE_VERSION = 1;
    public PictureDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    private SQLiteDatabase db;

    public void PictureDB(){
        initiateDB();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+ PictureEntry.TABLE_NAME + " ("
                        + PictureEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + PictureEntry.IMG_PATH + " TEXT NOT NULL,"
                        + PictureEntry.IMG_LOCATION + " TEXT NOT NULL,"
                        + PictureEntry.IMG_PATH + "TEXT NOT NULL,"
                        + PictureEntry.IMG_ROTATION + "TEXT NOT NULL,"
                        + PictureEntry.USER_ID + "FLOAT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long insertPicture(PictureObject po){
        if (db == null) return -1;

        ContentValues values = toContentValue(po);

        return db.insert(PictureEntry.TABLE_NAME, null, values);
    }

    public Cursor getAllPictures(){
        if (db == null) return null;

        return db.query(PictureEntry.TABLE_NAME, null,null, null, null, null, null);
    }

    public long deletePicture(PictureObject po) {
        if (db == null) return -1;

        return db.delete(PictureEntry.TABLE_NAME, PictureEntry._ID + "=" + po.getPicture_id(), null);
    }

    public static ContentValues toContentValue(PictureObject po){
        ContentValues values = new ContentValues();
        values.put(PictureEntry.USER_ID, po.getUser_id());
        values.put(PictureEntry.IMG_PATH, po.getImage_path());
        values.put(PictureEntry.IMG_LOCATION, po.getLocationString());
        values.put(PictureEntry.IMG_ROTATION, po.getRotationString());
        return values;
    }

    public static PictureObject toPictureObject(Cursor c){
        long id = c.getLong(c.getColumnIndex(PictureEntry._ID));
        String img_path = c.getString(c.getColumnIndex(PictureEntry.IMG_PATH));
        float user_id = c.getFloat(c.getColumnIndex(PictureEntry.USER_ID));
        double [] latLongHeight = PictureObject.parseQueryString(c.getString(c.getColumnIndex(PictureEntry.IMG_LOCATION)));
        Location location = new Location("");
        location.setLatitude(latLongHeight[0]);
        location.setLatitude(latLongHeight[1]);
        double height = latLongHeight[2];
        double [] rotation = PictureObject.parseQueryString(c.getString(c.getColumnIndex(PictureEntry.IMG_ROTATION)));

        PictureObject po = new PictureObject(user_id, img_path, location, height, rotation);
        po.setPicture_id(id);

        return po;
    }
    public void closeDB(){
        if(db != null){
            db.close();
            db = null;
        }
    }

    public void initiateDB(){
        if(db == null){
            db = getWritableDatabase();
        }
    }

    public void cleanDB(){
        db.delete(PictureEntry.TABLE_NAME, null, null);
    }
}
