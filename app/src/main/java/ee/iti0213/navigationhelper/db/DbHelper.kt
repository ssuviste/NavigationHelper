package ee.iti0213.navigationhelper.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "ee.iti0213.navigationhelper.db"
        const val DATABASE_VERSION = 1

        const val SESSIONS_TABLE_NAME = "Sessions"

        const val SESSION_ID = "_id"
        const val SESSION_LOCAL_ID = "localId"
        const val SESSION_SERVER_ID = "serverId"
        const val SESSION_NAME = "sessionName"
        const val SESSION_DESCRIPTION = "description"
        const val SESSION_START_TIME = "startTime"
        const val SESSION_PACE_MIN = "paceMin"
        const val SESSION_PACE_MAX = "paceMax"
        const val SESSION_IS_FINISHED = "isFinished"

        const val LOCATIONS_TABLE_NAME = "Locations"

        const val LOCATION_ID = "_id"
        const val LOCATION_SESSION_LOCAL_ID = "sessionLocalId"
        const val LOCATION_RECORDED_AT = "recordedAt"
        const val LOCATION_LATITUDE = "latitude"
        const val LOCATION_LONGITUDE = "longitude"
        const val LOCATION_ALTITUDE = "altitude"
        const val LOCATION_ACCURACY = "accuracy"
        const val LOCATION_TYPE = "locationType"
        const val LOCATION_NEEDS_SYNC = "needsSync"

        const val SQL_CREATE_TABLE_SESSIONS =
            "CREATE TABLE IF NOT EXISTS $SESSIONS_TABLE_NAME(" +
                    "$SESSION_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$SESSION_LOCAL_ID TEXT NOT NULL, " +
                    "$SESSION_SERVER_ID TEXT, " +
                    "$SESSION_NAME TEXT NOT NULL, " +
                    "$SESSION_DESCRIPTION TEXT NOT NULL, " +
                    "$SESSION_START_TIME INTEGER NOT NULL, " +
                    "$SESSION_PACE_MIN INTEGER NOT NULL, " +
                    "$SESSION_PACE_MAX INTEGER NOT NULL, " +
                    "$SESSION_IS_FINISHED INTEGER NOT NULL" +
                    ");"

        const val SQL_CREATE_TABLE_LOCATIONS =
            "CREATE TABLE IF NOT EXISTS $LOCATIONS_TABLE_NAME(" +
                    "$LOCATION_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$LOCATION_SESSION_LOCAL_ID TEXT NOT NULL, " +
                    "$LOCATION_RECORDED_AT INTEGER NOT NULL, " +
                    "$LOCATION_LATITUDE REAL NOT NULL, " +
                    "$LOCATION_LONGITUDE REAL NOT NULL, " +
                    "$LOCATION_ALTITUDE REAL NOT NULL, " +
                    "$LOCATION_ACCURACY REAL NOT NULL, " +
                    "$LOCATION_TYPE TEXT NOT NULL, " +
                    "$LOCATION_NEEDS_SYNC INTEGER NOT NULL" +
                    ");"

        const val SQL_DELETE_TABLE_SESSIONS = "DROP TABLE IF EXISTS $SESSIONS_TABLE_NAME"
        const val SQL_DELETE_TABLE_LOCATIONS = "DROP TABLE IF EXISTS $LOCATIONS_TABLE_NAME"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_TABLE_SESSIONS)
        db?.execSQL(SQL_CREATE_TABLE_LOCATIONS)
        //db?.execSQL(SQL_DELETE_TABLE_SESSIONS)
        //db?.execSQL(SQL_DELETE_TABLE_LOCATIONS)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(SQL_DELETE_TABLE_LOCATIONS)
        db?.execSQL(SQL_DELETE_TABLE_SESSIONS)
        onCreate(db)
    }
}