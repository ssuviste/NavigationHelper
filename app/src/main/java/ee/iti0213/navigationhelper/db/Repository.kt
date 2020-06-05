package ee.iti0213.navigationhelper.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import ee.iti0213.navigationhelper.helper.C

class Repository(val context: Context) {
    private lateinit var dbHelper: DbHelper
    private lateinit var db: SQLiteDatabase

    fun open(): Repository {
        dbHelper = DbHelper(context)
        db = dbHelper.writableDatabase
        dbHelper.onCreate(db)
        return this
    }

    fun close() {
        dbHelper.close()
    }

    fun addSession(sessionData: SessionData) {
        val values = ContentValues()
        values.put(DbHelper.SESSION_LOCAL_ID, sessionData.localId)
        values.put(DbHelper.SESSION_SERVER_ID, sessionData.serverId)
        values.put(DbHelper.SESSION_NAME, sessionData.sessionName)
        values.put(DbHelper.SESSION_DESCRIPTION, sessionData.description)
        values.put(DbHelper.SESSION_START_TIME, sessionData.startTime)
        values.put(DbHelper.SESSION_PACE_MIN, sessionData.paceMin)
        values.put(DbHelper.SESSION_PACE_MAX, sessionData.paceMax)
        values.put(DbHelper.SESSION_IS_FINISHED, sessionData.isFinished)

        db.insert(DbHelper.SESSIONS_TABLE_NAME, null, values)
    }

