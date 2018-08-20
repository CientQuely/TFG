package uib.tfg.project.model;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

public class FileIO {
    protected static final String FILE_DIR= "/DATA/";
    protected static final String DATA_BASE_PATH= "%s.db";
    protected static final String DATA_BASE_CONFIG_PATH= "%s.cf";
    protected static final String TAG="model/FileIO";

    protected static Bitmap openImageBitmap(String image_path){
        throw new UnsupportedOperationException();
    }

    /**
     * Elimina un archivo
     * @param file_path
     * @return
     */
    public static boolean deleteFile(String file_path){
        File to_delete = new File(file_path);
        if(!to_delete.exists()) return false;
        if (to_delete.delete()){
            return true;
        }
        return false;
    }

    public static Object loadDB_Config(String db_conf){
        String file_path = String.format(FILE_DIR+DATA_BASE_CONFIG_PATH,db_conf);
        File f = new File(file_path);
        if(!f.exists()) return null;
        FileInputStream fis;
        ObjectInputStream ois;
        try{
            fis = new FileInputStream(file_path);
            ois = new ObjectInputStream(fis);
            Object o =  ois.readObject();
            ois.close();
            fis.close();
        }catch(IOException ioe){
            ioe.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void storeDB_Config(String db_conf, Object o){
        String file_path = String.format(FILE_DIR+DATA_BASE_CONFIG_PATH,db_conf);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file_path,true);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(o);
            oos.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Crea una session de guardado que mantiene el fichero abierto para ir guardando
     * la información de la base de datos a medida que se crea.
     */
    public static class DBStoreSession {
        private FileOutputStream fos;
        private ObjectOutputStream oos;

        public DBStoreSession(String db_name){
            String db_dir = String.format(FILE_DIR+DATA_BASE_PATH,db_name);
            try {
                fos = new FileOutputStream(db_dir,true);
                oos = new ObjectOutputStream(fos);

            } catch (FileNotFoundException e) {
                Log.e(TAG," DBStoreSession Error: File not found exception");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void storeDBObject(Object db_object){
            try {
                oos.writeObject(db_object);
            } catch (IOException e) {
                Log.e(TAG," DBStoreSession Error: Could not save db_object");
                e.printStackTrace();
            }
        }

        public void closeDBStoreSession(){
            try {
                oos.close();
                fos.close();
            } catch (IOException e) {
                Log.e(TAG," DBStoreSession Error: Session could not be closed");
                e.printStackTrace();
            }
        }
    }

    /**
     * Esta clase itera desde un fichero para cargar la base de datos.
     */
    public static class DBFileIterator implements Iterable<Object>{

        private FileInputStream fis = null;
        private ObjectInputStream ois = null;
        private float db_count;
        private float db_size;

        public DBFileIterator(String db_name, float db_size){
            this.db_size = db_size;
            db_count = 0;
            String db_dir = String.format(FILE_DIR+DATA_BASE_PATH,db_name);
            try {
                //Crea el fichero si no existe
                File f = new File(db_dir);
                if(!f.exists()){
                    f.createNewFile();
                }
                FileInputStream fis = new FileInputStream(db_dir);
                ois = new ObjectInputStream(fis);
            } catch (FileNotFoundException fnf){
                Log.e(TAG," DBFileIterator Error: File not found exception");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        protected void finalize() {
            try {
                ois.close();
                fis.close();
            } catch (IOException e) {
                Log.e(TAG," DBFileIterator Error: File could not be closed");
                e.printStackTrace();
            }
        }
        @NonNull
        @Override
        public Iterator<Object> iterator() {
            return new Iterator<Object>(){
                //Cierra los ficheros automaticamente al usar hasNext
                public boolean hasNext() {
                    if(db_count <= db_size){
                        return true;
                    }
                    try {
                        this.finalize();
                    } catch (Throwable throwable) {
                        Log.e(TAG," DBFileIterator Error: Throwable error ocurred");
                        throwable.printStackTrace();
                    }
                    return false;
                }
                public Object next(){
                    try {
                        db_count++;
                        return ois.readObject();
                    } catch (ClassNotFoundException e) {
                        Log.e(TAG," DBFileIterator Error: EOF");
                        return null;
                    } catch (IOException e) {
                        return null;
                    }
                }
                public void remove(){
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

}
