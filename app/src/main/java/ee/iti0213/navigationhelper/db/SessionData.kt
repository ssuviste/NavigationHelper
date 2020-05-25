package ee.iti0213.navigationhelper.db

class SessionData(
    var localId: String,
    var serverId: String?,
    var sessionName: String,
    var description: String,
    var startTime: Long,
    var paceMin: Int,
    var paceMax: Int,
    var isFinished: Int
)