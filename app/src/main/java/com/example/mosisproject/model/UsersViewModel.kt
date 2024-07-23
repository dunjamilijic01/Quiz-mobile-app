package com.example.mosisproject.model

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mosisproject.data.Question
import com.example.mosisproject.data.User
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson

class UsersViewModel :ViewModel(){
    private lateinit var firestore: FirebaseFirestore
    private lateinit var liveDatabase: DatabaseReference
    private lateinit var collectionRef: CollectionReference
    val _users:MutableLiveData<ArrayList<User>> = MutableLiveData<ArrayList<User>>()


    fun getAllUsers() {
        val usersList: ArrayList<User> = ArrayList<User>()

        liveDatabase.get().addOnSuccessListener {
            if (it.getValue() != null) {
                val q: Map<String, Object> = it.getValue() as HashMap<String, Object>
                q.forEach { (key, value) ->
                    run {

                        val gson = Gson()
                        val json = gson.toJson(value)
                        val u = gson.fromJson(json, User::class.java)
                        usersList.add(u)

                    }
                }
            } else {
            }
            _users.value = usersList
            _users.value!!.sortByDescending { it.points }
        }
    }

    fun addUser(u:User, callback: (String?)->Unit)
    {
        u.answeredQuestions= ArrayList<String>()
        _users.value?.add(u)
        liveDatabase.child(u.id).setValue(u).addOnSuccessListener {

            callback.invoke(null)
        }
            .addOnFailureListener {
              callback.invoke(it.message)
            }
    }

    fun updateUser(user:User){

        val liveDb=FirebaseDatabase.getInstance().getReference("Users").child(user.id)
        _users.value!!.removeIf { it.id==user.id }
        _users.value!!.add(user)
        _users.value!!.sortByDescending { it.points }
        val update= mapOf<String,Any>("points" to user.points.toString(),"answeredQuestions" to user.answeredQuestions )
        liveDb.updateChildren(update).addOnSuccessListener {

        }

    }

    init {
        firestore=FirebaseFirestore.getInstance()
        collectionRef=firestore.collection("users")
        liveDatabase = FirebaseDatabase.getInstance().getReference("Users")
    }
}