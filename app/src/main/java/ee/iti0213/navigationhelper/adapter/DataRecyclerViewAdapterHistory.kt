package ee.iti0213.navigationhelper.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import ee.iti0213.navigationhelper.HistoryMapsActivity
import ee.iti0213.navigationhelper.R
import ee.iti0213.navigationhelper.db.Repository
import ee.iti0213.navigationhelper.db.SessionData
import ee.iti0213.navigationhelper.helper.C
import ee.iti0213.navigationhelper.helper.Common
import ee.iti0213.navigationhelper.helper.DateFormat
import kotlinx.android.synthetic.main.recycler_row_session.view.*

class DataRecyclerViewAdapterHistory(context: Context, private val databaseConnector: Repository) :
    RecyclerView.Adapter<DataRecyclerViewAdapterHistory.ViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var sessions: List<SessionData> = databaseConnector.getAllFinishedSessions()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowView = inflater.inflate(R.layout.recycler_row_session, parent, false)
        return ViewHolder(rowView)
    }

    override fun getItemCount(): Int {
        return sessions.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = sessions[position]
        val cpCount = databaseConnector.getAllCPsBySessionLocalId(session.localId).size
        holder.itemView.recyclerRowSession.tag = session.localId
        holder.itemView.textViewSessionTitle.text = session.sessionName
        holder.itemView.textViewSessionDate.text = Common.convertLongToDate(session.startTime, DateFormat.DEFAULT)
        holder.itemView.textViewSessionCPsCount.text = cpCount.toString()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.recyclerRowSession.setOnClickListener {
                val intent = Intent(it.context, HistoryMapsActivity::class.java)
                intent.putExtra(C.SESSION_LOCAL_ID_KEY, it.tag.toString())
                startActivity(it.context, intent, null)
            }
        }
    }
}