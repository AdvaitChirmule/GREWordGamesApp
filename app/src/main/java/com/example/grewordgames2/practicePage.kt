package com.example.grewordgames2

import android.annotation.SuppressLint
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.Html
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.play.integrity.internal.n
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.random.Random

class practicePage : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    var newWordsProficiency = mutableListOf<String>()
    var excellentWordsProficiency = mutableListOf<String>()
    var goodWordsProficiency = mutableListOf<String>()
    var difficultWordsProficiency = mutableListOf<String>()

    var checkWords = mutableListOf<Boolean>()

    private lateinit var submitButton: Button
    private lateinit var wordPlaceholder: TextView
    private lateinit var meaningPlaceholder: TextView
    private lateinit var wrongButton: Button
    private lateinit var rightButton: Button

    var wordCount = 0
    var newWordsCount = 0
    var excellentWordsCount = 0
    var goodWordsCount = 0
    var difficultWordsCount = 0

    var fullList = mutableListOf<String>()
    var randomValuesList = mutableListOf<Int>()
    var randomValuesInt = 0

    var proficiencyIndexes = mutableListOf<String>()

    var currentMeaning = ""

    lateinit var userTableName: String
    lateinit var dbWorker: DatabaseWorker
    lateinit var db: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_practice_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = Firebase.auth
        database = Firebase.database.reference

        userTableName = auth.uid!! + "words"
        dbWorker = DatabaseWorker(this)
        db = dbWorker.writableDatabase
        dbWorker.initiateTable(db, userTableName)

        submitButton = findViewById(R.id.submitButton)
        wordPlaceholder = findViewById(R.id.wordPlaceholder)
        meaningPlaceholder = findViewById(R.id.meaningPlaceholder)
        wrongButton = findViewById(R.id.wrongButton)
        rightButton = findViewById(R.id.rightButton)

        wrongButton.isEnabled = false
        wrongButton.isClickable = false
        rightButton.isEnabled = false
        rightButton.isClickable = false

        submitButton.setOnClickListener(){
            submitButton.isEnabled = false
            submitButton.isClickable = false
            rightButton.isEnabled = true
            rightButton.isClickable = true
            wrongButton.isEnabled = true
            wrongButton.isClickable = true
            getMeaning()
        }

        wrongButton.setOnClickListener(){
            submitButton.isEnabled = true
            submitButton.isClickable = true
            rightButton.isEnabled = false
            rightButton.isClickable = false
            wrongButton.isEnabled = false
            wrongButton.isClickable = false
            getCards(0)
        }

        rightButton.setOnClickListener(){
            submitButton.isEnabled = true
            submitButton.isClickable = true
            rightButton.isEnabled = false
            rightButton.isClickable = false
            wrongButton.isEnabled = false
            wrongButton.isClickable = false
            getCards(1)
        }

        val difficulty = intent.getStringExtra("difficulty")!!.toInt()
        practiceWords(difficulty)
    }

    private fun practiceWords(difficulty: Int){

        val allWords = dbWorker.queryTable(db, userTableName)

        val wordIndexes = allWords.wordsList
        proficiencyIndexes = allWords.proficiencyList
        wordCount = wordIndexes.size

        for (i in 0..wordIndexes.size-1){
            if (proficiencyIndexes[i].tooLow()){
                newWordsProficiency += wordIndexes[i]
                newWordsCount += 1
            }
            else {
                val proficiencyWord = proficiencyIndexes[proficiencyIndexes.size - i - 1].proficiencyPercentage()
                if (proficiencyWord >= 75){
                    excellentWordsProficiency += wordIndexes[i]
                    excellentWordsCount += 1
                }
                else if (proficiencyWord >= 50){
                    goodWordsProficiency += wordIndexes[i]
                    goodWordsCount += 1
                }
                else {
                    difficultWordsProficiency += wordIndexes[i]
                    difficultWordsCount += 1
                }
            }
        }

        if (wordCount == 0){
            Toast.makeText(this, "Please enter words to practice", Toast.LENGTH_SHORT).show()
        }
        else{
            when (difficulty) {
                0 -> fullyRandomClassification()
                1 -> standardClassification()
                2 -> newBiasedClassification()
                3 -> strugglingBiasedClassification()
                4 -> difficultClassification()
                else -> Toast.makeText(this, "Unexpected error occurred", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun fullyRandomClassification() {
        fullList = (newWordsProficiency + excellentWordsProficiency + goodWordsProficiency + difficultWordsProficiency).toMutableList()
        checkWords = (0..fullList.size).map{false}.toMutableList()

        val size = wordCount
        val s = HashSet<Int>(size)
        while (s.size < size) {
            val find = Random.nextInt(wordCount)
            if (find !in s){
                randomValuesList += find
                s += find
            }
        }

        randomValuesInt = 0

        wordPlaceholder.text = fullList[randomValuesList[randomValuesInt]].capitalizeEachWord()
        meaningPlaceholder.text = ""
        randomValuesInt += 1

        val rawMeaning = dbWorker.queryWordMeaning(db, userTableName, fullList[randomValuesList[randomValuesInt]])
        currentMeaning = cleanWordMeaning(rawMeaning)
    }

    private fun standardClassification() {
        Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show()
    }

    private fun newBiasedClassification() {
        Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show()
    }

    private fun strugglingBiasedClassification() {
        Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show()
    }

    private fun difficultClassification() {
        Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("SetTextI18n")
    private fun getCards(result: Int){
        val resultantProficiency = proficiencyIndexes[randomValuesInt-1].recordResult(result)
        proficiencyIndexes[randomValuesInt-1] = resultantProficiency
        dbWorker.updateProficiency(db, userTableName, fullList[randomValuesList[randomValuesInt-1]], resultantProficiency)
        if (randomValuesInt < randomValuesList.size){
            wordPlaceholder.text = fullList[randomValuesList[randomValuesInt]].capitalizeEachWord()
            meaningPlaceholder.text = ""
            currentMeaning = ""

            val rawMeaning = dbWorker.queryWordMeaning(db, userTableName, fullList[randomValuesList[randomValuesInt]])
            currentMeaning = cleanWordMeaning(rawMeaning)

            randomValuesInt += 1
        }
        else {
            database.child("metadata").child(auth.uid!!).child("proficiency").setValue(proficiencyIndexes.toString())
            completePractice()
        }
    }

    private fun getMeaning(){
        meaningPlaceholder.text = Html.fromHtml(currentMeaning, Html.FROM_HTML_MODE_COMPACT)
    }

    private fun completePractice(){
        Toast.makeText(this, "You completed practice", Toast.LENGTH_SHORT).show()
        submitButton.isEnabled = false
        submitButton.isClickable = false
        rightButton.isEnabled = false
        rightButton.isClickable = false
        wrongButton.isEnabled = false
        wrongButton.isClickable = false
    }
}