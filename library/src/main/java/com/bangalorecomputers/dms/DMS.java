package com.bangalorecomputers.dms;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

public class DMS {
    public String direction= "N";
    public int degree= 0;
    public int minute= 0;
    public float second= 0;
    public DMS() {

    }
    public DMS(String direction, int degree, int minute, float second) {
        this.direction= direction;
        this.degree= degree;
        this.minute= minute;
        this.second= second;
    }



    public static String parseDDtoDMS(double latitude, double longitude) {
        return parseDDtoDMS(latitude, "NS") + ", " + parseDDtoDMS(longitude, "EW");
    }
    public static String[] parseDDtoDMSList(double latitude, double longitude) {
        return new String[] { parseDDtoDMS(latitude, "NS"), parseDDtoDMS(longitude, "EW") };
    }

    public static DMS[] parseDDtoDMSObjList(double latitude, double longitude) {
        return new DMS[] { parseDDtoDMSObj(latitude, "NS"), parseDDtoDMSObj(longitude, "EW") };
    }
    public static String parseDDtoDMS(double latlong, String directions) {
        StringBuilder builder = new StringBuilder();

        DMS dmsObj= parseDDtoDMSObj(latlong, directions);

        builder.append(dmsObj.direction).append(" ")
                .append(dmsObj.degree).append("°")
                .append(dmsObj.minute).append("'")
                .append(String.format(Locale.getDefault(), "%.1f", dmsObj.second)).append("''");

        return builder.toString();
    }
    public static DMS parseDDtoDMSObj(double latlong, String directions) {
        DMS dmsObj= new DMS();

        if (latlong < 0) {
            dmsObj.direction= directions.substring(1, 2);
        } else {
            dmsObj.direction= directions.substring(0, 1);
        }

        String inDegrees = Location.convert(Math.abs(latlong), Location.FORMAT_SECONDS);
        String[] latitudeSplit = inDegrees.split(":");
        dmsObj.degree= Integer.parseInt(latitudeSplit[0]);
        dmsObj.minute= Integer.parseInt(latitudeSplit[1]);
        dmsObj.second= Float.parseFloat(latitudeSplit[2]);

        return dmsObj;
    }



    public static LatLng parseDMStoDD(String input) {
        String[] parts = input.replaceAll("(?=[NSEW]|[nsew]+).", " $0 ").trim().split("[^\\d\\w\\.]+");
        if(parts.length == 8) {
            int dir= "N".equalsIgnoreCase(parts[0])||"S".equalsIgnoreCase(parts[0])?0:3;
            int start= dir==0?1:0;
            double lat= convertDMSToDD(Double.parseDouble(parts[start]), Double.parseDouble(parts[start+1]),
                    Double.parseDouble(parts[start+2]), parts[dir]);

            dir= "E".equalsIgnoreCase(parts[4])||"W".equalsIgnoreCase(parts[4])?4:7;
            start= dir==4?5:4;
            double lng= convertDMSToDD(Double.parseDouble(parts[start]), Double.parseDouble(parts[start+1]),
                    Double.parseDouble(parts[start+2]), parts[dir]);
            return new LatLng(lat, lng);
        } else {
            return null;
        }
    }
    public static double convertDMSToDD(double degrees, double minutes, double seconds, String direction) {
        double dd = degrees + (minutes/60) + (seconds/(60*60));
        if ("S".equalsIgnoreCase(direction) || "W".equalsIgnoreCase(direction)) {
            dd = dd * -1;
        }
        return dd;
    }
}
