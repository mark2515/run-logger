package moe.kunlonghe.myruns

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import moe.kunlonghe.myruns.database.MyRunsEntry

class MyRunsEntryAdapter(
    context: Context,
    private var entries: List<MyRunsEntry>
) : ArrayAdapter<MyRunsEntry>(context, 0, entries) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val entry = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.list_item_history, parent, false
        )

        val titleTextView = view.findViewById<TextView>(R.id.textview_title)
        val detailsTextView = view.findViewById<TextView>(R.id.textview_details)

        entry?.let {
            val activityName = UnitConverter.getActivityTypeName(it.activityType)
            val dateTime = UnitConverter.formatDateTime(it.dateTime)
            titleTextView.text = "Manual Entry: $activityName, $dateTime"

            val distance = if (it.distance > 0) {
                UnitConverter.formatDistance(context, it.distance)
            } else {
                "0 Miles"
            }
            
            val duration = if (it.duration > 0) {
                UnitConverter.formatDurationFromSeconds(it.duration)
            } else {
                "0 secs"
            }
            
            detailsTextView.text = "$distance, $duration"
        }

        return view
    }

    override fun getCount(): Int {
        return entries.size
    }

    override fun getItem(position: Int): MyRunsEntry? {
        return entries[position]
    }

    fun replace(newEntries: List<MyRunsEntry>) {
        entries = newEntries
        notifyDataSetChanged()
    }
}

