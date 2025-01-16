package com.example.grewordgames2

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.TextView

fun TextView.underline() {
    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
}

fun TextView.setErrorColor() {
    setTextColor(Color.parseColor("#8b0000"))
}

fun TextView.setNormalColor() {
    setTextColor(Color.parseColor("#000000"))
}

fun TextView.setSuccessColor() {
    setTextColor(Color.parseColor("#008b00"))
}

fun String.capitalizeEachWord() : String{
    return this.split(" ").map {
        if(it.contains("/")){
            it.split("/").map { word -> word.replaceFirstChar { firstChar -> firstChar.uppercase() }
            }.joinToString("/")
        }else{
            it.replaceFirstChar { firstChar -> firstChar.uppercase() }
        }
    }.joinToString(" ")
}

fun String.howOld(): String{
    for(i in 1..this.length-1){
        if (this[i] == 'Y'){
            val value = this.slice(1..i-1)
            if (value == "1"){
                return "1 year ago"
            }
            else{
                return "$value years ago"
            }
        }
        else if (this[i] == 'M'){
            val value = this.slice(1..i-1)
            if (value == "1"){
                return "1 month ago"
            }
            else {
                return "$value months ago"
            }
        }
        else if (this[i] == 'D') {
            val value = this.slice(1..i-1).toInt()
            if (value >= 14){
                val weeksString = (value / 7).toInt().toString()
                return "$weeksString weeks ago"
            }
            else if (value >= 7){
                return "1 week ago"
            }
            else if (value >= 2){
                return "$value days ago"
            }
            else if (value == 1){
                return "yesterday"
            }
            else {
                return "today"
            }
        }
    }
    return "error"
}

fun String.proficiencyPercentage(): Int{
    for(i in 1..this.length-2){
        if (this[i] == '|'){
            val num = this.slice(1..i-1).toInt()
            val den = this.slice(i+1..this.length-2).toInt()

            if (num == 0 && den == 0){
                val perc = 0
                return perc
            }
            else{
                val perc = 100 * num / den
                return perc
            }
        }
    }
    return -1
}

fun String.tooLow(): Boolean{
    for(i in 1..this.length-2){
        if (this[i] == '|'){
            val den = this.slice(i+1..this.length-2).toInt()

            if(den <= 5){
                return true
            }
            else{
                return false
            }
        }
    }
    return true
}

fun String.recordResult(result: Int): String{
    for(i in 1..this.length-2){
        if (this[i] == '|'){
            var num = this.slice(1..i-1).toInt()
            var den = this.slice(i+1..this.length-2).toInt()

            num += result
            den += 1
            return "($num|$den)"
        }
    }
    return "error"
}

fun isOnline(context: Context): Boolean{
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

    if (capabilities!= null){
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)){
            return true
        }
        else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)){
            return true
        }
        else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)){
            return true
        }
    }

    return false
}