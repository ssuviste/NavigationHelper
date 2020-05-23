package ee.iti0213.navigationhelper.helper

class C {
    companion object {
        const val NOTIFICATION_CHANNEL = "default_channel"
        const val NOTIFICATION_ACTION_CP = "notification_action.cp"
        const val NOTIFICATION_ACTION_WP = "notification_action.wp"

        const val DISABLE_TRACKING = "disable_tracking"

        const val TIMER_ACTION = "timer_action"

        const val LOCATION_UPDATE = "location_update"

        const val LOC_UPD_LOCATION_KEY = "location_update.location"
        const val LOC_UPD_LATITUDE_KEY = "location_update.latitude"
        const val LOC_UPD_LONGITUDE_KEY = "location_update.longitude"
        const val LOC_UPD_BEARING_KEY = "location_update.bearing"

        const val TRACKING_UPDATE = "tracking_update"

        const val TCK_UPD_TRACK_KEY = "tracking_update.track"
        const val TCK_UPD_CPS_KEY = "tracking_update.all_cps"
        const val TCK_UPD_WP_KEY = "tracking_update.wp"

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

        const val RES_LOCATION_SERVICE_STATE_KEY = "tracking_service_state"
        const val RES_COMPASS_STATE_KEY = "compass_state"
        const val RES_UP_DIR_STATE_KEY = "up_dir_state"

        const val RES_TRACK_KEY = "restore.track"
        const val RES_CPS_KEY = "restore.all_cps"
        const val RES_WP = "restore.wp"

        const val RES_WALK_DIST_START_KEY = "restore.walk_distance_start"
        const val RES_FLY_DIST_START_KEY = "restore.fly_distance_start"
        const val RES_TIME_START_KEY = "restore.time_start"
        const val RES_SPEED_START_KEY = "restore.speed_start"

        const val RES_WALK_DIST_CP_KEY = "restore.walk_distance_cp"
        const val RES_FLY_DIST_CP_KEY = "restore.fly_distance_cp"
        const val RES_TIME_CP_KEY = "restore.time_cp"
        const val RES_SPEED_CP_KEY = "restore.speed_cp"

        const val RES_WALK_DIST_WP_KEY = "restore.walk_distance_wp"
        const val RES_FLY_DIST_WP_KEY = "restore.fly_distance_wp"
        const val RES_TIME_WP_KEY = "restore.time_wp"
        const val RES_SPEED_WP_KEY = "restore.speed_wp"

        const val LOC_STAND_RADIUS = 5f
        const val LOC_MIN_ACCURACY = 20f

        const val LOC_UPD_INTERVAL_IN_MILLISECONDS = 2000L
        const val LOC_FASTEST_UPD_INTERVAL_IN_MILLISECONDS = LOC_UPD_INTERVAL_IN_MILLISECONDS / 2
        const val TIMER_INTERVAL_IN_MILLISECONDS = 1000L

        const val MIN_SYNC_INTERVAL_IN_MILLISECONDS = 5000L

        const val NOTIFICATION_ID = 4321
        const val REQUEST_PERMISSIONS_REQUEST_CODE = 34

        const val TOASTED_BTN_COOL_DOWN = 2000L
        const val TRACK_WIDTH = 10f
    }
}