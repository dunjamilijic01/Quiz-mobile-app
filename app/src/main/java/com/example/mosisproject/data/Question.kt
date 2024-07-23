package com.example.mosisproject.data

import android.net.Uri
import java.text.SimpleDateFormat
import java.util.*

data class Question(val id:String="", val user:String="", val question:String="", val correctAnsw:String="",
                    val answ1:String="", val answ2:String="", val answ3:String="", val answ4:String="", val category:String="",
                    val difficulty:String="", val image: String?="", val longitude:String="", val latitude:String="",val date: String)
