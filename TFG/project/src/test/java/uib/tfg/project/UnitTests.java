package uib.tfg.project;

import android.location.Location;

import org.junit.Test;

import uib.tfg.project.model.representation.Matrix;
import uib.tfg.project.model.representation.Quaternion;
import uib.tfg.project.presenter.LocationService;
import uib.tfg.project.view.VirtualCameraView;

import static org.junit.Assert.assertEquals;

public class UnitTests {
    @Test
    public void locationMetersConversion(){
        double latitude = 39.720577;
        double longitude = 2.917275;
        double [] meters = locationToMeters(latitude, longitude);
        double [] angle = metersToLocation(meters[0], meters[1]);

        assertEquals(latitude, angle[0], 0.0000001);
        assertEquals(longitude, angle[1], 0.00000001);
    }


    private static double earthRadius = 6378137;
    private static double degreePerMeter = (1 / ((2 * Math.PI / 360) * earthRadius));

    public static double[] metersToLocation(double latitude, double longitude){
        double [] latAndLong = new double [2];
        latAndLong[0] = latitude * degreePerMeter;
        latAndLong[1] = longitude * degreePerMeter / Math.cos(latitude * degreePerMeter * Math.PI / 180);
        return latAndLong;
    }

    public static double [] locationToMeters(double lat, double longi){
        double [] latAndLong = new double [2];
        latAndLong[0] = lat / degreePerMeter;
        latAndLong[1] = longi / degreePerMeter*Math.cos(lat * Math.PI / 180);
        return latAndLong;
    }

    @Test
    public void relativeLocation(){
        Quaternion q = new Quaternion();
        q.setXYZW(0, 0,0.7071203316249954f,  0.7071203316249954f);

        double [] vector = {-2, 0, 0};
        vector = q.rotateVector(vector);

        System.out.println("X: "+ vector[0] + " ,Y: "+ vector[1] + " ,Z: "+ vector[2]);
        assertEquals(1,2);

    }

    @Test
    public void relativeLocationWithQuat(){

        Quaternion point = new Quaternion();
        point.setXYZW(-1,0,0,0);

        Quaternion q = new Quaternion();
        q.setXYZW(0, 0, 0.7071203316249954f, 0.7071203316249954f);

        Quaternion qXpoint = new Quaternion();

        q.multiplyByQuat(point, qXpoint);

        Quaternion p = new Quaternion();
        p.setXYZW(-q.getX(), -q.getY(), -q.getZ(), q.getW());

        Quaternion qXpointXp = new Quaternion();
        qXpoint.multiplyByQuat(p, qXpointXp);

        System.out.println("X: "+ qXpointXp.getX() + " ,Y: "+ qXpointXp.getY() + " ,Z: "+ qXpointXp.getZ());
        assertEquals(1,2);

    }

}