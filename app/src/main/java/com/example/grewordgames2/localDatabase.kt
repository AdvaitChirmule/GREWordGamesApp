package com.example.grewordgames2

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseWorker (context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase){
        println("initiated database")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int){
        println("do nothing")
    }

    fun initiateTable(db: SQLiteDatabase, tableStringName: String){
        db.execSQL("CREATE TABLE IF NOT EXISTS $tableStringName (id Int, words TEXT PRIMARY KEY, meaning TEXT, dateAdded TEXT, proficiency TEXT)" )
    }

    fun queryTable(db: SQLiteDatabase, userTableName: String, orderColumn: String = "id", descending: Boolean = false): LocalDatabase {
        var orderColumnQuery = orderColumn
        if (descending){
            orderColumnQuery += " DESC"
        }

        val cursor = db.query(
            userTableName,
            null,
            null,
            null,
            null,
            null,
            orderColumnQuery
        )

        val wordList = mutableListOf<String>()
        val idList = mutableListOf<Int>()
        val meaningList = mutableListOf<String>()
        val dateAddedList = mutableListOf<String>()
        val proficiencyList = mutableListOf<String>()

        with(cursor) {
            while (moveToNext()) {
                val itemId = getInt(getColumnIndexOrThrow("id"))
                val itemWords = getString(getColumnIndexOrThrow("words"))
                val itemMeaning = getString(getColumnIndexOrThrow("meaning"))
                val itemDateAdded = getString(getColumnIndexOrThrow("dateAdded"))
                val itemProficiency = getString(getColumnIndexOrThrow("proficiency"))

                idList.add(itemId)
                wordList.add(itemWords)
                meaningList.add(itemMeaning)
                dateAddedList.add(itemDateAdded)
                proficiencyList.add(itemProficiency)
            }
        }

        cursor.close()

        val wordsMetadata = LocalDatabase(idList, wordList, meaningList, dateAddedList, proficiencyList)

        return wordsMetadata
    }

    fun insertRow(db: SQLiteDatabase, tableStringName: String, row: WordMetadata){
        val values = ContentValues().apply {
            put("id", row.id)
            put("words", row.words)
            put("meaning", row.wordMeaning)
            put("dateAdded", row.dateAdded)
            put("proficiency", row.proficiency)
        }
        db.insert(tableStringName, null, values)
    }

    fun deleteRow(db: SQLiteDatabase, tableStringName: String, wordDel: String){
        val deleteLine1 = "words = ?"
        val deleteLine2 = arrayOf(wordDel)
        db.delete(tableStringName, deleteLine1, deleteLine2)
    }

    fun queryWordMeaning(db: SQLiteDatabase, userTableName: String, word: String): String{
        val cursor = db.query(
            userTableName,
            null,
            "words = ?",
            arrayOf(word),
            null,
            null,
            null
        )

        var meaning = ""
        with(cursor) {
            while(moveToNext()){
                meaning = getString(getColumnIndexOrThrow("meaning"))
            }
        }
        cursor.close()

        return meaning
    }

    fun updateProficiency(db: SQLiteDatabase, tableStringName: String, word: String, newProficiency: String) {
        val values = ContentValues().apply {
            put("proficiency", newProficiency)
        }
        db.update(
            tableStringName,
            values,
            "words = ?",
            arrayOf(word)
        )
    }


    fun deleteTable(db: SQLiteDatabase, tableStringName: String){
        db.execSQL("DROP TABLE IF EXISTS $tableStringName")
        println("deleted database")
    }

    companion object {
        const val DATABASE_NAME = "users.db"
        const val DATABASE_VERSION = 1
    }
}