package uib.tfg.project.model.Data;

import android.graphics.Point;
import android.location.Location;

import java.util.HashMap;
import java.util.Iterator;

public class HashPictureBox {

    //Tamaño de cada una de las cajas del hash
    private static final double BOX_METERS = 30;
    //This location decimal is about 1,1 meters
    private static final double METER_CORRELATION = 0.00001;
    private final int INITIAL_HASH_DT_SIZE = 500;
    HashMap<String,PictureBox> box_hash;

    private Iterator<HashMap.Entry<String, PictureBox>> iterator;
    public HashPictureBox(){
        box_hash = new HashMap<>();
    }

    public void addPicture(PictureObject po){
        //Obtenemos la posicion de la cuadricula relativa
        Point box_position = getPictureBoxPosition(po.getLocation());
        //buscamos en el hash la PictureBox que contendra la imagen
        PictureBox box = box_hash.get(getHashKey(box_position.x, box_position.y));
        if(box == null){
            //Si no existe la creamos
            box = new PictureBox(box_position.x,box_position.y);
            box_hash.put(getHashKey(box),box);
        }
        //Añadimos el PictureObject
        box.addPicture(po);
    }

    public boolean deletePicture(PictureObject po){
        boolean deleted = false;
        //Obtenemos la posicion de la cuadricula relativa
        Point box_position = getPictureBoxPosition(po.getLocation());
        //buscamos en el hash la PictureBox con la imagen
        PictureBox box = box_hash.get(getHashKey(box_position.x, box_position.y));
        if(box != null){
            deleted = box.deletePicture(po.getUser_id(), po.getPicture_id());
            if(box.isEmpty()) box_hash.remove(getHashKey(box));
        }
        return deleted;
    }

    public static Point getPictureBoxPosition(Location location){
        if(location == null) return new Point(-1,-1);
        int x_position = (int)((float)location.getLatitude()/(BOX_METERS*METER_CORRELATION));
        int y_position = (int)((float)location.getLongitude()/(BOX_METERS*METER_CORRELATION));
        return new Point(x_position,y_position);
    }

    private String getHashKey(int x, int y){
        return "pictureHashKey-"+x+"-"+y;
    }

    private String getHashKey(PictureBox pb){
        return "pictureHashKey-"+(int)pb.getX_Coordinate()+"-"+(int)pb.getY_Coordinate();
    }

    public void initiateIterator () {
        iterator =  box_hash.entrySet().iterator();
    }

    public PictureBox getNextPictureBox(){
        if(iterator != null && iterator.hasNext()){
            return iterator.next().getValue();
        }
        return null;
    }

    public PictureBox getPictureBox(int x, int y){
        return box_hash.get(getHashKey(x,y));
    }


    public static boolean pictureUserBoxChanged(Location old_user_location,
                                                Location new_user_location) {
        Point old_box = getPictureBoxPosition(old_user_location);
        Point new_box = getPictureBoxPosition(new_user_location);

        return !old_box.equals(new_box);
    }

    public void clean(){
        box_hash.clear();
    }
}
