package com.example.grewordgames2

import android.provider.BaseColumns

data class UserClass(val username: String,
                     val lastAccessedDevice: String,
                     val stateLoggedIn: Boolean = true,
                     val stateWaitingRoom: Boolean = false,
                     val stateRoomNumber: Int = -1,
                     val statePlaying: Boolean = false,
                     val wordCount: Int = 0,
                     val words: String = "[]",
                     val dateAdded: String = "[]",
                     val proficiency: String = "[]")

data class WordClass(val id: Int,
                     val wordMeaning: String,
                     val dateAdded: String? = null)

data class WordMetadata(val id: Int,
                        val words: String,
                        val wordMeaning: String,
                        val dateAdded: String,
                        val proficiency: String)

data class LocalDatabase(val idList: MutableList<Int>,
                         val wordsList: MutableList<String>,
                         val meaningList: MutableList<String>,
                         val dateAddedList: MutableList<String>,
                         val proficiencyList: MutableList<String>)

data class FirebaseDatabase(val wordsList: MutableList<String>,
                            val dateAddedList: MutableList<String>,
                            val proficiencyList: MutableList<String>)