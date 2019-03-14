package com.tema.smartalarm.model

import com.google.gson.annotations.SerializedName

data class DistMatrix(
    val rows: List<Row>,
    val traffic_type: String
)

data class Row(
    val elements: List<Element>
)

data class Element(
    val distance: Distance,
    val duration: Duration,
    val status: String
)

data class Duration(
    @SerializedName("value")
    val seconds: Int
)

data class Distance(
    @SerializedName("value")
    val meters: Int
)