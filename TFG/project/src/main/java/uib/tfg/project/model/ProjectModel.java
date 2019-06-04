package uib.tfg.project.model;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.Observer;
import java.util.concurrent.Semaphore;

import uib.tfg.project.model.Data.HashPictureBox;
import uib.tfg.project.model.Data.ImageCache;
import uib.tfg.project.model.Data.PictureBox;
import uib.tfg.project.model.Data.PictureObject;
import uib.tfg.project.model.Data.UserData;
import uib.tfg.project.model.PictureDatabase.PictureDB;
import uib.tfg.project.model.representation.Quaternion;
import uib.tfg.project.presenter.Presenter;

/**
 * Created by Micky on 15/03/2018.
 */

public class ProjectModel implements Model{
    private Presenter presenter;
    UserData user_data;
    private HashPictureBox picture_hash;
    private ImageCache image_cache;
    protected Semaphore hash_mutex = new Semaphore(1);
    protected Semaphore nimg_mutex = new Semaphore(1);
    PictureDB pictureDB;
    private volatile boolean pictureListModified = false;
    ArrayList<PictureObject> nearestPictures = new ArrayList<PictureObject>();
    ArrayList<PictureObject> oldNearestPictures = new ArrayList<PictureObject>();

    public ProjectModel(Presenter p, Bitmap not_found_img, Context c) {
        this.presenter = p;
        picture_hash = new HashPictureBox();
        user_data = new UserData(1);
        image_cache = new ImageCache(not_found_img);
        pictureDB = new PictureDB(c);
    }


    @Override
    public void loadDataBase() throws InterruptedException{
        Cursor pictureCursor = pictureDB.getAllPictures();

        //Empty DB
        if(pictureCursor.getCount() == 0){
            return;
        }

        pictureCursor.moveToFirst();
        do{
            PictureObject po = PictureDB.toPictureObject(pictureCursor);
            hash_mutex.acquire();
            picture_hash.addPicture(po);
            hash_mutex.release();
        }while(pictureCursor.moveToNext());
    }

    //Can't only be used for one thread OpenGL
    @Override
    public boolean isPictureListModified(){
        return pictureListModified;
    }

    @Override
    public void setPictureListModified (boolean value){
        pictureListModified = value;
    }

    @Override
    public void closeDataBase() {
        pictureDB.closeDB();
    }

    @Override
    public void startDataBase() {
        pictureDB.initiateDB();
    }

    @Override
    public void createPicture(Location picLocation, double height, float [] picRotation) {
        Bitmap currentBitmap = user_data.getCurrentBitmap();
        String path = user_data.getCurrentBitmapPath();
        savePicture(picLocation, height, path, currentBitmap, picRotation);

    }

    public void savePicture(Location location, double height, String img_path,
                            Bitmap bitmap, float [] picRotation){
        try{
            PictureObject to_save = new PictureObject(
                    user_data.getUser_id(),
                    img_path,
                    location,
                    height,
                    picRotation,
                    user_data.getPixelPerCentimetreRatio());

            //Store picture to DB
            long picture_id = pictureDB.insertPicture(to_save);
            if(picture_id == -1){
                throw new Exception("DB INSERT FAILED");
            }
            to_save.setPicture_id(picture_id);

            //Store picture to Hash
            hash_mutex.acquire();
            picture_hash.addPicture(to_save);
            hash_mutex.release();

            if(image_cache.getCachedImage(img_path) == null ){
                image_cache.cacheImage(img_path, bitmap);
            }

            nimg_mutex.acquire();
            nearestPictures.add(to_save);
            nimg_mutex.release();

            pictureListModified = true;
        }catch(Exception e){
            Log.e("Model", "exception", e);
            if(hash_mutex.availablePermits() <= 0){
                hash_mutex.release();
            }
            if(nimg_mutex.availablePermits() <= 0){
                nimg_mutex.release();
            }
        }
    }

