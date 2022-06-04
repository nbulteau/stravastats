package me.nicolas.stravastats.service

import me.nicolas.stravastats.service.srtm.Point
import me.nicolas.stravastats.service.srtm.SRTMFile
import java.io.File
import java.nio.file.Path
import kotlin.math.abs
import kotlin.math.floor


/**
 * Get elevation using Shuttle Radar Topography Mission (SRTM) level 1 files
 */
class SRTMService(private val cachePath: Path) {

    private val srtmTilescache = mutableMapOf<String, SRTMFile>()

    fun getElevation(latitudeLongitudeList: List<List<Double>>): List<Double> {

        return latitudeLongitudeList.map { latlong ->
            if (latlong[0] == 0.0 && latlong[1] == 0.0) {
                0.0
            } else {
                getElevation(latlong[0], latlong[1])
            }
        }
    }

    private fun getElevation(latitude: Double, longitude: Double): Double {

        // get SRTM tile
        val srtmFileName = getTileFileName(latitude, longitude)
        val srtmFile: SRTMFile
        if (srtmTilescache.contains(srtmFileName)) {
            srtmFile = srtmTilescache[srtmFileName]!!
        } else {
            srtmFile = SRTMFile(File(cachePath.toFile(), "$srtmFileName.hgt"))
            srtmTilescache[srtmFileName] = srtmFile
        }

        val point = Point(latitude, longitude)
        return if (srtmFile.contains(point)) {
            srtmFile.getElevation(point).elevation
        } else {
            0.0
        }
    }

    private fun getTileFileName(lat: Double, lng: Double): String {
        val latitude = lat.toDegreesMinutesAndSeconds().first
        val latitudeCardinal = if (lat >= 0) "N" else "S"

        val longitude = lng.toDegreesMinutesAndSeconds().first + 1
        val longitudeCardinal = if (lng >= 0) "E" else "W"

        return "$latitudeCardinal$latitude$longitudeCardinal${longitude.toString().padStart(3, '0')}"
    }

    private fun Double.toDegreesMinutesAndSeconds(): Triple<Int, Int, Int> {
        val absolute = abs(this)
        val degrees = floor(absolute)
        val minutesNotTruncated = (absolute - degrees) * 60
        val minutes = floor(minutesNotTruncated)
        val seconds = floor((minutesNotTruncated - minutes) * 60)

        return Triple(degrees.toInt(), minutes.toInt(), seconds.toInt())
    }
}
