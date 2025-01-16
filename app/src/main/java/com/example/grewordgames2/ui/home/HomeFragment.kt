package com.example.grewordgames2.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.grewordgames2.loginPage
import com.example.grewordgames2.Register2Activity
import com.example.grewordgames2.databinding.FragmentHomeBinding
import com.example.grewordgames2.setNormalColor
import com.example.grewordgames2.underline
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        auth = Firebase.auth
        database = Firebase.database.reference

        if(auth.currentUser != null){
            welcomeUser()
        }
        else{
            notLoggedIn()
        }

        initiateVideo()

        val signOutButton = binding.signOutHomePageButton
        signOutButton.underline()

        signOutButton.setOnClickListener(){
            signOut()
        }

        val accessWordsButton = binding.accessWordsButton
        accessWordsButton.setOnClickListener(){
            accessWords()
        }

        val accessPracticeButton = binding.accessPracticeButton
        accessPracticeButton.setOnClickListener(){
            accessPractice()
        }

        val accessAboutGameButton = binding.accessAboutGame
        accessAboutGameButton.setOnClickListener(){
            accessAboutGame()
        }

        return root
    }

    private fun initiateVideo() {
        val videoViewHomePage: VideoView = binding.videoViewHomePage
    }

    private fun signOut(){
        database.child("metadata").child(auth.uid!!).child("stateLoggedIn").setValue(false)
        Firebase.auth.signOut()
        val welcomeIntent = Intent(activity, loginPage::class.java)
        startActivity(welcomeIntent)
    }

    private fun notLoggedIn(){
        Toast.makeText(activity, "Please login first", Toast.LENGTH_SHORT).show()
        val welcomeIntent = Intent(activity, loginPage::class.java)
        startActivity(welcomeIntent)
    }

    @SuppressLint("SetTextI18n")
    private fun welcomeUser(){
        val welcomeUser = binding.welcomeUser
        database.child("metadata").child(auth.uid!!).child("username").get().addOnSuccessListener {
            val username = it.value.toString()
            welcomeUser.text = "Welcome $username"
            welcomeUser.setNormalColor()

        }.addOnFailureListener{
            Toast.makeText(activity, "Ran into an unexpected error, please login again", Toast.LENGTH_SHORT).show()
        }
    }

    private fun accessWords(){
        val accessWordsIntent = Intent(activity, Register2Activity::class.java).apply{
            putExtra("Fragment", "myWords")
        }
        startActivity(accessWordsIntent)
    }

    private fun accessPractice(){
        val accessWordsIntent = Intent(activity, Register2Activity::class.java).apply{
            putExtra("Fragment", "practice")
        }
        startActivity(accessWordsIntent)
    }

    private fun accessAboutGame(){
        val accessWordsIntent = Intent(activity, Register2Activity::class.java).apply{
            putExtra("Fragment", "aboutGame")
        }
        startActivity(accessWordsIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}