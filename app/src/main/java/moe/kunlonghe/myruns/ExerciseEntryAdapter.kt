package moe.kunlonghe.myruns

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import moe.kunlonghe.myruns.database.ExerciseEntry

class ExerciseEntryAdapter(
    context: Context,
    private var entries: List<ExerciseEntry>
) : ArrayAdapter<ExerciseEntry>(context, 0, entries) {

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
            titleTextView.text = "$activityName: $dateTime"

            val distance = if (it.distance > 0) {
                UnitConverter.formatDistance(context, it.distance)
            } else {
                "Distance: N/A"
            }
            
            val duration = if (it.duration > 0) {
                UnitConverter.formatDurationInMinutes(it.duration)
            } else {
                "Duration: N/A"
            }
            
            detailsTextView.text = "$distance, $duration"
        }

        return view
    }

    override fun getCount(): Int {
        return entries.size
    }

    override fun getItem(position: Int): ExerciseEntry? {
        return entries[position]
    }

    fun replace(newEntries: List<ExerciseEntry>) {
        entries = newEntries
        notifyDataSetChanged()
    }
}

