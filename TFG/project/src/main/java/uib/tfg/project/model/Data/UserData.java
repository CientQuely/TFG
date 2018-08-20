package uib.tfg.project.model.Data;

import android.location.Location;
import java.util.Vector;

public class UserData {
    private float user_id;
    private volatile Location user_location;
    private volatile float [] user_rotation;
    private volatile float [] user_acceleration;
    public final int X_AXIS = 0;
    public final int Y_AXIS = 1;
    public final int Z_AXIS = 2;
    public UserData(float user_id){
        this.user_id = user_id;
        this.user_rotation = new float
                [3];
        this.user_acceleration = new float [3];
        this.user_location = null;
    }
    public float getUser_id() {
        return user_id;
    }

    public void setUser_id(float user_id) {
        this.user_id = user_id;
    }

    public Location getUser_location() {

        return user_location;
    }

    public void setUser_location(Location user_location) {
        this.user_location = user_location;
    }

    public void setUser_rotation(float [] rotation){
        if(rotation.length != 3) return;
            this.user_rotation[X_AXIS] = rotation[X_AXIS];
            this.user_rotation[Y_AXIS] = rotation[Y_AXIS];
            this.user_rotation[Z_AXIS] = rotation[Z_AXIS];
    }

    public void setUser_acceleration(float [] acceleration) {
        if(acceleration.length != 3) return;
        this.user_acceleration[X_AXIS] = acceleration[X_AXIS];
        this.user_acceleration[Y_AXIS] = acceleration[Y_AXIS];
        this.user_acceleration[Z_AXIS] = acceleration[Z_AXIS];
    }

    public float [] get_Rotation(){
        return user_rotation.clone();
    }
    public float [] get_Acceleration(){
        return user_acceleration.clone();
    }

}
