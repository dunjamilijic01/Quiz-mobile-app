package com.example.mosisproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.mosisproject.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding:ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth=FirebaseAuth.getInstance()

        if(firebaseAuth.currentUser!=null){
            val i:Intent=Intent(this,MainActivity::class.java)
            startActivity(i)
            finish()
        }

        binding.loginBtn.setOnClickListener {
            val email=binding.loginEmail.text.toString()
            val pass=binding.loginPassword.text.toString()

            if(email.isNotEmpty() &&pass.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email,pass).addOnSuccessListener {
                        val intent= Intent(this,MainActivity::class.java)
                        startActivity(intent)
                        finish()
                }.addOnFailureListener {
                    Toast.makeText(this,it.message, Toast.LENGTH_SHORT).show()

                }
            }else{
                Toast.makeText(this,"Must fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.signupRedirectText.setOnClickListener {
            val registerIntent=Intent(this,SignupActivity::class.java)
            startActivity(registerIntent)
            finish()
        }
    }
}