    @Override
    public void deletePicture(PictureObject po){
        try{
            hash_mutex.acquire();
            picture_hash.deletePicture(po);
            hash_mutex.release();
        }catch(Exception e){
            Log.e("Model", "exception", e);
            if(hash_mutex.availablePermits() <= 0){
                hash_mutex.release();
            }
        }
        pictureDB.deletePicture(po);
    }


    private ArrayList<PictureObject> copyList(ArrayList<PictureObject> toCopy) {
        ArrayList<PictureObject> poList = new ArrayList<>();
        for (PictureObject po: toCopy) {
            poList.add(po);
        }
        return poList;
    }

    @Override
    public ArrayList<PictureObject> getImageList(){
        ArrayList<PictureObject> list = null;
        try{
            nimg_mutex.acquire();
            list = copyList(nearestPictures);
            nimg_mutex.release();
        }catch(Exception e){
            Log.e("Model", "exception", e);
            if(nimg_mutex.availablePermits() <= 0){
                nimg_mutex.release();
            }
        }
        return list;
    }

    private Point getUserLocationBox(){
        Location user_location = getUserCurrentLocation();
        return HashPictureBox.getPictureBoxPosition(user_location);
    }

    private ArrayList<PictureObject> appendPicturesFromBox(ArrayList<PictureObject> pictures, PictureBox box ){
        box.initiateIterator();
        PictureObject actual = box.getNextPicture();
        while(actual != null){
            pictures.add(actual);
            actual = box.getNextPicture();
        }
        return pictures;
    }

    private void loadPicturesFromBoxInCache(PictureBox box){
        box.initiateIterator();
        PictureObject actual = box.getNextPicture();
        while(actual != null){
            Bitmap actual_bitmap = image_cache.getCachedImage(actual.getImage_path());
            if( actual_bitmap == null){
                    actual_bitmap = FileIO.openImageBitmap(actual.getImage_path());
                    if(actual_bitmap != null){
                        image_cache.cacheImage(actual.getImage_path(), actual_bitmap);
                    }
            }
            actual = box.getNextPicture();
        }
    }

    //Update pictures cache and create a new NearestPictures List
    @Override
    public Point loadNearBoxes(){
        Point user_box = getUserLocationBox();
        ArrayList<PictureObject> newNearestPictures = new ArrayList<PictureObject>();
        int init_x_box = user_box.x - PictureBox.BOX_RANGE;
        int init_y_box = user_box.y - PictureBox.BOX_RANGE;
        int final_x_box = user_box.x + PictureBox.BOX_RANGE;
        int final_y_box = user_box.y + PictureBox.BOX_RANGE;

        for (int x= init_x_box; x <= final_x_box ; x++){
            for (int y = init_y_box; y <= final_y_box ; y++){
                PictureBox actual_box = picture_hash.getPictureBox(x, y);
                if(actual_box != null){
                    loadPicturesFromBoxInCache(actual_box);
                    newNearestPictures = appendPicturesFromBox(newNearestPictures, actual_box);
                }
            }
        }
        oldNearestPictures = nearestPictures;

        try{
            nimg_mutex.acquire();
            nearestPictures = newNearestPictures;
            nimg_mutex.release();
        }catch(Exception e){
            Log.e("Model", "exception", e);
            if(nimg_mutex.availablePermits() <= 0){
                nimg_mutex.release();
            }
        }

        pictureListModified = true;

        return user_box;
    }

    @Override
    public void removeFarCacheImagesBitmap(){
        oldNearestPictures.removeAll(nearestPictures);
        Bitmap bitmap;
        for (PictureObject currentPicture : oldNearestPictures) {
            String imgPath = currentPicture.getImage_path();
            bitmap = image_cache.getCachedImage(imgPath);
            if (bitmap != null &&
                ! imageBeingUsed(imgPath) &&
                ! imgPath.equals(user_data.getCurrentBitmapPath())){
                image_cache.deleteCachedImage(imgPath);
                bitmap.recycle();
            }
        }
    }

