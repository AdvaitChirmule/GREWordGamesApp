package com.example.grewordgames2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
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

class registerPage : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var registerStatus: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = Firebase.auth
        database = Firebase.database.reference

        val currentUser = auth.currentUser
        if (currentUser != null){
            alreadyLoggedIn()
        }

        registerStatus = findViewById<TextView>(R.id.registerStatus)

        val emailIDField = findViewById<EditText>(R.id.emailIDRegisterInput)
        val usernameField = findViewById<EditText>(R.id.usernameRegisterInput)
        val passwordField = findViewById<EditText>(R.id.passwordRegisterInput)
        val passwordVerifyField = findViewById<EditText>(R.id.passwordVerifyRegisterInput)

        val registerButton = findViewById<Button>(R.id.registerRegisterButton)
        val loginButton = findViewById<Button>(R.id.loginRegisterButton)

        registerButton?.setOnClickListener(){

            val emailID = emailIDField.text.toString()
            val username = usernameField.text.toString()
            val password = passwordField.text.toString()
            val passwordVerify = passwordVerifyField.text.toString()

            if ((emailID == "") or (username == "") or (password == "") or (passwordVerify == "")) {
                fieldsNotFilled()
            }

            else if (password != passwordVerify){
                notSamePasswords()
            }

            else if (!emailID.contains("@")){
                invalidEmail()
            }

            else {
                registerUser(emailID, password, username)
            }
        }

        loginButton?.setOnClickListener(){
            navigateLogInPage()
        }
    }

    private fun alreadyLoggedIn(){
        Toast.makeText(this, "Sign out to register a new account", Toast.LENGTH_SHORT).show()
        val homePageIntent = Intent(this, Register2Activity::class.java)
        startActivity(homePageIntent)
    }

    @SuppressLint("SetTextI18n")
    private fun fieldsNotFilled(){
        registerStatus.text = "Please fill out all fields"
        registerStatus.setErrorColor()
    }

    @SuppressLint("SetTextI18n")
    private fun notSamePasswords(){
        registerStatus.text = "The passwords don't match"
        registerStatus.setErrorColor()
    }

    @SuppressLint("SetTextI18n")
    private fun invalidEmail(){
        registerStatus.text = "Please provide a valid Email ID"
        registerStatus.setErrorColor()
    }

    @SuppressLint("SetTextI18n", "HardwareIds")
    private fun registerUser(emailID: String, password: String, username: String){
        registerStatus.text = "Registering..."
        registerStatus.setNormalColor()
        auth.createUserWithEmailAndPassword(emailID, password)
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful){
                    val userDetails = UserClass(username, Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID))

                    database.child("metadata").child(auth.uid!!).setValue(userDetails)


                    database.child("metadata").child("userCount").get().addOnSuccessListener {
                        val userCount = it.value.toString().toInt() + 1
                        database.child("metadata").child("userCount").setValue(userCount)
                    }.addOnFailureListener{
                        Toast.makeText(this, "An unexpected error occurred", Toast.LENGTH_SHORT).show()
                    }

                    val homePageIntent = Intent(this, Register2Activity::class.java)
                    startActivity(homePageIntent)
                }
                else{
                    registerStatus.text = "We could not register your account. Verify your details and try again."
                    registerStatus.setErrorColor()
                }
            }
    }

    private fun navigateLogInPage(){
        val registerIntent = Intent(this, loginPage::class.java)
        startActivity(registerIntent)
    }
}