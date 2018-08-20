package uib.tfg.project.model.Data;

import android.graphics.Point;
import android.location.Location;

import java.util.HashMap;

public class HashPictureBox {

    //Tamaño de cada una de las cajas del hash
    private final double BOX_METERS = 30;
    //This location decimal is about 1,1 meters
    private final double METER_CORRELATION = 0.00001;
    private final int INITIAL_HASH_DT_SIZE = 500;
    HashMap<String,PictureBox> box_hash;

    public HashPictureBox(){
        box_hash = new HashMap<>();
    }

    public void addPicture(PictureObject po){
        //Obtenemos la posicion de la cuadricula relativa
        Point box_position = getPictureBoxPosition(po);
        //buscamos en el hash la PictureBox que contendra la imagen
        PictureBox box = box_hash.get(getHashKey(box_position));
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
        Point box_position = getPictureBoxPosition(po);
        //buscamos en el hash la PictureBox con la imagen
        PictureBox box = box_hash.get(getHashKey(box_position));
        if(box != null){
            deleted = box.deletePicture(po.getUser_id(), po.getPicture_id());
            if(box.isEmpty()) box_hash.remove(getHashKey(box));
        }
        return deleted;
    }

    private Point getPictureBoxPosition(PictureObject po){
        Location po_location = po.getLocation();
        int x_position = (int)((double)po_location.getLatitude()/(BOX_METERS*METER_CORRELATION));
        int y_position = (int)((double)po_location.getLongitude()/(BOX_METERS*METER_CORRELATION));
        return new Point(x_position,y_position);
    }

    private String getHashKey(Point p){
        return "pictureHashKey-"+p.x+"-"+p.y;
    }

    private String getHashKey(PictureBox pb){
        return "pictureHashKey-"+pb.getX_Coordinate()+"-"+pb.getY_Coordinate();
    }
}