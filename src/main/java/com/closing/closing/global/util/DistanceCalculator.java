package com.closing.closing.global.util;

import java.math.BigDecimal;

public final class DistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0088;

    private DistanceCalculator() {
    }

    public static Double calculateKm(
            BigDecimal userLatitude,
            BigDecimal userLongitude,
            BigDecimal productLatitude,
            BigDecimal productLongitude
    ) {
        if (userLatitude == null
                || userLongitude == null
                || productLatitude == null
                || productLongitude == null) {
            return null;
        }

        double userLat = Math.toRadians(userLatitude.doubleValue());
        double productLat = Math.toRadians(productLatitude.doubleValue());

        double latitudeDifference =
                Math.toRadians(
                        productLatitude.doubleValue()
                                - userLatitude.doubleValue()
                );

        double longitudeDifference =
                Math.toRadians(
                        productLongitude.doubleValue()
                                - userLongitude.doubleValue()
                );

        double a =
                Math.sin(latitudeDifference / 2)
                        * Math.sin(latitudeDifference / 2)
                        + Math.cos(userLat)
                        * Math.cos(productLat)
                        * Math.sin(longitudeDifference / 2)
                        * Math.sin(longitudeDifference / 2);

        double c =
                2 * Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1 - a)
                );

        double distanceKm = EARTH_RADIUS_KM * c;

        // 소수점 첫째 자리까지 반환
        return Math.round(distanceKm * 10.0) / 10.0;
    }
}
