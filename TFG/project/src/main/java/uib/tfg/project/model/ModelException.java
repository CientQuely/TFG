package uib.tfg.project.model;


public class ModelException extends Exception {

    public static class DB_File_Exception extends Exception {
        public DB_File_Exception(String message) {
            super(message);
        }
    }

    public static class DB_Config_Exception extends Exception {
        public DB_Config_Exception(String message) {
            super(message);
        }
    }

    public static class Bitmap_Not_Found_Exception extends Exception {
        public Bitmap_Not_Found_Exception(String message) {
            super(message);
        }
    }



}
