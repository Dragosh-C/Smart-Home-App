package app.smarthomeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
class WidgetAdapter(private val widgetList: List<Widget2>) : RecyclerView.Adapter<WidgetAdapter.WidgetViewHolder>() {

    // ViewHolder to hold the view for each item
    inner class WidgetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val widgetNameTextView: TextView = view.findViewById(R.id.widget_name)
        val widgetColorTextView: TextView = view.findViewById(R.id.widget_color)
        val widgetSizeTextView: TextView = view.findViewById(R.id.widget_size)
    }

    // Inflate the item layout and create ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_widget, parent, false)
        return WidgetViewHolder(view)
    }

    // Bind widget data to the views
    override fun onBindViewHolder(holder: WidgetViewHolder, position: Int) {
        val widget = widgetList[position]
        holder.widgetNameTextView.text = widget.name
        holder.widgetColorTextView.text = widget.color
        holder.widgetSizeTextView.text = widget.size.toString()
    }

    override fun getItemCount(): Int {
        return widgetList.size
    }
}
