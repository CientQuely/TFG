package uib.tfg.project.model;

import android.location.Location;

import uib.tfg.project.model.Data.DB_Config;
import uib.tfg.project.model.Data.HashPictureBox;
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
    public ProjectModel(Presenter p){
        this.presenter = p;
        picture_hash = new HashPictureBox();
        user_data = new UserData(1);
    }

    public void loadDataBase(){
        //Crea o carga la configuración de la base de datos
        db_config = (DB_Config) FileIO.loadDB_Config(DB_NAME);
        if(db_config == null){
            db_config = new DB_Config();
        }

        //En caso de no estar vacia carga el fichero de la
        //base de datos en el hash
        if(db_config.getDB_SIZE() == 0) return;

        for(Object actual: new FileIO.DBFileIterator(DB_NAME,db_config.getDB_SIZE())){
            picture_hash.addPicture((PictureObject)actual);
        }
    }

    private void closeDataBase(){
       if(db_config != null) FileIO.storeDB_Config(DB_NAME,db_config);
        ///TO DO
    }

    public void savePicture(Location location, double height, String img_path){
        PictureObject to_save = new PictureObject(
                user_data.getUser_id(),
                db_config.getLAST_PICTURE_ID(),
                img_path,
                location, height);
        picture_hash.addPicture(to_save);
        db_config.addToDBCount();
        db_config.increment_LAST_PICTURE_ID();
    }

    public void deletePicture(PictureObject po){
        if (picture_hash.deletePicture(po)){
            db_config.substractFromDBCount();
        }
    }

    public void deleteUser(float user_id){
        if (!db_config.removeUser(user_id)) return;

        //picture_hash.deleteUserPictures();
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
