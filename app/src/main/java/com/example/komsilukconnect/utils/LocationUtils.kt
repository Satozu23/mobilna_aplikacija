package com.example.komsilukconnect.utils

import com.google.android.gms.maps.model.LatLng
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

fun calculateDistance(loc1: LatLng, loc2: LatLng): Double {
    val lat1 = Math.toRadians(loc1.latitude)
    val lon1 = Math.toRadians(loc1.longitude)
    val lat2 = Math.toRadians(loc2.latitude)
    val lon2 = Math.toRadians(loc2.longitude)
    val r = 6371.0
    return acos(sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(lon2 - lon1)) * r
}