package com.emtanveer.uberremake

import com.emtanveer.uberremake.model.DriverInfoModel
import java.lang.StringBuilder

object Common {
    fun buildWelcomeMessage(): String {
        return StringBuilder("Welcome, ")
            .append(currentUser!!.firstName)
            .append(" ")
            .append(currentUser!!.lastName)
            .toString()
    }

    const val DRIVERS_LOCATION_REFERENCE: String = "DriversLocation"
    const val DRIVER_INFO_REFERENCE:String = "DriverInfo"
    var currentUser: DriverInfoModel? = null
}