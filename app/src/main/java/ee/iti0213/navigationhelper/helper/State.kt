package ee.iti0213.navigationhelper.helper

object State {
    var loggedIn: Boolean = false
    var userEmail: String? = null
    var sessionActive: Boolean = false
    var toastBtnLastClickTime: Long = 0L
}