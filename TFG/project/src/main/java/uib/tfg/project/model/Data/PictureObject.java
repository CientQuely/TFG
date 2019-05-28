package uib.tfg.project.model.Data;

import android.location.Location;

public class PictureObject {
    private long picture_id;
    private float user_id;
    private String image_path;
    private double [] imageRotation;
    private Location location;
    //In meters
    private double height;
    private static final String SEPARATOR = "%";

    public PictureObject(float user_id, String img_path, Location img_location,
                         double height, double [] imageRotation){
            this.image_path = img_path;
            this.location = img_location;
            this.height = height;
            this.user_id = user_id;
            this.imageRotation = imageRotation;
    }

    // Radiants
    public double [] getImageRotation(){
        return imageRotation;
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

    public String getRotationString() {
        return  String.valueOf(imageRotation[0])+SEPARATOR+imageRotation[1]+SEPARATOR+imageRotation[2];
    }
}
