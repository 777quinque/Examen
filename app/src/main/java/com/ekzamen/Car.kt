package com.ekzamen

import java.io.Serializable

data class Car(
    var brand: String = "",
    var year: Int = 0,
    var engineVolume: Float = 0.0f,
    var dealerEmail: String = "",
    var websiteUrl: String = "",
    var photoUrl: String = ""
) : Serializable