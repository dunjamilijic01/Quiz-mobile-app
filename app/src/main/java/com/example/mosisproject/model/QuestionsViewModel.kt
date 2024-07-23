package com.example.mosisproject.model

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mosisproject.data.Question
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson

class QuestionsViewModel:ViewModel() {

    private lateinit var liveDatabase: DatabaseReference
    val  _questions:MutableLiveData<ArrayList<Question>> = MutableLiveData<ArrayList<Question>>()
    val questions:LiveData<ArrayList<Question>>
            get()=_questions

    fun addQuestion(q:Question){

        _questions.value?.add(q)

        val key=liveDatabase.push().key

           liveDatabase.child(key!!).setValue(q).addOnCompleteListener {

           }.addOnFailureListener{
           }
    }

    fun getAllQuestions(){
        val qs: ArrayList<Question> = ArrayList<Question>()

        liveDatabase.get().addOnSuccessListener {
            if(it.getValue()!=null) {
                val q: Map<String, Object> = it.getValue() as HashMap<String, Object>
                q.forEach { (key, value) ->
                    run {

                        val gson = Gson()
                        val json = gson.toJson(value)
                        val question = gson.fromJson(json, Question::class.java)
                        qs.add(question)

                    }
                }
            }
            else{

            }
            _questions.value=qs

        }

    }

    init {
        liveDatabase = FirebaseDatabase.getInstance().getReference("Questions")
    }

}