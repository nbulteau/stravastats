package me.nicolas.stravastats.ihm

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import me.nicolas.stravastats.MyStravaStatsApp
import me.nicolas.stravastats.MyStravaStatsProperties
import me.nicolas.stravastats.business.Athlete
import me.nicolas.stravastats.service.StatisticsService
import me.nicolas.stravastats.service.StravaService
import me.nicolas.stravastats.service.statistics.ActivityStatistic
import me.nicolas.stravastats.service.statistics.Statistic
import me.nicolas.stravastats.strava.StravaApi
import tornadofx.Controller

class MainController : Controller() {

    private val myStravaStatsProperties = loadPropertiesFromFile()

    private val stravaService = StravaService(StravaApi(myStravaStatsProperties))

    private val statsService = StatisticsService()

    private val activities = stravaService.loadActivities(
        MyStravaStatsApp.myStravaStatsParameters.clientId,
        MyStravaStatsApp.myStravaStatsParameters.clientSecret,
        MyStravaStatsApp.myStravaStatsParameters.year
    )

    fun getLoggedInAthlete(): Athlete? {
        return stravaService.getLoggedInAthlete(
            MyStravaStatsApp.myStravaStatsParameters.clientId,
            MyStravaStatsApp.myStravaStatsParameters.clientSecret
        )
    }

    fun getStatisticsToDisplay(year: Int?): StatisticsToDisplay {
        val stravaStatistics = statsService.computeStatistics(activities.filter { activity ->
            activity.startDateLocal.subSequence(0, 4).toString().toInt() == year
        })
        return StatisticsToDisplay(
            buildStatisticsToDisplay(stravaStatistics.globalStatistics),
            buildStatisticsToDisplay(stravaStatistics.sportRideStatistics),
            buildStatisticsToDisplay(stravaStatistics.commuteRideStatistics),
            buildStatisticsToDisplay(stravaStatistics.runStatistics),
            buildStatisticsToDisplay(stravaStatistics.inlineSkateStats),
            buildStatisticsToDisplay(stravaStatistics.hikeStatistics)
        )
    }

    private fun buildStatisticsToDisplay(statistics: List<Statistic>): ObservableList<StatisticDisplay> {
        val activityStatistics = statistics.map { statistic ->
            when (statistic) {
                is ActivityStatistic -> {
                    StatisticDisplay(
                        statistic.name,
                        statistic.value,
                        if (statistic.activity != null) statistic.activity.toString() else ""
                    )
                }
                else -> StatisticDisplay(statistic.name, statistic.toString(), "")
            }
        }
        return FXCollections.observableArrayList(activityStatistics)
    }


    /**
     * Load properties from application.yml
     */
    private fun loadPropertiesFromFile(): MyStravaStatsProperties {

        val mapper = ObjectMapper(YAMLFactory()) // Enable YAML parsing
        mapper.registerModule(KotlinModule()) // Enable Kotlin support

        val inputStream = javaClass.getResourceAsStream("/application.yml")
        return mapper.readValue(inputStream, MyStravaStatsProperties::class.java)
    }
}
