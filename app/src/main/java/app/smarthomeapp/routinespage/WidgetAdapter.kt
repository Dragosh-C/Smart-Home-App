package app.smarthomeapp.routinespage

import FirebaseHelper
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import app.smarthomeapp.R

class WidgetAdapter(private var widgets: List<Widget1>,
                    private val onItemClick: (Widget1) -> Unit
                    ) : RecyclerView.Adapter<WidgetAdapter.WidgetViewHolder>() {

    inner class WidgetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.widgetTitle)
        val type: TextView = view.findViewById(R.id.widgetType)
        val icon: ImageView = view.findViewById(R.id.widgetIcon)
        val statusIndicator: View = view.findViewById(R.id.statusIndicator)



        //------Experiment-----

//        init {
//            view.setOnClickListener {
//                onItemClick(widgets[adapterPosition])
//            }
//        }
//        private val widgetDao = WidgetDatabase.getDatabase().widgetDao()

        init {
            view.setOnClickListener {
                val context = view.context
                val intent = Intent(context, WidgetDetailsActivity::class.java).apply {
                    putExtra("widget_id", widgets[adapterPosition].id)
                    putExtra("widget_title", widgets[adapterPosition].title)
                    putExtra("widget_type", widgets[adapterPosition].type)
                    putExtra("widget_isEnabled", widgets[adapterPosition].isEnabled)
                    putExtra("if_condition", widgets[adapterPosition].ifCondition)
                    putExtra("then_action", widgets[adapterPosition].thenAction)
                    putExtra("repeat_every_day", widgets[adapterPosition].repeatEveryDay)
                    putExtra("device_id", widgets[adapterPosition].deviceID)

                }
                context.startActivity(intent)
            }
        }
        //--------
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.routine_widet_item,
                                                                parent,false)
        return WidgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: WidgetViewHolder, position: Int) {
        val widget = widgets[position]
        holder.title.text = widget.title
        holder.type.text = widget.type

        if (widget.isEnabled) {
            holder.statusIndicator.setBackgroundResource(R.drawable.circle_green)
            val pulseAnimation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.pulsing_animation)
            holder.statusIndicator.startAnimation(pulseAnimation)
        } else {
            holder.statusIndicator.setBackgroundResource(R.drawable.circle_grey)
            holder.statusIndicator.clearAnimation()

        }

        val drawableResId = when (widget.type) {
            "Title1" -> R.drawable.ic_light
            "Dimmer" -> R.drawable.ic_dimmer2
            "Relay" -> R.drawable.ic_relay
            "Alarm" -> R.drawable.ic_clock
            "Alert" -> R.drawable.ic_alert
            else -> R.drawable.ic_routine
        }
        holder.icon.setImageResource(drawableResId)

    }

    override fun getItemCount() = widgets.size

    fun updateData(newWidgets: List<Widget1>) {
        widgets = newWidgets
        notifyDataSetChanged()
//        notifyItemRangeChanged(0, widgets.size)

    }
}
