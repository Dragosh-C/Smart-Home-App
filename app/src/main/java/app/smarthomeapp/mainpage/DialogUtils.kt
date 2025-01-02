package app.smarthomeapp.mainpage

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import app.smarthomeapp.R


object DialogHelper {

    fun showDataSelectionDialog(
        context: Context,
        existingWidget: Widget? = null,
        isEditMode: Boolean,
        toggleEditMode: () -> Unit,
        addNewWidget: (String, String, String) -> Unit,
        updateWidget: (Widget) -> Unit,
        editButton: Button
    ) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_select_data)

        val nameInput = dialog.findViewById<EditText>(R.id.name_input)
        val portSpinner = dialog.findViewById<Spinner>(R.id.port_spinner)
        val typeSpinner = dialog.findViewById<Spinner>(R.id.type_spinner)
        val saveButton = dialog.findViewById<Button>(R.id.save_button)
        val modifyButton = dialog.findViewById<Button>(R.id.modify_button)

        val portOptions = (1..9).map { "Port $it" }
        val portAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, portOptions)
        portAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        portSpinner.adapter = portAdapter

        val typeOptions = listOf("Outlet", "Light", "Dimmer")
        val typeAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, typeOptions)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = typeAdapter

        existingWidget?.let {
            nameInput.setText(it.name)
            portSpinner.setSelection(portOptions.indexOf(it.port))
            typeSpinner.setSelection(typeOptions.indexOf(it.type))
        }

        editButton.visibility = View.GONE

        saveButton.setOnClickListener {
            val widgetName = nameInput.text.toString()
            val selectedPort = portSpinner.selectedItem.toString()
            val selectedType = typeSpinner.selectedItem.toString()

            if (widgetName.isNotBlank()) {
                if (existingWidget == null) {
                    // Add a new widget if no existing widget
                    addNewWidget(widgetName, selectedPort, selectedType)
                } else {
                    // Update existing widget
                    updateWidget(
                        existingWidget.copy(
                            name = widgetName,
                            port = selectedPort,
                            type = selectedType
                        )
                    )
                }
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Please enter a name", Toast.LENGTH_SHORT).show()
            }
        }

        modifyButton.setOnClickListener {
            toggleEditMode()
            dialog.dismiss()

                editButton.visibility = View.VISIBLE
                editButton.setOnClickListener {
                    toggleEditMode()
                    editButton.visibility = View.GONE
                }
        }

        val gradientDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 30f
            setColor(Color.parseColor("#1c1d23"))
        }

        val displayMetrics = DisplayMetrics()
        val windowMetrics = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).currentWindowMetrics
        val bounds = windowMetrics.bounds

        displayMetrics.widthPixels = bounds.width()
        displayMetrics.heightPixels = bounds.height()

        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val halfScreenHeight = screenHeight / 1.5

        dialog.window?.setBackgroundDrawable(gradientDrawable)

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width = screenWidth
        layoutParams.height = halfScreenHeight.toInt()
        layoutParams.gravity = Gravity.BOTTOM

        dialog.window?.attributes = layoutParams
        dialog.show()
    }

}
