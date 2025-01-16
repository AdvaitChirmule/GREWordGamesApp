package com.example.grewordgames2.ui.myWords

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.grewordgames2.DatabaseWorker
import com.example.grewordgames2.R
import com.example.grewordgames2.WordClass
import com.example.grewordgames2.WordMetadata
import com.example.grewordgames2.capitalizeEachWord
import com.example.grewordgames2.databinding.FragmentMyWordsBinding
import com.example.grewordgames2.howOld
import com.example.grewordgames2.proficiencyPercentage
import com.example.grewordgames2.setErrorColor
import com.example.grewordgames2.setNormalColor
import com.example.grewordgames2.setSuccessColor
import com.example.grewordgames2.underline
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import okhttp3.OkHttpClient
import okhttp3.Request

class MyWordsFragment : Fragment() {

    private var _binding: FragmentMyWordsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var myWordsStatus: TextView

    lateinit var userTableName: String
    lateinit var dbWorker: DatabaseWorker
    lateinit var db: SQLiteDatabase

    var sortIdDesc = false
    var sortWordsDesc = false
    var sortDateAddedDesc = false
    var sortProficiencyDesc = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val galleryViewModel =
            ViewModelProvider(this).get(MyWordsViewModel::class.java)

        _binding = FragmentMyWordsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        auth = Firebase.auth
        database = Firebase.database.reference

        userTableName = auth.uid!! + "words"
        dbWorker = activity?.let { DatabaseWorker(it) }!!
        db = dbWorker.writableDatabase
        dbWorker.initiateTable(db, userTableName)

        val addWordInput = binding.addWordInput
        val addWordButton = binding.addWordButton
        val refreshButton = binding.refreshButton
        var sortIdButton = binding.srNoHeader
        var sortWordsButton = binding.wordHeader
        var sortDateAddedButton = binding.dateHeader
        var sortProficiencyButton = binding.proficiencyHeader

        myWordsStatus = binding.myWordsStatus
        myWordsStatus.text = "Initiating Table"
        myWordsStatus.setNormalColor()

        refreshButton.underline()

        populateTable(true)

        addWordButton.setOnClickListener(){
            val addWordTextInput = addWordInput.text.toString()
            myWordsStatus.text = "Adding the word '$addWordTextInput' to table"
            myWordsStatus.setNormalColor()
            addWord(addWordTextInput)
        }

        refreshButton.setOnClickListener(){
            myWordsStatus.text = "Refreshing Table"
            myWordsStatus.setNormalColor()
            refreshTable()
        }

        sortIdButton.setOnClickListener(){
            sortById()
        }

        sortWordsButton.setOnClickListener(){
            sortByWords()
        }

        sortDateAddedButton.setOnClickListener(){
            sortByDateAdded()
        }

        sortProficiencyButton.setOnClickListener(){
            sortByProficiency()
        }

