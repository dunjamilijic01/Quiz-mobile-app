package com.example.mosisproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.mosisproject.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        firebaseAuth=FirebaseAuth.getInstance()

        val navHostFragment=supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController

        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        binding.bottomNav.labelVisibilityMode= NavigationBarView.LABEL_VISIBILITY_LABELED
        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.mainMapFragment->{navController.navigate(R.id.mainMapFragment)
                true}
                R.id.addQuestionFragment->{navController.navigate(R.id.addQuestionFragment)
                true}
                R.id.questionListFragment->{navController.navigate(R.id.questionListFragment)
                true}
                R.id.usersListFragment->{navController.navigate(R.id.usersListFragment)
                true}
                else ->true
            }
        }


    }

    public fun setSelectedNavBarId(id:Int){
        binding.bottomNav.menu.findItem(id).setChecked(true)
    }

    /*override fun onBackPressed() {
        //onBackPressed()
        supportFragmentManager.popBackStack()
    }*/
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}