package app.smarthomeapp

import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_screen)

            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            @Suppress("DEPRECATION")
            window.statusBarColor = android.graphics.Color.TRANSPARENT


        // Find the layout by its ID
        val mainLayout = findViewById<LinearLayout>(R.id.main_layout)

        // Load the gradient drawable and enable dithering
        val gradientDrawable = ContextCompat.getDrawable(this, R.drawable.background_gardient) as GradientDrawable
        gradientDrawable.setDither(true)

        // Set the drawable as the background for the layout
        mainLayout.background = gradientDrawable
    }
}