        return root
    }

    @SuppressLint("SetTextI18n")
    private fun addWord(wordAdd: String){
        if (wordAdd == "wordCount"){
            myWordsStatus.text = "Sorry, you can not add this word"
            myWordsStatus.setErrorColor()
        }
        else{
            database.child("words").child(wordAdd).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()){
                    addWordToUserDatabase(wordAdd)
                }
                else{
                    lifecycleScope.launch {
                        addWordToGlobalDatabase(wordAdd)
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun addWordToUserDatabase(wordAdd: String){
        database.child("metadata").child(auth.uid!!).get()
            .addOnSuccessListener { snapshot ->
                val wordsRaw = snapshot.child("words").value.toString()
                val wordsClean = wordsRaw.replace(Regex("""[\[\]]"""), "")
                var wordIndexes = wordsClean.split(",").map { it.trim() }

                val dateAddedRaw = snapshot.child("dateAdded").value.toString()
                val dateAddedClean = dateAddedRaw.replace(Regex("""[\[\]]"""), "")
                var dateAddedIndexes = dateAddedClean.split(",").map { it.trim() }

                val proficiencyRaw = snapshot.child("proficiency").value.toString()
                val proficiencyClean = proficiencyRaw.replace(Regex("""[\[\]]"""), "")
                var proficiencyIndexes = proficiencyClean.split(",").map { it.trim() }

                var wordCount = snapshot.child("wordCount").value.toString().toInt()

                if (wordAdd in wordIndexes){
                    myWordsStatus.text = "You have already added the word '$wordAdd' in your table"
                    myWordsStatus.setNormalColor()
                }
                else {
                    if (wordCount == 0){
                        wordIndexes = emptyList()
                        dateAddedIndexes = emptyList()
                        proficiencyIndexes = emptyList()
                    }

                    val allWords = dbWorker.queryTable(db, userTableName)

                    val wordSet: MutableSet<String> = mutableSetOf()
                    val words = allWords.wordsList as? MutableList<String>
                    if (words != null) {
                        wordSet.addAll(words)
                    }

                    val timeNow = (Clock.System.now()).toString()

                    wordIndexes = wordIndexes + wordAdd
                    dateAddedIndexes = dateAddedIndexes + timeNow
                    proficiencyIndexes = proficiencyIndexes + "(0|0)"

                    val wordIndexesString = wordIndexes.toString()
                    val dateAddedString = dateAddedIndexes.toString()
                    val proficiencyString = proficiencyIndexes.toString()

                    wordCount += 1

                    if (wordAdd !in wordSet) {
                        database.child("words").child(wordAdd).child("wordMeaning").get()
                            .addOnSuccessListener { wordSnapshot ->
                                val wordMeaning = wordSnapshot.value.toString()
                                val row = WordMetadata(wordCount, wordAdd, wordMeaning, timeNow, "(0|0)")
                                dbWorker.insertRow(db, userTableName, row)

                            }.addOnFailureListener{
                                Toast.makeText(activity, "Unexpected Error Occurred", Toast.LENGTH_SHORT).show()
                            }
                    }

                    database.child("metadata").child(auth.uid!!).child("words").setValue(wordIndexesString)
                    database.child("metadata").child(auth.uid!!).child("dateAdded").setValue(dateAddedString)
                    database.child("metadata").child(auth.uid!!).child("proficiency").setValue(proficiencyString)
                    database.child("metadata").child(auth.uid!!).child("wordCount").setValue(wordCount)

                    myWordsStatus.text = "The word '$wordAdd' is successfully added to your table!"
                    myWordsStatus.setSuccessColor()

                    populateTable(false)
                }

            }.addOnFailureListener {
                myWordsStatus.text = "Sorry, an unexpected error occurred and we could not add $wordAdd."
                myWordsStatus.setErrorColor()
            }
    }

    @SuppressLint("SetTextI18n")
    private suspend fun addWordToGlobalDatabase(wordAdd: String) {
        return withContext(Dispatchers.IO) {
            try {
                val wordAddLowercase = wordAdd.lowercase()
                val urlWordSearch = "https://api.datamuse.com/words?sl=$wordAddLowercase&max=10&md=d"

                val client = OkHttpClient()
                val request = Request.Builder().url(urlWordSearch).build()
                client.newCall(request).execute().use { response ->
                    if(response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val jsonOutput = org.json.JSONArray(responseBody)

                        var i = 0
                        var wordFound = false
                        var wordName = ""

                        while (i < jsonOutput.length()){
                            val wordMetadata = jsonOutput.getJSONObject(i)
                            wordName = wordMetadata.get("word").toString()

                            if (wordName == wordAdd){
                                wordFound = true
                                val wordDefinition = wordMetadata.get("defs").toString()

                                database.child("words").child("wordCount").get().addOnSuccessListener {
                                    val wordId = it.value.toString().toInt() + 1
                                    val wordDetails = WordClass(wordId, wordDefinition)
                                    database.child("words").child(wordName).setValue(wordDetails)
                                    database.child("words").child("wordCount").setValue(wordId)
                                    addWordToUserDatabase(wordAdd)
                                }

                                break
                            }

                            i += 1
                        }

                        if (wordFound == false){
                            myWordsStatus.text = "Sorry, our database has deemed $wordAdd as invalid. Please try a different word."
                            myWordsStatus.setErrorColor()
                        }
                    }
                    else {
                        myWordsStatus.text = "Sorry, an unexpected error occurred and we could not add $wordAdd."
                        myWordsStatus.setErrorColor()
                    }
                }
            } catch (e: Exception) {
                myWordsStatus.text = "Sorry, an unexpected error occurred and we could not add $wordAdd."
                myWordsStatus.setErrorColor()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun populateTable(landing: Boolean, sortBy: String = "id", desc: Boolean = false){
        val dataTable = binding.allWordsTable

        database.child("metadata").child(auth.uid!!).get()
            .addOnSuccessListener { snapshot ->
                val lastAccessedDevice = snapshot.child("lastAccessedDevice").value.toString()
                val currentDevice = Settings.Secure.getString(requireActivity().contentResolver, Settings.Secure.ANDROID_ID)

                if (lastAccessedDevice != currentDevice){
                    refreshTable()
                }

                val userDatabase = dbWorker.queryTable(db, userTableName, sortBy, desc)

                if (userDatabase.idList.size > 0){
                    dataTable.removeViews(1, dataTable.childCount - 1)
                    val inflater = LayoutInflater.from(activity)

                    for (i in 0..< userDatabase.dateAddedList.size) {
                        val newRow = inflater.inflate(R.layout.table, dataTable, false) as TableRow

                        val col0 = newRow.findViewById<TextView>(R.id.srNoCol)
                        col0.text = (i + 1).toString()

                        val col1 = newRow.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.wordCol).apply {
                            setOnClickListener {
                                openPopUpWord(userDatabase.wordsList[userDatabase.dateAddedList.size - i - 1])
                            }
                        }
                        col1.text = userDatabase.wordsList[userDatabase.dateAddedList.size - i - 1].capitalizeEachWord()

                        val col2 = newRow.findViewById<TextView>(R.id.dateCol)
                        val dateDifference = LocalDate.parse(Clock.System.now().toString().slice(0..9)) - LocalDate.parse(userDatabase.dateAddedList[userDatabase.dateAddedList.size - i - 1].slice(0..9))
                        col2.text = dateDifference.toString().howOld()

                        val col3 = newRow.findViewById<TextView>(R.id.proficiencyCol)
                        val proficiencyWord = userDatabase.proficiencyList[userDatabase.proficiencyList.size - i - 1].proficiencyPercentage()
                        col3.text = "$proficiencyWord %"

                        val col4 = newRow.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.delWord).apply {
                            setOnClickListener {
                                val delWord = userDatabase.wordsList[userDatabase.dateAddedList.size - i - 1]

                                val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                                builder
                                    .setTitle("Confirm Deletion")
                                    .setMessage(Html.fromHtml("Are you sure you want to delete the word <i>$delWord</i> from the table?", Html.FROM_HTML_MODE_COMPACT))
                                    .setNegativeButton("Close") { dialog, which ->
                                    }
                                    .setPositiveButton("Delete") { dialog, which ->
                                        myWordsStatus.text = "Adding the word '$delWord' to table"
                                        myWordsStatus.setNormalColor()
                                        delWord(delWord)
                                    }

                                val dialog: AlertDialog = builder.create()
                                dialog.show()
                            }
                        }
                        col4.text = "X"

                        dataTable.addView(newRow)
                    }


                    if (landing){
                        myWordsStatus.text = ""
                        myWordsStatus.setNormalColor()
                    }
                }
            }
    }

    private fun refreshTable() {
        database.child("metadata").child(auth.uid!!).get()
            .addOnSuccessListener { snapshot ->
                val wordsRaw = snapshot.child("words").value.toString()
                val wordsClean = wordsRaw.replace(Regex("""[\[\]]"""), "")
                val wordIndexes = wordsClean.split(",").map { it.trim() }

                val dateAddedRaw = snapshot.child("dateAdded").value.toString()
                val dateAddedClean = dateAddedRaw.replace(Regex("""[\[\]]"""), "")
                val dateAddedIndexes = dateAddedClean.split(",").map { it.trim() }

                val proficiencyRaw = snapshot.child("proficiency").value.toString()
                val proficiencyClean = proficiencyRaw.replace(Regex("""[\[\]]"""), "")
                val proficiencyIndexes = proficiencyClean.split(",").map { it.trim() }

                val wordCount = snapshot.child("wordCount").value.toString().toInt()

                dbWorker.deleteTable(db, userTableName)
                dbWorker.initiateTable(db, userTableName)

                for (i in 0..wordCount-1){
                    database.child("words").child(wordIndexes[i]).child("wordMeaning").get()
                        .addOnSuccessListener { wordSnapshot ->
                            val wordMeaning = wordSnapshot.value.toString()
                            val row = WordMetadata(i, wordIndexes[i], wordMeaning, dateAddedIndexes[i], proficiencyIndexes[i])
                            dbWorker.insertRow(db, userTableName, row)

                        }.addOnFailureListener{
                            Toast.makeText(activity, "Unexpected Error Occurred", Toast.LENGTH_SHORT).show()
                        }
                }

                populateTable(true)
            }
    }

    private fun openPopUpWord(word: String){
        var currentMeaning = ""
        var rawMeaning = dbWorker.queryWordMeaning(db, userTableName, word)

        rawMeaning = rawMeaning.slice(2..rawMeaning.length-4)
        val rawLines = rawMeaning.split("\",\"")

        for (line in rawLines){
            val cleanLine = line.split("\\t")
            when (cleanLine[0]){
                "adj" -> currentMeaning += "<i>Adjective</i><br>"
                "adv" -> currentMeaning += "<i>Adverb</i><br>"
                "n" -> currentMeaning += "<i>Noun</i><br>"
                "v" -> currentMeaning += "<i>Verb</i><br>"
                else -> {}
            }
            currentMeaning += cleanLine[1] + "<br><br>"
        }


        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setMessage(Html.fromHtml(currentMeaning, Html.FROM_HTML_MODE_COMPACT))
            .setTitle(word.capitalizeEachWord())
            .setNegativeButton("Close") { dialog, which ->

            }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun delWord(delWord: String){
        database.child("metadata").child(auth.uid!!).get()
            .addOnSuccessListener { snapshot ->
                val wordsRaw = snapshot.child("words").value.toString()
                val wordsClean = wordsRaw.replace(Regex("""[\[\]]"""), "")
                val wordIndexes = wordsClean.split(",").map { it.trim() }.toMutableList()

                val dateAddedRaw = snapshot.child("dateAdded").value.toString()
                val dateAddedClean = dateAddedRaw.replace(Regex("""[\[\]]"""), "")
                val dateAddedIndexes = dateAddedClean.split(",").map { it.trim() }.toMutableList()

                val proficiencyRaw = snapshot.child("proficiency").value.toString()
                val proficiencyClean = proficiencyRaw.replace(Regex("""[\[\]]"""), "")
                val proficiencyIndexes = proficiencyClean.split(",").map { it.trim() }.toMutableList()

                var wordCount = snapshot.child("wordCount").value.toString().toInt()

                var wordIterator = 0
                while (wordIterator < wordIndexes.size){
                    if (wordIndexes[wordIterator] == delWord){
                        break
                    }
                    wordIterator += 1
                }

                wordIndexes.removeAt(wordIterator)
                dateAddedIndexes.removeAt(wordIterator)
                proficiencyIndexes.removeAt(wordIterator)

                wordCount -= 1

                val wordIndexesString = wordIndexes.toString()
                val dateAddedString = dateAddedIndexes.toString()
                val proficiencyString = proficiencyIndexes.toString()

                database.child("metadata").child(auth.uid!!).child("words").setValue(wordIndexesString)
                database.child("metadata").child(auth.uid!!).child("dateAdded").setValue(dateAddedString)
                database.child("metadata").child(auth.uid!!).child("proficiency").setValue(proficiencyString)
                database.child("metadata").child(auth.uid!!).child("wordCount").setValue(wordCount)

                myWordsStatus.text = "The word '$delWord' has been successfully removed from your table!"
                myWordsStatus.setSuccessColor()

                populateTable(false)
            }

        dbWorker.deleteRow(db, userTableName, delWord)
    }

    private fun sortById(){
        if (sortIdDesc){
            populateTable(true, "id", false)
            sortIdDesc = false
        }
        else {
            populateTable(true, "id", true)
            sortIdDesc = true
        }
    }

    private fun sortByWords(){
        if (sortWordsDesc){
            populateTable(true, "words", false)
            sortWordsDesc = false
        }
        else {
            populateTable(true, "words", true)
            sortWordsDesc = true
        }
    }

    private fun sortByDateAdded(){
        if (sortDateAddedDesc){
            populateTable(true, "id", false)
            sortDateAddedDesc = false
        }
        else {
            populateTable(true, "id", true)
            sortDateAddedDesc = true
        }
    }

    private fun sortByProficiency(){
        Toast.makeText(activity, "Not implemented yet", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}