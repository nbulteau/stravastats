package me.nicolas.stravastats.ihm

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.chart.XYChart
import javafx.scene.control.Hyperlink
import javafx.scene.image.ImageView
import me.nicolas.stravastats.business.*
import me.nicolas.stravastats.business.badges.Badge
import me.nicolas.stravastats.service.*
import me.nicolas.stravastats.service.statistics.ActivityStatistic
import me.nicolas.stravastats.service.statistics.Statistic
import tornadofx.*
import java.awt.Desktop
import java.net.URI
import kotlin.collections.component1
import kotlin.collections.component2


class MainController(private val clientId: String, private val activities: ObservableList<Activity>) : Controller() {

    private val statsService = StatisticsService()

    private val chartsService = ChartsService()

    private val csvService = CSVService()

    private val badgeService = BadgeService()

    fun generateCSV(year: Int) {
        csvService.exportCSV(clientId, activities, year)
    }

    fun generateCharts(year: Int) {
        chartsService.buildCharts(activities, year)
    }

    fun getActiveDaysByActivityTypeByYear(activityType: String, year: Int): Map<String, Int> {

        val filteredActivities = filterActivitiesByType(activityType)
        return filteredActivities
            .filter { activity -> activity.startDateLocal.subSequence(0, 4).toString().toInt() == year }
            .groupBy { activity -> activity.startDateLocal.substringBefore('T') }
            .mapValues { (_, activities) -> activities.sumOf { activity -> activity.distance / 1000 } }
            .mapValues { entry -> entry.value.toInt() }
            .toMap()
    }

    fun getActiveDaysByActivityType(activityType: String): Map<String, Int> {

        val filteredActivities = filterActivitiesByType(activityType)
        return filteredActivities
            .groupBy { activity -> activity.startDateLocal.substringBefore('T') }
            .mapValues { (_, activities) -> activities.sumOf { activity -> activity.distance / 1000 } }
            .mapValues { entry -> entry.value.toInt() }
            .toMap()
    }

    fun getActivitiesByYear(activityType: String): Map<String, List<Activity>> {

        val filteredActivities = filterActivitiesByType(activityType)

        return ActivityHelper.groupActivitiesByYear(filteredActivities)
    }

    fun getActivitiesToDisplay(activityType: String, year: Int): ObservableList<ActivityDisplay> {

        val filteredActivities = filterActivitiesByType(activityType)
            .filter { activity -> activity.startDateLocal.subSequence(0, 4).toString().toInt() == year }
        return buildActivitiesToDisplay(filteredActivities)
    }

    fun getStatisticsToDisplay(activityType: String, year: Int?): ObservableList<StatisticDisplay> {

        val filteredActivities = filterActivitiesByType(activityType)
            .filter { activity -> activity.startDateLocal.subSequence(0, 4).toString().toInt() == year }
        val statistics = when (activityType) {
            Ride -> statsService.computeRideStatistics(filteredActivities)
            Commute -> statsService.computeCommuteStatistics(filteredActivities)
            Run -> statsService.computeRunStatistics(filteredActivities)
            InlineSkate -> statsService.computeInlineSkateStatistics(filteredActivities)
            Hike -> statsService.computeHikeStatistics(filteredActivities)
            else -> emptyList()
        }

        return buildStatisticsToDisplay(statistics)
    }

    fun getGeneralBadgesToDisplay(activityType: String): ObservableList<BadgeDisplay> {
        val filteredActivities = filterActivitiesByType(activityType)

        val imageUrl = when (activityType) {
            Ride -> "images/racing.png"
            Run -> "images/run.png"
            Commute -> "images/badge.png"

            else -> ""
        }
        val badges = badgeService.computeGeneralBadges(activityType, filteredActivities)
        return buildBadgesToDisplay(badges, imageUrl)
    }

    fun getLocationBadgesToDisplay(activityType: String): ObservableList<BadgeDisplay> {
        val filteredActivities = filterActivitiesByType(activityType)

        val badges: List<Triple<Badge, Activity?, Boolean>>
        val imageUrl: String
        when (activityType) {
            Ride -> {
                badges = badgeService.computeLocationRideBadges(filteredActivities)
                imageUrl = "images/cycling.png"
            }
            else -> {
                badges = emptyList()
                imageUrl = ""
            }
        }
        return buildBadgesToDisplay(badges, imageUrl)
    }

