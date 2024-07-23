package com.example.mosisproject

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.mosisproject.model.UsersViewModel
import com.google.firebase.auth.FirebaseAuth


class ProfileFragment : Fragment() {

    private val usersViewModel: UsersViewModel by activityViewModels()
    private lateinit var firebaseAuth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view= inflater.inflate(R.layout.fragment_profile, container, false)
        firebaseAuth=FirebaseAuth.getInstance()
        val user=usersViewModel._users.value!!.find { it.id==firebaseAuth.currentUser!!.uid }

        val activity=activity as AppCompatActivity
        activity.getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)

        val image=view.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.profileImage)
        val username=view.findViewById<TextView>(R.id.usernameTextView)
        val name=view.findViewById<TextView>(R.id.fullNameTextView)
        val phone=view.findViewById<TextView>(R.id.phoneNumTextView)

        Glide.with(context!!).load(user!!.image).into(image)
        username.setText(user.email)
        name.setText(user.firstName+" "+user.lastName)
        phone.setText(user.phone)

        return view
    }

}