    private fun getAllSessions(): List<SessionData> {
        val sessions = ArrayList<SessionData>()
        val columns = arrayOf(
            DbHelper.SESSION_LOCAL_ID,
            DbHelper.SESSION_SERVER_ID,
            DbHelper.SESSION_NAME,
            DbHelper.SESSION_DESCRIPTION,
            DbHelper.SESSION_START_TIME,
            DbHelper.SESSION_PACE_MIN,
            DbHelper.SESSION_PACE_MAX,
            DbHelper.SESSION_IS_FINISHED
        )
        val orderBy = DbHelper.SESSION_ID + " DESC"
        val cursor = db.query(
            DbHelper.SESSIONS_TABLE_NAME,
            columns,
            null,
            null,
            null,
            null,
            orderBy
        )
        while (cursor.moveToNext()) {
            val serverId = if (cursor.isNull(cursor.getColumnIndex(DbHelper.SESSION_SERVER_ID))) {
                null
            } else {
                cursor.getString(cursor.getColumnIndex(DbHelper.SESSION_SERVER_ID))
            }
            sessions.add(
                SessionData(
                    cursor.getString(cursor.getColumnIndex(DbHelper.SESSION_LOCAL_ID)),
                    serverId,
                    cursor.getString(cursor.getColumnIndex(DbHelper.SESSION_NAME)),
                    cursor.getString(cursor.getColumnIndex(DbHelper.SESSION_DESCRIPTION)),
                    cursor.getLong(cursor.getColumnIndex(DbHelper.SESSION_START_TIME)),
                    cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_PACE_MIN)),
                    cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_PACE_MAX)),
                    cursor.getInt(cursor.getColumnIndex(DbHelper.SESSION_IS_FINISHED))
                )
            )
        }
        cursor.close()
        return sessions
    }

    fun getAllFinishedSessions(): List<SessionData> {
        return getAllSessions().filter { it.isFinished != 0 }
    }

    fun getAllSessionsWhereServerIdNull(): List<SessionData> {
        return getAllSessions().filter { it.serverId == null }
    }

    fun getSessionServerIdByLocalId(localId: String): String? {
        val columns = arrayOf(
            DbHelper.SESSION_SERVER_ID
        )
        val selection = "${DbHelper.SESSION_LOCAL_ID} = '$localId'"
        val cursor = db.query(
            DbHelper.SESSIONS_TABLE_NAME,
            columns,
            selection,
            null,
            null,
            null,
            null
        )
        var result: String? = null
        if (cursor.moveToNext()) {
            result = cursor.getString(cursor.getColumnIndex(DbHelper.SESSION_SERVER_ID))
        }
        cursor.close()
        return result
    }

    fun getAllSessionLocalIds(): List<String> {
        val columns = arrayOf(
            DbHelper.SESSION_LOCAL_ID
        )
        val cursor = db.query(
            DbHelper.SESSIONS_TABLE_NAME,
            columns,
            null,
            null,
            null,
            null,
            null
        )
        val result = ArrayList<String>()
        while (cursor.moveToNext()) {
            result.add(cursor.getString(cursor.getColumnIndex(DbHelper.SESSION_LOCAL_ID)))
        }
        cursor.close()
        return result
    }

    fun setSessionIsFinished(localId: String, isFinished: Int) {
        val values = ContentValues()
        values.put(DbHelper.SESSION_IS_FINISHED, isFinished)
        db.update(
            DbHelper.SESSIONS_TABLE_NAME,
            values,
            "${DbHelper.SESSION_LOCAL_ID} = '$localId'",
            null
        )
    }

    fun setSessionName(localId: String, sessionName: String) {
        val values = ContentValues()
        values.put(DbHelper.SESSION_NAME, sessionName)
        db.update(
            DbHelper.SESSIONS_TABLE_NAME,
            values,
            "${DbHelper.SESSION_LOCAL_ID} = '$localId'",
            null
        )
    }

    fun setSessionServerId(startTime: Long, serverId: String) {
        val values = ContentValues()
        values.put(DbHelper.SESSION_SERVER_ID, serverId)
        db.update(
            DbHelper.SESSIONS_TABLE_NAME,
            values,
            "${DbHelper.SESSION_START_TIME} = $startTime",
            null
        )
    }

    fun deleteAllSessionsByLocalId(localId: String) {
        db.delete(
            DbHelper.SESSIONS_TABLE_NAME,
            "${DbHelper.SESSION_LOCAL_ID} = '$localId'",
            null
        )
    }

    fun addLocation(locationData: LocationData) {
        val values = ContentValues()
        values.put(DbHelper.LOCATION_SESSION_LOCAL_ID, locationData.sessionLocalId)
        values.put(DbHelper.LOCATION_RECORDED_AT, locationData.recordedAt)
        values.put(DbHelper.LOCATION_LATITUDE, locationData.latitude)
        values.put(DbHelper.LOCATION_LONGITUDE, locationData.longitude)
        values.put(DbHelper.LOCATION_ALTITUDE, locationData.altitude)
        values.put(DbHelper.LOCATION_ACCURACY, locationData.accuracy)
        values.put(DbHelper.LOCATION_TYPE, locationData.locationType)
        values.put(DbHelper.LOCATION_NEEDS_SYNC, locationData.needsSync)

        db.insert(DbHelper.LOCATIONS_TABLE_NAME, null, values)
    }

    fun getAllLocationsBySessionLocalId(sessionLocalId: String): List<LocationData> {
        val columns = arrayOf(
            DbHelper.LOCATION_SESSION_LOCAL_ID,
            DbHelper.LOCATION_RECORDED_AT,
            DbHelper.LOCATION_LATITUDE,
            DbHelper.LOCATION_LONGITUDE,
            DbHelper.LOCATION_ALTITUDE,
            DbHelper.LOCATION_ACCURACY,
            DbHelper.LOCATION_TYPE,
            DbHelper.LOCATION_NEEDS_SYNC
        )
        val selection = "${DbHelper.LOCATION_SESSION_LOCAL_ID} = '$sessionLocalId'"
        val orderBy = DbHelper.LOCATION_ID + " ASC"
        val cursor = db.query(
            DbHelper.LOCATIONS_TABLE_NAME,
            columns,
            selection,
            null,
            null,
            null,
            orderBy
        )
        val locations = ArrayList<LocationData>()
        while (cursor.moveToNext()) {
            locations.add(
                LocationData(
                    cursor.getString(cursor.getColumnIndex(DbHelper.LOCATION_SESSION_LOCAL_ID)),
                    cursor.getLong(cursor.getColumnIndex(DbHelper.LOCATION_RECORDED_AT)),
                    cursor.getDouble(cursor.getColumnIndex(DbHelper.LOCATION_LATITUDE)),
                    cursor.getDouble(cursor.getColumnIndex(DbHelper.LOCATION_LONGITUDE)),
                    cursor.getDouble(cursor.getColumnIndex(DbHelper.LOCATION_ALTITUDE)),
                    cursor.getFloat(cursor.getColumnIndex(DbHelper.LOCATION_ACCURACY)),
                    cursor.getString(cursor.getColumnIndex(DbHelper.LOCATION_TYPE)),
                    cursor.getInt(cursor.getColumnIndex(DbHelper.LOCATION_NEEDS_SYNC))
                )
            )
        }
        cursor.close()
        return locations
    }

    fun getAllCPsBySessionLocalId(sessionLocalId: String): List<LocationData> {
        return getAllLocationsBySessionLocalId(sessionLocalId).filter {
            it.locationType == C.LOC_TYPE_CP
        }
    }

    fun getAllWPsBySessionLocalId(sessionLocalId: String): List<LocationData> {
        return getAllLocationsBySessionLocalId(sessionLocalId).filter {
            it.locationType == C.LOC_TYPE_WP
        }
    }

    fun getAllLocationsThatNeedSync(): List<LocationData> {
        val columns = arrayOf(
            DbHelper.LOCATION_SESSION_LOCAL_ID,
            DbHelper.LOCATION_RECORDED_AT,
            DbHelper.LOCATION_LATITUDE,
            DbHelper.LOCATION_LONGITUDE,
            DbHelper.LOCATION_ALTITUDE,
            DbHelper.LOCATION_ACCURACY,
            DbHelper.LOCATION_TYPE,
            DbHelper.LOCATION_NEEDS_SYNC
        )
        val selection = "${DbHelper.LOCATION_NEEDS_SYNC} <> 0"
        val orderBy = DbHelper.LOCATION_ID + " ASC"
        val cursor = db.query(
            DbHelper.LOCATIONS_TABLE_NAME,
            columns,
            selection,
            null,
            null,
            null,
            orderBy
        )
        val locations = ArrayList<LocationData>()
        while (cursor.moveToNext()) {
            locations.add(
                LocationData(
                    cursor.getString(cursor.getColumnIndex(DbHelper.LOCATION_SESSION_LOCAL_ID)),
                    cursor.getLong(cursor.getColumnIndex(DbHelper.LOCATION_RECORDED_AT)),
                    cursor.getDouble(cursor.getColumnIndex(DbHelper.LOCATION_LATITUDE)),
                    cursor.getDouble(cursor.getColumnIndex(DbHelper.LOCATION_LONGITUDE)),
                    cursor.getDouble(cursor.getColumnIndex(DbHelper.LOCATION_ALTITUDE)),
                    cursor.getFloat(cursor.getColumnIndex(DbHelper.LOCATION_ACCURACY)),
                    cursor.getString(cursor.getColumnIndex(DbHelper.LOCATION_TYPE)),
                    cursor.getInt(cursor.getColumnIndex(DbHelper.LOCATION_NEEDS_SYNC))
                )
            )
        }
        cursor.close()
        return locations
    }

    fun setLocationNeedsSyncByRecordedAt(recordedAt: Long, needsSync: Int) {
        val values = ContentValues()
        values.put(DbHelper.LOCATION_NEEDS_SYNC, needsSync)
        db.update(
            DbHelper.LOCATIONS_TABLE_NAME,
            values,
            "${DbHelper.LOCATION_RECORDED_AT} = $recordedAt",
            null
        )
    }

    fun deleteAllLocationsBySessionLocalId(sessionLocalId: String) {
        db.delete(
            DbHelper.LOCATIONS_TABLE_NAME,
            "${DbHelper.LOCATION_SESSION_LOCAL_ID} = '$sessionLocalId'",
            null
        )
    }
}