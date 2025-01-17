package com.example.grewordgames2.ui.home

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.grewordgames2.DatabaseWorker
import com.example.grewordgames2.R
import com.example.grewordgames2.loginPage
import com.example.grewordgames2.Register2Activity
import com.example.grewordgames2.cleanWordMeaning
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

    private lateinit var frontAnimation: AnimatorSet
    private lateinit var backAnimation: AnimatorSet

    private lateinit var frontTextView: TextView
    private lateinit var backTextView: TextView

    private lateinit var flipButton: androidx.appcompat.widget.AppCompatButton
    private lateinit var flipBackButton: androidx.appcompat.widget.AppCompatButton

    lateinit var userTableName: String
    lateinit var dbWorker: DatabaseWorker
    lateinit var db: SQLiteDatabase

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

        userTableName = auth.uid!! + "words"
        dbWorker = activity?.let { DatabaseWorker(it) }!!
        db = dbWorker.writableDatabase
        dbWorker.initiateTable(db, userTableName)

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

        frontTextView = binding.wordOfTheDayFront
        backTextView = binding.wordOfTheDayBack

        frontAnimation = AnimatorInflater.loadAnimator(context, R.animator.word_of_the_day_animation_front) as AnimatorSet
        backAnimation = AnimatorInflater.loadAnimator(context, R.animator.word_of_the_day_animation_back) as AnimatorSet

        val wordOfTheDay = "yield"

        val frontText = "<b>WORD OF THE DAY</b><br><br><br>" + wordOfTheDay.uppercase()

        frontTextView.text = Html.fromHtml(frontText, Html.FROM_HTML_MODE_COMPACT)

        val rawMeaning = dbWorker.queryWordMeaning(db, userTableName, wordOfTheDay)
        val currentMeaning = cleanWordMeaning(rawMeaning)

        backTextView.text = Html.fromHtml(currentMeaning, Html.FROM_HTML_MODE_COMPACT)

        flipButton = binding.wordOfTheDayFront
        flipButton.setOnClickListener(){
            flipWord()
        }

        flipBackButton = binding.wordOfTheDayBack
        flipBackButton.setOnClickListener(){
            flipBackWord()
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

    private fun flipWord(){
        frontAnimation.setTarget(frontTextView)
        backAnimation.setTarget(backTextView)
        frontAnimation.start()
        backAnimation.start()

        flipBackButton.isClickable = true
        flipButton.isClickable = false
    }

    private fun flipBackWord(){
        frontAnimation.setTarget(backTextView)
        backAnimation.setTarget(frontTextView)
        frontAnimation.start()
        backAnimation.start()

        flipButton.isClickable = true
        flipBackButton.isClickable = false
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