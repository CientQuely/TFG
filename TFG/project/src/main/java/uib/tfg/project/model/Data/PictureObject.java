package uib.tfg.project.model.Data;

import android.location.Location;

public class PictureObject {
    private volatile long picture_id;
    private volatile float user_id;
    private volatile String image_path;
    private volatile float [] rotationMatrix;
    private volatile Location location;
    //In meters
    private volatile double height;
    private static final String SEPARATOR = "%";

    private float pixel_ratio;

    public PictureObject(float user_id, String img_path, Location img_location,
                         double height, float [] rotation, float pixel_ratio){
            this.image_path = img_path;
            this.location = img_location;
            this.height = height;
            this.user_id = user_id;
            this.rotationMatrix = rotation;
            this.pixel_ratio = pixel_ratio;
    }

    // Radiants
    public float [] getImageRotation(){
        return rotationMatrix;
    }

    public long getPicture_id() {
        return picture_id;
    }

    public void setPicture_id(long picture_id) {
        this.picture_id = picture_id;
    }

    public String getImage_path() {
        return image_path;
    }

    public float getUser_id() {
        return user_id;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }


    public Location getLocation() {
        return location;
    }

    public double getHeight() {
        return height;
    }

    public boolean isThisPicture(float user_id, float picture_id){
        return user_id==this.user_id && picture_id==this.picture_id;
    }

    @Override
    public String toString(){
        return String.format("Picture ID: %2d, Location: %s .",picture_id, location.toString());
    }

    public String getLocationString() {
        return  location.getLatitude()+SEPARATOR+location.getLongitude()+SEPARATOR+height;
    }

    public static double [] parseQueryString(String queryString) {
        String [] values = queryString.split(SEPARATOR);

        double [] doubleValues = new double [values.length];

        for(int i = 0; i < values.length; i++){
            doubleValues[i] = Double.parseDouble(values[i]);
        }
        return doubleValues;
    }

    public static float [] parseQueryStringFloat(String queryString) {
        String [] values = queryString.split(SEPARATOR);

        float [] doubleValues = new float [values.length];

        for(int i = 0; i < values.length; i++){
            doubleValues[i] = Float.parseFloat(values[i]);
        }
        return doubleValues;
    }
    public String getRotationString() {
        String s = "";
        for (int i = 0; i < rotationMatrix.length; i++) {
            s += rotationMatrix[i] + SEPARATOR;
        }
        if(s.length() > 0){
            return s.substring(0, s.length() - 1);
        }
        return "-1";
    }


    public float getPixel_ratio() {
        return pixel_ratio;
    }

    public void setPixel_ratio(float pixel_ratio) {
        this.pixel_ratio = pixel_ratio;
    }

}
