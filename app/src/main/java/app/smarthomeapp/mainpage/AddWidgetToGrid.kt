package app.smarthomeapp.mainpage

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import android.view.ViewOutlineProvider
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat

import androidx.appcompat.widget.SwitchCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import app.smarthomeapp.R
import app.smarthomeapp.mainpage.WidgetDetailsActivityDevice

fun addWidgetToGridLayout(
    context: Context,
    widget: Widget,
    gridLayout: GridLayout,
    db: FirebaseFirestore,
    auth: FirebaseAuth,
    isEditMode: Boolean,
    widgetViews: MutableMap<String, SwitchCompat>
) {
    val cardView = CardView(context).apply {
        setBackgroundResource(R.drawable.rounded_background)
    }

    val params = GridLayout.LayoutParams().apply {
        width = 0
        height = GridLayout.LayoutParams.WRAP_CONTENT
        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        setMargins(16, 16, 16, 16)
    }
    cardView.layoutParams = params

    val innerLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(16, 16, 16, 16)
        setBackgroundResource(R.drawable.rounded_background)
    }

    val title = TextView(context).apply {
        text = widget.name.replaceFirstChar { it.uppercase() }
        textSize = 18f
        setTextColor(ContextCompat.getColor(context, android.R.color.white))
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        setPadding(30, 0, 0, 0)
    }

    val portText = TextView(context).apply {
        text = widget.port
        textSize = 12f
        setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        setPadding(30, 0, 0, 0)
    }

    val typeText = TextView(context).apply {
        text = widget.type
        textSize = 12f
        setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        setPadding(30, 0, 0, 0)
    }

    val iconAndTextLayout = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.START
        setPadding(0, 20, 20, 0)
    }

    val textLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        weightSum = 1f
        setPadding(0, 0, 20, 0)
    }

    val toggle = SwitchCompat(context).apply {
        isChecked = widget.isEnabled
        tag = widget.name
        setOnCheckedChangeListener { _, newCheckedState ->
            db.collection("users").document(auth.currentUser!!.uid).collection("widgets")
                .document(widget.name)
                .update("isEnabled", newCheckedState)
                .addOnSuccessListener {
                    Log.d("Widget", "Widget state updated: ${widget.name} = $newCheckedState")
                }
            widget.isEnabled = newCheckedState
        }
    }

    widgetViews[widget.name] = toggle
    textLayout.addView(portText)
    textLayout.addView(typeText)
    textLayout.addView(toggle)

    val icon = ImageView(context).apply {
        setImageResource(
            when (widget.type) {
                "Outlet" -> R.drawable.ic_outlet
                "Dimmer" -> R.drawable.ic_dimmer
                "Light" -> R.drawable.ic_light
                "Device" -> R.drawable.ic_device
                else -> R.drawable.ic_add
            }
        )
        layoutParams = LinearLayout.LayoutParams(172, 172).apply {
            marginStart = 26
            gravity = Gravity.CENTER_VERTICAL
        }
        clipToOutline = true
        outlineProvider = ViewOutlineProvider.BACKGROUND
    }

    iconAndTextLayout.addView(textLayout)
    iconAndTextLayout.addView(icon)

    if (isEditMode) {
        val deleteButton = Button(context).apply {
            text = "X"
            setOnClickListener {
                deleteWidget(db, auth, widget, context, gridLayout, cardView)
            }
        }
        innerLayout.addView(deleteButton)
    }

    innerLayout.addView(title)
    innerLayout.addView(iconAndTextLayout)
    cardView.addView(innerLayout)
    gridLayout.addView(cardView)
    // open a new activity when the card view is clicked


    if (widget.type == "Dimmer") {
        cardView.setOnClickListener {
            val intent = Intent(context, WidgetDetailActivity::class.java).apply {
                putExtra("widget_name", widget.name)
                putExtra("widget_type", widget.type)
                putExtra("widget_port", widget.port)
            }
            context.startActivity(intent)
        }
    } else if (widget.type == "Device") {
        cardView.setOnClickListener {
            val intent = Intent(context, WidgetDetailsActivityDevice::class.java).apply {
                putExtra("widget_name", widget.name)
                putExtra("widget_type", widget.type)
                putExtra("widget_port", widget.port)
            }
            context.startActivity(intent)
        }
    }
}

private fun deleteWidget(
    db: FirebaseFirestore,
    auth: FirebaseAuth,
    widget: Widget,
    context: Context,
    gridLayout: GridLayout,
    cardView: CardView
) {
    db.collection("users").document(auth.currentUser!!.uid).collection("rooms")
        .get()
        .addOnSuccessListener { result ->
            for (document in result) {
                val room = document.data
                val roomName = room["name"] as String
                val isSelected = room["isSelected"] as Boolean
                if (isSelected) {
                    db.collection("users").document(auth.currentUser!!.uid)
                        .collection("rooms").document(roomName)
                        .collection("widgets").document(widget.name).delete()
                        .addOnSuccessListener {
                            Toast.makeText(
                                context,
                                "Widget deleted",
                                Toast.LENGTH_SHORT
                            ).show()
                            gridLayout.removeView(cardView)
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                context,
                                "Failed to delete widget",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
        }
}
