package com.localapp.util;

public class DistanceUtil {

    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1), dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) *
                   Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 3958.8 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public static int estimateDrive(double miles) {
        return Math.max(2, (int) Math.round(miles / 25.0 * 60));
    }

    public static int estimateWalk(double miles) {
        return Math.max(1, (int) Math.round(miles / 3.0 * 60));
    }
}

