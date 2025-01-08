package app.smarthomeapp.routinespage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.smarthomeapp.R

class WidgetAdapter(private var widgets: List<Widget1>) : RecyclerView.Adapter<WidgetAdapter.WidgetViewHolder>() {

    class WidgetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.widgetTitle)
        val type: TextView = view.findViewById(R.id.widgetType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.routine_widet_item, parent, false)
        return WidgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: WidgetViewHolder, position: Int) {
        val widget = widgets[position]
        holder.title.text = widget.title
        holder.type.text = widget.type
    }

    override fun getItemCount() = widgets.size

    fun updateData(newWidgets: List<Widget1>) {
        widgets = newWidgets
        notifyDataSetChanged()
    }
}
