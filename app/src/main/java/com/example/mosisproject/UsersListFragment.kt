package com.example.mosisproject

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.mosisproject.data.User
import com.example.mosisproject.model.UsersViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class UsersListFragment : Fragment() {

    private val usersViewModel:UsersViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_users_list, container, false)

        val bottomNav=activity!!.findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.visibility=View.VISIBLE

        val act=activity as AppCompatActivity
        act.onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {

            }
        })

        val activity=activity as MainActivity
        val id=resources.getIdentifier("usersListFragment", "id", context!!.packageName)
        activity.setSelectedNavBarId(id)

        usersViewModel._users.observe(viewLifecycleOwner, Observer {
            val uris=ArrayList<String>()
            val name=ArrayList<String>()
            val points=ArrayList<String>()
            val users=ArrayList<User>()
            if(usersViewModel._users.value!=null){
                usersViewModel._users.value!!.forEach{
                    uris.add(it.image)
                    name.add(it.firstName+" "+it.lastName)
                    points.add(it.points.toString())
                    users.add(it)
                }
                val listView=view.findViewById<ListView>(R.id.users_list)
                val myAdapter=MyUsersListAdapter(activity!!,users)
                listView.adapter=myAdapter
                listView.setOnItemClickListener { adapterView, view, i, l ->  }
                val searchView=view.findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView)

                searchView.setOnQueryTextListener(object :androidx.appcompat.widget.SearchView.OnQueryTextListener{
                    override fun onQueryTextChange(p0: String?): Boolean {
                        myAdapter.filter.filter(p0)
                        return false
                    }

                    override fun onQueryTextSubmit(p0: String?): Boolean {
                        return false
                    }

            })
            }
            })

        return view

    }

}