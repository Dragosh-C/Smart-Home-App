//package app.smarthomeapp.sign_in
//
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Toast
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import app.smarthomeapp.StartPage
//import app.smarthomeapp.databinding.ActivityRegisterBinding
//import com.google.firebase.auth.FirebaseAuth
//
//class Register : AppCompatActivity() {
//
//    private lateinit var binding: ActivityRegisterBinding
//    private lateinit var firebaseAuth: FirebaseAuth
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        binding = ActivityRegisterBinding.inflate(layoutInflater)
//
//
//        binding.backButton.setOnClickListener {
//            val intent = Intent(this, StartPage::class.java)
//            startActivity(intent)
//        }
//
//        setContentView(binding.root)
//
//        firebaseAuth = FirebaseAuth.getInstance()
//
//
//
//        binding.register.setOnClickListener {
//            val email = binding.emailInput.text.toString()
//            val password = binding.passwordInput.text.toString()
//
//            if (email.isEmpty() || password.isEmpty()) {
//                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
//            }
//            else {
//                firebaseAuth.createUserWithEmailAndPassword(email, password)
//                    .addOnCompleteListener { task ->
//                        if (task.isSuccessful) {
//                            Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
//                            val intent = Intent(this, Login::class.java)
//                            startActivity(intent)
//
//                        }
//                        else {
//                            Toast.makeText(this, "Account creation failed", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//            }
//        }
//    }
//}


package app.smarthomeapp.sign_in

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import app.smarthomeapp.StartPage
import app.smarthomeapp.MainActivity
import app.smarthomeapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Register : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleAuthClient: GoogleAuthClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize binding, FirebaseAuth, and GoogleAuthClient
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        googleAuthClient = GoogleAuthClient(applicationContext)

        // Back button to navigate to StartPage
        binding.backButton.setOnClickListener {
            val intent = Intent(this, StartPage::class.java)
            startActivity(intent)
        }

        // Register button using email and password
        binding.register.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            } else {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, Login::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "Account creation failed", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        // Google Sign-In button
        binding.googleSignUpButton.setOnClickListener {
            googleSignIn()
        }
    }

    private fun googleSignIn() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Call googleAuthClient to sign in with Google
                val isSignedIn = googleAuthClient.signIn()
                if (isSignedIn) {
                    Toast.makeText(this@Register, "Google sign-in successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@Register, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@Register, "Google sign-in failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@Register, "Google sign-in error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
