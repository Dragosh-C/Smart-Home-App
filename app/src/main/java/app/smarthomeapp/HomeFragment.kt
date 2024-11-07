//package app.smarthomeapp
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//
//class HomeFragment : Fragment() {
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_home, container, false)
//    }
//}

package app.smarthomeapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    private lateinit var gridLayout: GridLayout
    private var widgetCount = 0 // number of widgets to do! sysncronise with firebase etc.

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        gridLayout = view.findViewById(R.id.grid_layout)

        val addButton = view.findViewById<Button>(R.id.add_widget_button)
        addButton.setOnClickListener {
            addNewWidget()
        }

        return view
    }

    private fun addNewWidget() {
        val cardView = CardView(requireContext()).apply {
            radius = 16f
            cardElevation = 4f
            setContentPadding(16, 16, 16, 16)
        }

        val params = GridLayout.LayoutParams().apply {
            width = 0
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            setMargins(8, 8, 8, 8)
        }
        cardView.layoutParams = params

        // Create inner layout and components for the widget
        val innerLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            setBackgroundColor(ContextCompat.getColor(requireContext(), com.google.android.material.R.color.design_default_color_background))
        }

        // Create TextViews, ImageView, and Switch for layout
        val title = TextView(requireContext()).apply {
            text = "Widget ${++widgetCount}"
            textSize = 16f
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        }

        val location = TextView(requireContext()).apply {
            text = "Location"
            textSize = 14f
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
        }

        val icon = ImageView(requireContext()).apply {
            setImageResource(R.drawable.ic_home) // Replace with your actual icon drawable
            layoutParams = LinearLayout.LayoutParams(48, 48).apply {
                topMargin = 8
                bottomMargin = 8
            }
        }

        val toggle = Switch(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = 8
            }
        }

        // Add components to card
        innerLayout.addView(title)
        innerLayout.addView(location)
        innerLayout.addView(icon)
        innerLayout.addView(toggle)
        cardView.addView(innerLayout)

        gridLayout.addView(cardView)
    }
}