    private boolean imageBeingUsed(String imgPath){
        for (PictureObject currentPicture : nearestPictures) {
            if (imgPath.equals(currentPicture.getImage_path())){
                return true;
            }
        }
        return false;
    }
    @Override
    public void setUserObserver(Observer o) {
        user_data.setObserver(o);
    }

    @Override
    public void removeUserObserver(Observer o) {
        user_data.removeObserver(o);
    }

    @Override
    public void setCurrentUserBitmap(String path, Bitmap bitmap) {
        user_data.setCurrentBitmap(path, bitmap);
    }

    @Override
    public double getUserHeight() {
        return user_data.getUserHeight();
    }

    @Override
    public void setUserHeight(double height) {
        user_data.setUserHeight(height);
    }

    @Override
    public void cleanDataBase() {
        pictureDB.cleanDB();
    }

    @Override
    public void cleanPictureHash() {
        try{
            hash_mutex.acquire();
            picture_hash.clean();
            hash_mutex.release();
        }catch(Exception e){
            Log.e("Model", "exception", e);
            if(hash_mutex.availablePermits() <= 0){
                hash_mutex.release();
            }
        }
    }

    @Override
    public void cleanPictureList() {
        oldNearestPictures.clear();

        try{
            nimg_mutex.acquire();
            nearestPictures = new ArrayList<>();
            nimg_mutex.release();
        }catch(Exception e){
            Log.e("Model", "exception", e);
            if(nimg_mutex.availablePermits() <= 0){
                nimg_mutex.release();
            }
        }

        pictureListModified = true;
    }

    @Override
    public Location getUserCurrentLocation() {
        return user_data.getUser_location();
    }

    @Override
    public Quaternion getUserRotation() {
        return user_data.getRotation();
    }

    @Override
    public void setUserCurrentLocation(Location location) {
        user_data.setUser_location(location);
    }

    @Override
    public void setUserRotation(Quaternion newRotation) {
        user_data.setRotation(newRotation);
    }

    @Override
    public Bitmap getCurrentBitmap(){
        return user_data.getCurrentBitmap();
    }

    @Override
    public double getImageCreationDistance(){
        return user_data.getImageCreationDistance();
    }

    @Override
    public Bitmap getImageBitmap(PictureObject po){
        String imgPath = po.getImage_path();
        Bitmap bitmap = image_cache.getCachedImage(imgPath);
        if (bitmap == null){
            bitmap = image_cache.getCachedImage(ImageCache.NOT_FOUND_IMAGE);
        }
        return bitmap;
    }

    @Override
    public Location getImageLocation(PictureObject po){
        return  po.getLocation();
    }

    @Override
    public double getHeight(PictureObject po){
        return  po.getHeight();
    }

    @Override
    public float [] getRotation(PictureObject po){
        return  po.getImageRotation();
    }


    @Override
    public void setImageCreationDistance(double newDistance){
        user_data.setImageCreationDistance(newDistance);
    }
    @Override
    public void setImageRemovalDistance(double newDistance){
        user_data.setImageRemovalDistance(newDistance);
    }

    @Override
    public double getImageRemovalDistance(){
        return user_data.getImageRemovalDistance();
    }

    @Override
    public float getPixelPerCentimeterRatio() {
        return user_data.getPixelPerCentimetreRatio();
    }

    @Override
    public float getPixelPerCentimeterRatio(PictureObject po) {
        return po.getPixel_ratio();
    }

    @Override
    public void setPixelPerCentimeterRatio(float newRatio) {
        user_data.setPixelPerCentimetreRatio(newRatio);
    }

    @Override
    public GPS_MODE getCurrentGPSMode() {
        return user_data.getCurrentGPSMode();
    }

    @Override
    public void setCurrentGPSMode(GPS_MODE currentMode) {
        user_data.setCurrentGPSMode(currentMode);
    }
}