    fun buildDistanceByMonthsSeries(
        activityType: String,
        year: Int,
    ): ObservableList<XYChart.Data<String, Number>> {

        val filteredActivities = filterActivitiesByType(activityType)
            .filter { activity -> activity.startDateLocal.subSequence(0, 4).toString().toInt() == year }

        val activitiesByMonth: Map<String, List<Activity>> = ActivityHelper.groupActivitiesByMonth(filteredActivities)
        val distanceByMonth: Map<String, Double> = activitiesByMonth.mapValues { (_, activities) ->
            activities.sumOf { activity -> activity.distance / 1000 }
        }

        return FXCollections.observableList(distanceByMonth.map { entry ->
            XYChart.Data(entry.key, entry.value)
        })
    }

    fun buildDistanceByDaysSeries(
        activityType: String,
        year: Int,
    ): ObservableList<XYChart.Data<String, Number>>? {

        val filteredActivities = filterActivitiesByType(activityType)
            .filter { activity -> activity.startDateLocal.subSequence(0, 4).toString().toInt() == year }

        val activitiesByDay: Map<String, List<Activity>> = ActivityHelper.groupActivitiesByDay(filteredActivities, year)
        val distanceByDay: Map<String, Double> = activitiesByDay.mapValues { (_, activities) ->
            activities.sumOf { activity -> activity.distance / 1000 }
        }

        return FXCollections.observableList(distanceByDay.map { entry ->
            XYChart.Data(entry.key, entry.value)
        })
    }

    private fun filterActivitiesByType(activityType: String) = if (activityType == Commute) {
        activities
            .filter { activity -> activity.type == Ride && activity.commute }
    } else {
        activities
            .filter { activity -> activity.type == activityType && !activity.commute }
    }

    private fun buildBadgesToDisplay(
        badgesList: List<Triple<Badge, Activity?, Boolean>>,
        url: String
    ): ObservableList<BadgeDisplay> {

        val badges = badgesList.map { triple ->
            val isCompleted = triple.third
            val activity = triple.second
            val badge = triple.first

            val imageView = ImageView(url)
                .apply {
                    if (!isCompleted) {
                        opacity = 0.15
                    }
                    fitWidth = 120.0
                    isPreserveRatio = true
                    isSmooth = true
                    isCache = true
                }

            val hyperlink = Hyperlink("", imageView)
                .apply {
                    if (triple.second != null) {
                        onAction = EventHandler {
                            Desktop.getDesktop().browse(URI("https://www.strava.com/activities/${activity?.id}"))
                        }
                    }
                }

            BadgeDisplay(badge.toString(), hyperlink)
        }

        return FXCollections.observableArrayList(badges)
    }

    private fun buildStatisticsToDisplay(statistics: List<Statistic>): ObservableList<StatisticDisplay> {

        val activityStatistics = statistics.map { statistic ->
            when (statistic) {
                is ActivityStatistic -> {
                    val hyperlink = if (statistic.activity != null) {
                        Hyperlink(statistic.activity.toString()).apply {
                            onAction = EventHandler {
                                Desktop.getDesktop()
                                    .browse(URI("http://www.strava.com/activities/${statistic.activity?.id}"))
                            }
                        }
                    } else {
                        null
                    }

                    StatisticDisplay(statistic.name, statistic.value, hyperlink)
                }
                else -> StatisticDisplay(statistic.name, statistic.toString(), null)
            }
        }
        return FXCollections.observableArrayList(activityStatistics)
    }

    private fun buildActivitiesToDisplay(activities: List<Activity>): ObservableList<ActivityDisplay> {
        val activitiesToDisplay = activities.map { activity ->

            val hyperlink = Hyperlink(activity.name).apply {
                onAction = EventHandler {
                    Desktop.getDesktop().browse(URI("http://www.strava.com/activities/${activity.id}"))
                }
            }

            ActivityDisplay(
                hyperlink,
                activity.distance,
                activity.elapsedTime,
                activity.totalElevationGain,
                activity.startDateLocal.formatDate()
            )
        }
        return FXCollections.observableArrayList(activitiesToDisplay)
    }
}
