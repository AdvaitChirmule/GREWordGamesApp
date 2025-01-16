package com.example.grewordgames2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class loginPage : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var loginStatus: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = Firebase.auth
        database = Firebase.database.reference

        if (auth.currentUser != null){
            alreadyLoggedIn()
        }

        loginStatus = findViewById<TextView>(R.id.loginStatus)

        val emailIDField = findViewById<EditText>(R.id.usernameInput)
        val passwordField = findViewById<EditText>(R.id.passwordInput)

        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)

        loginButton?.setOnClickListener(){
            val email = emailIDField.text.toString()
            val password = passwordField.text.toString()
            if ((email == "") or (password == "")) {
                fieldsNotFilled()
            }
            else {
                logIn(email, password)
            }
        }

        registerButton?.setOnClickListener(){
            navigateRegisterPage()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun fieldsNotFilled(){
        loginStatus.text = "Please fill out all fields"
        loginStatus.setErrorColor()
    }

    private fun alreadyLoggedIn(){
        Toast.makeText(this, "Sign out to login via a different account", Toast.LENGTH_SHORT).show()
        val homePageIntent = Intent(this, Register2Activity::class.java)
        startActivity(homePageIntent)
    }

    @SuppressLint("SetTextI18n")
    private fun logIn(email: String, password: String){
        loginStatus.text = "Logging in..."
        loginStatus.setNormalColor()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful){
                    Toast.makeText(this, "Successfully logged in", Toast.LENGTH_SHORT).show()
                    database.child("metadata").child(auth.uid!!).child("stateLoggedIn").setValue(true)
                    val homePageIntent = Intent(this, Register2Activity::class.java)
                    startActivity(homePageIntent)
                }
                else{
                    loginStatus.text = "Incorrect credentials provided, please try again."
                    loginStatus.setErrorColor()
                }
            }
    }

    private fun navigateRegisterPage(){
        val registerIntent = Intent(this, registerPage::class.java)
        startActivity(registerIntent)
    }
}