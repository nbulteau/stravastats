package me.nicolas.stravastats.core.csv

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Hike
import me.nicolas.stravastats.core.formatDate
import me.nicolas.stravastats.core.formatSeconds
import me.nicolas.stravastats.core.statistics.calculateBestDistanceForTime
import me.nicolas.stravastats.core.statistics.calculateBestTimeForDistance
import java.io.FileWriter

internal class HikeCSVExporter(activities: List<Activity>) : CSVExporter(activities, Hike) {

    override fun generateHeader(writer: FileWriter) {
        listOf(
            "Date",
            "Description",
            "Distance (km)",
            "Time",
            "Time (seconds)",
            "Speed (km/h)",
            "Elevation (m)",
            "Highest point (m)",
            "Best 200m (km/h)",
            "Best 400m (km/h)",
            "Best 1000m (km/h)",
            "Best 10000m (km/h)",
            "Best 1 h (km/h)",
        ).writeCSVLine(writer)
    }

    override fun generateActivities(writer: FileWriter) {

        activities.forEach { activity ->

            listOf(
                activity.startDateLocal.formatDate(),
                activity.name.trim(),
                "%.02f".format(activity.distance / 1000),
                activity.elapsedTime.formatSeconds(),
                "%d".format(activity.elapsedTime),
                activity.getSpeed(),
                "%.0f".format(activity.totalElevationGain),
                "%.0f".format(activity.elevHigh),
                activity.calculateBestTimeForDistance(200.0)?.getSpeed() ?: "",
                activity.calculateBestTimeForDistance(400.0)?.getSpeed() ?: "",
                activity.calculateBestTimeForDistance(1000.0)?.getSpeed() ?: "",
                activity.calculateBestTimeForDistance(10000.0)?.getSpeed() ?: "",
                activity.calculateBestDistanceForTime(60 * 60)?.getSpeed() ?: "",
            ).writeCSVLine(writer)
        }
    }

    override fun generateFooter(writer: FileWriter) {
        val lastRow = activities.size + 1
        listOf(
            ";;" +
                    "=SOMME(\$C2:\$C$lastRow);;" +
                    "=SOMME(\$E2:\$E$lastRow);" +
                    "=MAX(\$F2:\$F$lastRow);" +
                    "=MAX(\$G2:\$G$lastRow);" +
                    "=MAX(\$H2:\$H$lastRow);" +
                    "=MAX(\$I2:\$I$lastRow);" +
                    "=MAX(\$J2:\$J$lastRow);" +
                    "=MAX(\$K2:\$K$lastRow);" +
                    "=MAX(\$L2:\$L$lastRow);" +
                    "=MAX(\$M2:\$M$lastRow)"
        ).writeCSVLine(writer)
    }
}