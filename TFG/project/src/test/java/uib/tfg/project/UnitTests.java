package uib.tfg.project;

import android.location.Location;

import org.junit.Test;

import uib.tfg.project.model.representation.Matrix;
import uib.tfg.project.presenter.LocationService;

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

}