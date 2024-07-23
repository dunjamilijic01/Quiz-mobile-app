package com.example.mosisproject.data

data class User(val id:String,val email:String,val password:String,val firstName:String,val lastName:String,val phone:String,val image:String,var points:Int=0,var answeredQuestions:ArrayList<String> =ArrayList<String>())
