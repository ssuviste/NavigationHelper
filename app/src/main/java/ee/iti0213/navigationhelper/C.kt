package ee.iti0213.navigationhelper

class C {
    companion object {
        const val NOTIFICATION_CHANNEL = "default_channel"
        const val NOTIFICATION_ACTION_CP = "notification_action.cp"
        const val NOTIFICATION_ACTION_WP = "notification_action.wp"

        const val TIMER_ACTION = "timer_action"

        const val ENABLE_TRACKING = "enable_tracking"
        const val DISABLE_TRACKING = "disable_tracking"

        const val LOCATION_UPDATE = "location_update"

        const val LOC_UPD_LATITUDE_KEY = "location_update.latitude"
        const val LOC_UPD_LONGITUDE_KEY = "location_update.longitude"
        const val LOC_UPD_BEARING_KEY = "location_update.bearing"

        const val TRACKING_UPDATE = "tracking_update"

        const val TCK_UPD_TRACK_KEY = "tracking_update.track"

        const val TCK_UPD_WALK_DIST_START_KEY = "tracking_update.walk_distance_start"
        const val TCK_UPD_FLY_DIST_START_KEY = "tracking_update.fly_distance_start"
        const val TCK_UPD_TIME_START_KEY = "tracking_update.time_start"
        const val TCK_UPD_SPEED_START_KEY = "tracking_update.speed_start"

        const val TCK_UPD_WALK_DIST_CP_KEY = "tracking_update.walk_distance_cp"
        const val TCK_UPD_FLY_DIST_CP_KEY = "tracking_update.fly_distance_cp"
        const val TCK_UPD_TIME_CP_KEY = "tracking_update.time_cp"
        const val TCK_UPD_SPEED_CP_KEY = "tracking_update.speed_cp"

        const val TCK_UPD_WALK_DIST_WP_KEY = "tracking_update.walk_distance_wp"
        const val TCK_UPD_FLY_DIST_WP_KEY = "tracking_update.fly_distance_wp"
        const val TCK_UPD_TIME_WP_KEY = "tracking_update.time_wp"
        const val TCK_UPD_SPEED_WP_KEY = "tracking_update.speed_wp"

        const val LOCATION_SERVICE_STATE_KEY = "tracking_service_state"
        const val COMPASS_STATE_KEY = "compass_state"
        const val UP_DIR_STATE_KEY = "up_dir_state"

        const val TRACK_KEY = "track"

        const val WALK_DIST_START_KEY = "walk_distance_start"
        const val FLY_DIST_START_KEY = "fly_distance_start"
        const val TIME_START_KEY = "time_start"
        const val SPEED_START_KEY = "speed_start"

        const val WALK_DIST_CP_KEY = "walk_distance_cp"
        const val FLY_DIST_CP_KEY = "fly_distance_cp"
        const val TIME_CP_KEY = "time_cp"
        const val SPEED_CP_KEY = "speed_cp"

        const val WALK_DIST_WP_KEY = "walk_distance_wp"
        const val FLY_DIST_WP_KEY = "fly_distance_wp"
        const val TIME_WP_KEY = "time_wp"
        const val SPEED_WP_KEY = "speed_wp"

        const val LOC_UPD_INTERVAL_IN_MILLISECONDS = 2000L
        const val LOC_FASTEST_UPD_INTERVAL_IN_MILLISECONDS = LOC_UPD_INTERVAL_IN_MILLISECONDS / 2
        const val TIMER_INTERVAL_IN_MILLISECONDS = 1000L

        const val NOTIFICATION_ID = 4321
        const val REQUEST_PERMISSIONS_REQUEST_CODE = 34

        const val TOASTED_BTN_COOL_DOWN = 2000L
    }
}