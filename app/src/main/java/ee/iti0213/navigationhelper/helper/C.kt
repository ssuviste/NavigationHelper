package ee.iti0213.navigationhelper.helper

class C {
    companion object {
        const val NOTIFICATION_CHANNEL_DESC = "Default channel"
        const val NOTIFICATION_CHANNEL = "default_channel"
        const val NOTIFICATION_ACTION_CP = "notification_action.cp"
        const val NOTIFICATION_ACTION_WP = "notification_action.wp"

        const val LOCATION_UPDATE = "location_update"
        const val LOC_UPD_LOCATION_KEY = "location_update.location"

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

        const val DISABLE_TRACKING = "disable_tracking"
        const val DIS_TCK_SESSION_NAME_KEY = "disable_tracking.session_name"

        const val TIMER_ACTION = "timer_action"

        const val RES_FORCE_ZOOM_KEY = "restore.force_zoom"
        const val RES_CURR_LAT_LNG_KEY = "restore.current_lat_lng"
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

        const val SESSION_LOCAL_ID_KEY = "session.local_id"

        const val LOCAL_SESSION = "local_session"

        const val LOC_TYPE_LOC = "LOC"
        const val LOC_TYPE_CP = "CP"
        const val LOC_TYPE_WP = "WP"

        const val PREF_FILE_KEY = "preferences_file"
        const val PREF_SYNC_ENABLED_KEY = "preferences.sync_enabled"
        const val PREF_SYNC_INTERVAL_KEY = "preferences.sync_interval"
        const val PREF_GPS_ACC_KEY = "preferences.gps_accuracy"
        const val PREF_GRAD_ENABLED_KEY = "preferences.gradient_enabled"
        const val PREF_GRAD_MIN_PACE_KEY = "preferences.gradient_min_pace"
        const val PREF_GRAD_MAX_PACE_KEY = "preferences.gradient_max_pace"

        const val DEFAULT_SYNC_ENABLED = true
        const val DEFAULT_SYNC_INTERVAL = 30000L
        const val DEFAULT_GPS_ACC = 20
        const val DEFAULT_GRAD_ENABLED = true
        const val DEFAULT_GRAD_MIN_PACE = 7
        const val DEFAULT_GRAD_MAX_PACE = 20

        const val SESSION_LOCAL_ID_LENGTH = 10

        const val LOC_MIN_ACCURACY = 5
        const val LOC_STAND_RADIUS = 2.0f
        const val LOC_UPD_INTERVAL_IN_MILLISECONDS = 2000L
        const val LOC_FASTEST_UPD_INTERVAL_IN_MILLISECONDS = LOC_UPD_INTERVAL_IN_MILLISECONDS / 2

        const val TIMER_INTERVAL_IN_MILLISECONDS = 1000L

        const val MIN_SYNC_INTERVAL_IN_MILLISECONDS = 5000L
        const val PACE_MIN = 1

        const val TOASTED_BTN_COOL_DOWN = 2000L
        const val TRACK_WIDTH = 10f

        const val NOTIFICATION_ID = 4321
        const val REQUEST_FINE_LOC_PERMISSIONS_REQUEST_CODE = 34
        const val REQUEST_EXT_STORAGE_PERMISSIONS_REQUEST_CODE = 1
    }
}