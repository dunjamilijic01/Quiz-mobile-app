package com.example.mosisproject

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.mosisproject.model.UsersViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage


class QuestionFragment : Fragment() {

    private val usersViewModel: UsersViewModel by activityViewModels()
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storage:FirebaseStorage


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        firebaseAuth=FirebaseAuth.getInstance()
        storage=FirebaseStorage.getInstance()


        val bottomNav=activity!!.findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.visibility=View.GONE

       // val clbck=OnBackPressedCallback
        val act=activity as AppCompatActivity
        act.onBackPressedDispatcher.addCallback(this,object :OnBackPressedCallback(true){
            override fun handleOnBackPressed() {

            }
        })

        val bundle=this.arguments

        val view=inflater.inflate(R.layout.fragment_question, container, false)
        view.findViewById<TextView>(R.id.questionText).text=bundle!!.getString("question")
        view.findViewById<Button>(R.id.questionAnsw1).text=bundle!!.getString("answer1")
        view.findViewById<Button>(R.id.questionAnsw2).text=bundle!!.getString("answer2")
        view.findViewById<Button>(R.id.questionAnsw3).text=bundle!!.getString("answer3")
        view.findViewById<Button>(R.id.questionAnsw4).text=bundle!!.getString("answer4")

        val imgView=view.findViewById<ImageView>(R.id.questionImage)
        var imgName=bundle!!.getString("image")

        if(imgName==null){
            imgView.visibility=View.GONE

        }
        else{
            imgView.visibility=View.VISIBLE
        }
        Glide.with(context!!).load(imgName).into(imgView)

        val user=usersViewModel._users.value!!.find { it.id==firebaseAuth.currentUser!!.uid }
        view.findViewById<Button>(R.id.questionAnsw1).setOnClickListener {
            if ( view.findViewById<Button>(R.id.questionAnsw1).text==bundle.getString("correctAnswer"))
            {
                user!!.points+=15
                if(user!!.answeredQuestions==null)
                    user!!.answeredQuestions=ArrayList<String>()
                user!!.answeredQuestions.add(bundle!!.getString("id")!!)
                usersViewModel.updateUser(user)

                val dialog= AlertDialog.Builder(context)
                dialog.setTitle("Question answered")
                dialog.setMessage("The answer is CORRECT. You gained 15 points")
                dialog.setPositiveButton("OK", DialogInterface.OnClickListener{ dialog, id->
                    dialog.cancel()
                })
                dialog.create().show()

            }
            else{
                user!!.points-=5
                if(user!!.answeredQuestions==null)
                    user!!.answeredQuestions=ArrayList<String>()
                user!!.answeredQuestions.add(bundle!!.getString("id")!!)
                usersViewModel.updateUser(user)

                val dialog= AlertDialog.Builder(context)
                dialog.setTitle("Question answered")
                dialog.setMessage("The answer is WRONG. You lost 5 points")
                dialog.setPositiveButton("OK", DialogInterface.OnClickListener{ dialog, id->
                    dialog.cancel()
                })
                dialog.create().show()
            }

            parentFragmentManager.popBackStack()
        }
        view.findViewById<Button>(R.id.questionAnsw2).setOnClickListener {
            if ( view.findViewById<Button>(R.id.questionAnsw2).text==bundle.getString("correctAnswer"))
            {
                user!!.points+=15
                if(user!!.answeredQuestions==null)
                    user!!.answeredQuestions=ArrayList<String>()
                user!!.answeredQuestions.add(bundle!!.getString("id")!!)
                usersViewModel.updateUser(user)

                val dialog= AlertDialog.Builder(context)
                dialog.setTitle("Question answered")
                dialog.setMessage("The answer is CORRECT. You gained 15 points")
                dialog.setPositiveButton("OK", DialogInterface.OnClickListener{ dialog, id->
                    dialog.cancel()
                })
                dialog.create().show()
            }
            else{
                user!!.points-=5
                if(user!!.answeredQuestions==null)
                    user!!.answeredQuestions=ArrayList<String>()
                user!!.answeredQuestions.add(bundle!!.getString("id")!!)
                usersViewModel.updateUser(user)

                val dialog= AlertDialog.Builder(context)
                dialog.setTitle("Question answered")
                dialog.setMessage("The answer is WRONG. You lost 5 points")
                dialog.setPositiveButton("OK", DialogInterface.OnClickListener{ dialog, id->
                    dialog.cancel()
                })
                dialog.create().show()
            }
            parentFragmentManager.popBackStack()
        }
        view.findViewById<Button>(R.id.questionAnsw3).setOnClickListener {
            if ( view.findViewById<Button>(R.id.questionAnsw3).text==bundle.getString("correctAnswer"))
            {
                user!!.points+=15
                if(user!!.answeredQuestions==null)
                    user!!.answeredQuestions=ArrayList<String>()
                user!!.answeredQuestions.add(bundle!!.getString("id")!!)
                usersViewModel.updateUser(user)

                val dialog= AlertDialog.Builder(context)
                dialog.setTitle("Question answered")
                dialog.setMessage("The answer is CORRECT. You gained 15 points")
                dialog.setPositiveButton("OK", DialogInterface.OnClickListener{ dialog, id->
                    dialog.cancel()
                })
                dialog.create().show()
            }
            else{
                user!!.points-=5
                if(user!!.answeredQuestions==null)
                    user!!.answeredQuestions=ArrayList<String>()
                user!!.answeredQuestions.add(bundle!!.getString("id")!!)
                usersViewModel.updateUser(user)

                val dialog= AlertDialog.Builder(context)
                dialog.setTitle("Question answered")
                dialog.setMessage("The answer is WRONG. You lost 5 points")
                dialog.setPositiveButton("OK", DialogInterface.OnClickListener{ dialog, id->
                    dialog.cancel()
                })
                dialog.create().show()
            }
            parentFragmentManager.popBackStack()
        }
        view.findViewById<Button>(R.id.questionAnsw4).setOnClickListener {
            if ( view.findViewById<Button>(R.id.questionAnsw1).text==bundle.getString("correctAnswer"))
            {
                user!!.points+=15
                if(user!!.answeredQuestions==null)
                    user!!.answeredQuestions=ArrayList<String>()
                user!!.answeredQuestions.add(bundle!!.getString("id")!!)
                usersViewModel.updateUser(user)

                val dialog= AlertDialog.Builder(context)
                dialog.setTitle("Question answered")
                dialog.setMessage("The answer is CORRECT. You gained 15 points")
                dialog.setPositiveButton("OK", DialogInterface.OnClickListener{ dialog, id->
                    dialog.cancel()
                })
                dialog.create().show()
            }
            else{
                user!!.points-=5
                if(user!!.answeredQuestions==null)
                    user!!.answeredQuestions=ArrayList<String>()
                user!!.answeredQuestions.add(bundle!!.getString("id")!!)
                usersViewModel.updateUser(user)

                val dialog= AlertDialog.Builder(context)
                dialog.setTitle("Question answered")
                dialog.setMessage("The answer is WRONG. You lost 5 points")
                dialog.setPositiveButton("OK", DialogInterface.OnClickListener{ dialog, id->
                    dialog.cancel()
                })
                dialog.create().show()
            }
            parentFragmentManager.popBackStack()
        }
        return view
    }
}