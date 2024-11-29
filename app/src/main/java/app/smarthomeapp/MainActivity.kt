//package app.smarthomeapp
//
//import android.graphics.drawable.GradientDrawable
//import android.os.Bundle
//import android.view.View
//import android.widget.LinearLayout
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.content.ContextCompat
//import androidx.fragment.app.Fragment
//import com.google.android.material.bottomnavigation.BottomNavigationView
//
//
//class MainActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.main_screen)
//
//            @Suppress("DEPRECATION")
//            window.decorView.systemUiVisibility =
//                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//            @Suppress("DEPRECATION")
//            window.statusBarColor = android.graphics.Color.TRANSPARENT
//
//
//        // Find the layout by its ID
//        val mainLayout = findViewById<LinearLayout>(R.id.main_layout)
//
//        // Load the gradient drawable and enable dithering
//        val gradientDrawable = ContextCompat
//            .getDrawable(this, R.drawable.background_gardient) as GradientDrawable
//        gradientDrawable.setDither(true)
//
//        // Set the drawable as the background for the layout
//        mainLayout.background = gradientDrawable
//
//
//        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
//
//        // Set default selected item
//        bottomNavigationView.selectedItemId = R.id.nav_home
//        loadFragment(HomeFragment()) // Show the Home fragment initially
//
//        // Handle navigation item clicks
//        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.nav_home -> {
//                    loadFragment(HomeFragment())
//                    true
//                }
//                R.id.nav_scenarios -> {
//                    loadFragment(ScenariosFragment())
//                    true
//                }
//                R.id.nav_profile -> {
//                    loadFragment(ProfileFragment())
//                    true
//                }
//                else -> false
//            }
//        }
//    }
//
//    // load a fragment
//    private fun loadFragment(fragment: Fragment) {
//        val transaction = supportFragmentManager.beginTransaction()
//        transaction.replace(R.id.fragment_container, fragment)
//        transaction.commit()
//    }
//
//}

package app.smarthomeapp

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_screen)


        enableEdgeToEdge()
        // Set background gradient
        val mainLayout = findViewById<android.widget.LinearLayout>(R.id.main_layout)
        val gradientDrawable = resources.getDrawable(R.drawable.background_gardient) as GradientDrawable
        gradientDrawable.setDither(true)
        mainLayout.background = gradientDrawable

        // BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_home
        loadFragment(HomeFragment())

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_scenarios -> {
                    loadFragment(ScenariosFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }

                R.id.nav_camera -> {
                    loadFragment(CameraFragment())
                    true
                }
                else -> false
            }
        }
    }

    // load fragments
    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }
}
