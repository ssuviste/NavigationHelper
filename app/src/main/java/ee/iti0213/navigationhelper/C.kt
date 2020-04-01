package ee.iti0213.navigationhelper

class C {
    companion object {
        const val NOTIFICATION_CHANNEL = "default_channel"
        const val NOTIFICATION_ACTION_WP = "ee.iti0213.wp"
        const val NOTIFICATION_ACTION_CP = "ee.iti0213.cp"

        const val LOCATION_UPDATE_ACTION = "ee.iti0213.location_update"

        const val LOCATION_UPDATE_ACTION_LATITUDE = "ee.iti0213.location_update.latitude"
        const val LOCATION_UPDATE_ACTION_LONGITUDE = "ee.iti0213.location_update.longitude"

        const val UPDATE_INTERVAL_IN_MILLISECONDS = 2000L
        const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2

        const val NOTIFICATION_ID = 4321
        const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }
}