package ee.iti0213.navigationhelper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import ee.iti0213.navigationhelper.adapter.DataRecyclerViewAdapterHistory
import ee.iti0213.navigationhelper.db.Repository
import kotlinx.android.synthetic.main.activity_history.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var databaseConnector: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val white = ContextCompat.getColor(this, R.color.colorWhite)
        val mode = android.graphics.PorterDuff.Mode.MULTIPLY
        val toolbar = findViewById<Toolbar>(R.id.mainMenuToolbar)
        toolbar.title = getString(R.string.history)
        toolbar.logo = getDrawable(R.drawable.baseline_restore_24)
        //toolbar.overflowIcon = getDrawable(R.drawable.baseline_account_box_24)
        @Suppress("DEPRECATION")
        toolbar.logo.setColorFilter(white, mode)
        setSupportActionBar(toolbar)

        databaseConnector = Repository(this).open()
    }

    override fun onResume() {
        super.onResume()
        recyclerViewHistory.layoutManager = LinearLayoutManager(this)
        recyclerViewHistory.adapter = DataRecyclerViewAdapterHistory(this, databaseConnector)
    }

    override fun onDestroy() {
        super.onDestroy()
        databaseConnector.close()
    }
}
