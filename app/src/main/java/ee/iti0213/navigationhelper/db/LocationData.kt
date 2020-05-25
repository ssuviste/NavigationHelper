package ee.iti0213.navigationhelper.db

import android.location.Location

class LocationData {
    var sessionLocalId: String
    var recordedAt: Long
    var latitude: Double
    var longitude: Double
    var altitude: Double
    var accuracy: Float
    var locationType: String
    var needsSync: Int

    constructor(sessionLocalId: String, location: Location, locationType: String, needsSync: Int) {
        this.sessionLocalId = sessionLocalId
        this.recordedAt = location.time
        this.latitude = location.latitude
        this.longitude = location.longitude
        this.altitude = location.altitude
        this.accuracy = location.accuracy
        this.locationType = locationType
        this.needsSync = needsSync
    }

    constructor(
        sessionLocalId: String, recordedAt: Long, latitude: Double, longitude: Double,
        altitude: Double, accuracy: Float, locationType: String, needsSync: Int
    ) {
        this.sessionLocalId = sessionLocalId
        this.recordedAt = recordedAt
        this.latitude = latitude
        this.longitude = longitude
        this.altitude = altitude
        this.accuracy = accuracy
        this.locationType = locationType
        this.needsSync = needsSync
    }
}