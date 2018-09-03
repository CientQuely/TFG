package uib.tfg.project.model.Data;

import java.io.Serializable;
import java.util.ArrayList;

public class DB_Config implements Serializable {
    private float DB_SIZE;
    private float USERS_NUMBER;
    private float LAST_USER_ID;
    private float LAST_PICTURE_ID;
    private ArrayList<Float> users_id_list;

    public DB_Config (){
        DB_SIZE = 0;
        USERS_NUMBER = 0;
        LAST_USER_ID = 1;
        LAST_PICTURE_ID = 1;
        users_id_list = new ArrayList<>();
    }

    public void addUser(float user_id){
        users_id_list.add(user_id);
        USERS_NUMBER++;
    }

    public boolean removeUser(float user_id){
        for (float user: users_id_list){
            if(user == user_id){
                users_id_list.remove(user);
                USERS_NUMBER--;
                return true;
            }
        }
        return false;
    }

    public void addToDBCount(){
        DB_SIZE++;
    }
    public void substractFromDBCount(){
        DB_SIZE--;
    }

    public float getDB_SIZE() {
        return DB_SIZE;
    }

    public void restart_DB_SIZE() {
        this.DB_SIZE = 0;
    }

    public void restart_LAST_PICTURE_ID() {
        this.LAST_PICTURE_ID = 0;
    }

    public float getUSERS_NUMBER() {
        return USERS_NUMBER;
    }

    public void setUSERS_NUMBER(float USERS_NUMBER) {
        this.USERS_NUMBER = USERS_NUMBER;
    }

    public float getLAST_USER_ID() {
        return LAST_USER_ID;
    }

    public void increment_LAST_USER_ID() {
        this.LAST_USER_ID++;
    }

    public float getLAST_PICTURE_ID() {
        return LAST_PICTURE_ID;
    }

    public void increment_LAST_PICTURE_ID() {
        this.LAST_PICTURE_ID++;
    }
}
