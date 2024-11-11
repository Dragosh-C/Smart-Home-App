//package app.smarthomeapp
//
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import androidx.fragment.app.Fragment
//import com.google.firebase.auth.FirebaseAuth
//
//class ProfileFragment : Fragment() {
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//
//        Log.d("ProfileFragment", "onCreateView called")
//
//
//        // Button to logout
//        view?.findViewById<Button>(R.id.signOutButton)?.setOnClickListener {
//            Log.d("ProfileFragment", "Sign out button clicked")
//            FirebaseAuth.getInstance().signOut()
//            val intent = Intent(activity, StartPage::class.java)
//            startActivity(intent)
//
//
//
//
//        }
//
//        return inflater.inflate(R.layout.fragment_profile, container, false)
//    }
//}



package app.smarthomeapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("ProfileFragment", "onCreateView called")
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Button to logout
        val signOutButton = view.findViewById<Button>(R.id.signOutButton)
        signOutButton?.setOnClickListener {
            Log.d("ProfileFragment", "Sign out button clicked")
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, StartPage::class.java)
            startActivity(intent)
        }
    }
}


