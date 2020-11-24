package me.nicolas.stravastats.infrastructure.dao


import com.fasterxml.jackson.annotation.JsonProperty

data class Map(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("resource_state")
    val resourceState: Int,
    @JsonProperty("summary_polyline")
    val summaryPolyline: String?
)