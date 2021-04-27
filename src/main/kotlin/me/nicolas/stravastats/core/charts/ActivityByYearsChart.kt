package me.nicolas.stravastats.core.charts

import hep.dataforge.values.Value
import kscience.plotly.*
import kscience.plotly.models.*
import me.nicolas.stravastats.business.Activity
import java.time.LocalDate

internal class ActivityByYearsChart(activities: List<Activity>, val type: String) {

    private val activitiesByYear: Map<String, List<Activity>> =
        ChartHelper.groupActivitiesByYear(activities.filter { activity -> activity.type == type })

    fun build() {
        val plot = Plotly.grid {
            buildCumulativeKilometers()
            buildCumulativeElevation()
        }
        plot.makeFile()
    }

    private fun PlotGrid.buildCumulativeKilometers() {

        val annotationsList = mutableListOf<Text>()

        plot(row = 1, width = 12) {
            for (year in 2010..LocalDate.now().year) {
                val activities =
                    if (activitiesByYear[year.toString()] != null) activitiesByYear[year.toString()]!! else continue
                val activitiesByDay = ChartHelper.groupActivitiesByDay(activities, year)
                val cumulativeDistance = getCumulativeDistance(activitiesByDay)
                traces(
                    buildLine(cumulativeDistance, year)
                )
                val text = Text {
                    xref = "x"
                    yref = "y"
                    x = Value.of(cumulativeDistance.keys.last())
                    y = Value.of(cumulativeDistance.values.maxOf { it })
                    xanchor = XAnchor.left
                    yanchor = YAnchor.middle
                    text = "  %.0f".format(cumulativeDistance.values.maxOf { it })
                    font {
                        family = "Arial"
                        size = 12
                        color("black")
                    }
                    showarrow = false
                }
                annotationsList.add(text)
            }

            layout {
                barmode = BarMode.stack
                title = "$type distance (km)"

                xaxis {
                    title = "Day"
                    type = AxisType.category
                }
                yaxis {
                    title = "Km"
                }
                legend {
                    xanchor = XAnchor.left
                    bgcolor("#E2E2E2")
                    traceorder = TraceOrder.normal
                }
                annotations = annotationsList
            }
        }

    }

    private fun PlotGrid.buildCumulativeElevation() {
        val annotationsList = mutableListOf<Text>()

        plot(row = 2, width = 12) {
            for (year in 2010..LocalDate.now().year) {
                val activities =
                    if (activitiesByYear[year.toString()] != null) activitiesByYear[year.toString()]!! else continue
                val activitiesByDay = ChartHelper.groupActivitiesByDay(activities, year)
                val cumulativeElevation = getCumulativeElevation(activitiesByDay)
                traces(
                    buildLine(cumulativeElevation, year)
                )
                val text = Text {
                    xref = "x"
                    yref = "y"
                    x = Value.of(cumulativeElevation.keys.last())
                    y = Value.of(cumulativeElevation.values.maxOf { it })
                    xanchor = XAnchor.left
                    yanchor = YAnchor.middle
                    text = "  %.0f".format(cumulativeElevation.values.maxOf { it })
                    font {
                        family = "Arial"
                        size = 12
                        color("black")
                    }
                    showarrow = false
                }
                annotationsList.add(text)
            }

            layout {
                barmode = BarMode.stack
                title = "$type elevation (m)"

                xaxis {
                    title = "Day"
                    type = AxisType.category
                }
                yaxis {
                    title = "m"
                }
                legend {
                    xanchor = XAnchor.left
                    bgcolor("#E2E2E2")
                    traceorder = TraceOrder.normal
                }
                annotations = annotationsList
            }
        }
    }

    private fun buildLine(activitiesByDay: Map<String, Double>, year: Int): Scatter {

        return Scatter {
            x.set(activitiesByDay.keys)
            y.set(activitiesByDay.values)
            name = "$year"
        }
    }

    private fun getCumulativeDistance(activities: Map<String, List<Activity>>): Map<String, Double> {
        var sum = 0.0
        return activities.mapValues { (_, value) -> sum += value.sumOf { activity -> activity.distance / 1000 }; sum }
    }

    private fun getCumulativeElevation(activities: Map<String, List<Activity>>): Map<String, Double> {
        var sum = 0.0
        return activities.mapValues { (_, value) -> sum += value.sumOf { activity -> activity.totalElevationGain }; sum }
    }
}