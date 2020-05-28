package ee.iti0213.navigationhelper.helper

object Preferences {
    var syncEnabled: Boolean = true
    var syncInterval: Long = 30000L
    var gpsAccuracy: Int = 20
    var gradientEnabled: Boolean = true
    var gradientMinPace: Int = 7
    var gradientMaxPace: Int = 20
}