package uib.tfg.project.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Picture;
import android.graphics.Point;
import android.location.Location;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

import uib.tfg.project.model.Data.DB_Config;
import uib.tfg.project.model.Data.HashPictureBox;
import uib.tfg.project.model.Data.ImageCache;
import uib.tfg.project.model.Data.PictureBox;
import uib.tfg.project.model.Data.PictureObject;
import uib.tfg.project.model.Data.UserData;
import uib.tfg.project.presenter.Presenter;

/**
 * Created by Micky on 15/03/2018.
 */

public class ProjectModel implements Model{
    public static final String DB_NAME = "picture_database_one";
    private Presenter presenter;
    UserData user_data;
    DB_Config db_config;
    private HashPictureBox picture_hash;
    private ImageCache image_cache;
    protected Semaphore db_mutex = new Semaphore(1);
    protected Semaphore hash_mutex = new Semaphore(1);

    public ProjectModel(Presenter p, Bitmap not_found_img) throws ModelException.Bitmap_Not_Found_Exception {
        this.presenter = p;
        picture_hash = new HashPictureBox();
        user_data = new UserData(1);
        image_cache = new ImageCache(not_found_img);
    }
    @Override
    public void loadDataBase() throws ModelException.DB_Config_Exception, ModelException.DB_File_Exception {
        try {
            db_mutex.acquire();
        } catch (InterruptedException e) {
        }
        try {
            db_config = (DB_Config) FileIO.loadDB_Config(DB_NAME);
        } catch (IOException e) {
            throw new ModelException.DB_Config_Exception("DB Config not loaded");
        }
        if(db_config == null){
            db_config = new DB_Config();
        }

        //En caso de no estar vacia carga el fichero de la
        //base de datos en el hash
        if(db_config.getDB_SIZE() == 0) return;

        //Read database
        try {
            for(Object actual: new FileIO.DBFileIterator(DB_NAME,db_config.getDB_SIZE())){
                picture_hash.addPicture((PictureObject)actual);
            }
        } catch (IOException e) {
            throw new ModelException.DB_File_Exception("DB File not loaded");
        }
        db_mutex.release();
    }


    @Override
    public void closeDataBase() throws ModelException.DB_Config_Exception, ModelException.DB_File_Exception {
        //Store DB Configuration
        try {
            FileIO.storeDB_Config(DB_NAME,db_config);
        } catch (IOException e) {
            throw new ModelException.DB_Config_Exception("DB File not loaded");
        }
        //Store DB
        FileIO.DBStoreSession db_output = null;
        try {
            db_output = new FileIO.DBStoreSession(DB_NAME);
        } catch (IOException e) {
            throw new ModelException.DB_File_Exception("DB File not loaded");
        }
        try {
            db_mutex.acquire();
            picture_hash.initiateIterator();
            PictureBox current_box = picture_hash.getNextPictureBox();
            while(current_box != null){
                current_box.initiateIterator();
                PictureObject actual = current_box.getNextPicture();
                while(actual != null){
                    db_output.storeDBObject(actual);
                    actual = current_box.getNextPicture();
                }
                current_box = picture_hash.getNextPictureBox();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally{
            db_output.closeDBStoreSession();
            db_mutex.release();
        }
    }


    @Override
    public void savePicture(Location location, double height, String img_path) throws InterruptedException {
        hash_mutex.acquire();
        PictureObject to_save = new PictureObject(
                user_data.getUser_id(),
                db_config.getLAST_PICTURE_ID(),
                img_path,
                location, height);
        picture_hash.addPicture(to_save);
        db_config.addToDBCount();
        db_config.increment_LAST_PICTURE_ID();
        hash_mutex.release();
    }

    @Override
    public void deletePicture(PictureObject po) throws InterruptedException {
        hash_mutex.acquire();
        if (picture_hash.deletePicture(po)){
            db_config.substractFromDBCount();
        }
        hash_mutex.release();
    }


    public void deleteUser(float user_id){
        if (!db_config.removeUser(user_id)) return;

        //picture_hash.deleteUserPictures();
    }


    private Point getUserLocationBox(){
        Location user_location = getUserCurrentLocation();
        return HashPictureBox.getPictureBoxPosition(user_location);
    }


    public void loadPicturesFromBox(PictureBox box){
        box.initiateIterator();
        PictureObject actual = box.getNextPicture();
        while(actual != null){
            Bitmap actual_bitmap = image_cache.getCachedImage(actual.getImage_path());
            if( actual_bitmap == null){
                    actual_bitmap = FileIO.openImageBitmap(actual.getImage_path());
                    if(actual_bitmap == null){
                        actual_bitmap = image_cache.getCachedImage(ImageCache.NOT_FOUND_IMAGE);
                    }
                    image_cache.cacheImage(actual.getImage_path(), actual_bitmap);
            }
            actual.setBitmap(actual_bitmap);
            actual = box.getNextPicture();
        }
    }

    @Override
    public Point loadNearBoxes(){
        Point user_box = getUserLocationBox();
        int init_x_box = user_box.x - PictureBox.BOX_RANGE;
        int init_y_box = user_box.y - PictureBox.BOX_RANGE;
        int final_x_box = user_box.x + PictureBox.BOX_RANGE;
        int final_y_box = user_box.y + PictureBox.BOX_RANGE;
        for (int x= init_x_box; x<final_x_box; x++){
            for (int y= init_y_box; y<final_y_box; y++){
                PictureBox actual_box = picture_hash.getPictureBox(x, y);
                if(actual_box != null){
                    loadPicturesFromBox(actual_box);
                }
            }
        }
        return user_box;
    }
    @Override
    public Location getUserCurrentLocation() {
        return user_data.getUser_location();
    }

    @Override
    public float[] getUserRotation() {
        return user_data.get_Rotation();
    }

    @Override
    public float[] getUserAcceleration() {
        return user_data.get_Acceleration();
    }

    @Override
    public void setUserCurrentLocation(Location location) {
        user_data.setUser_location(location);
    }

    @Override
    public void setUserAcceleration(float[] new_acceleration) {
        user_data.setUser_acceleration(new_acceleration);
    }

    @Override
    public void setUserRotation(float[] new_rotation) {
        user_data.setUser_rotation(new_rotation);
    }
}
