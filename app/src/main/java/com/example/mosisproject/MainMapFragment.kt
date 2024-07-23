package com.example.mosisproject

import android.app.AlertDialog
import android.app.Dialog
import android.app.Fragment
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController

import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.example.mosisproject.data.Question
import com.example.mosisproject.model.QuestionsViewModel
import com.example.mosisproject.model.UsersViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
//import com.google.android.gms.maps.MapView
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainMapFragment : Fragment(){
    lateinit var map:MapView
    lateinit var locationManager: LocationManager
    lateinit var myLocationOverlay:MyLocationNewOverlay
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var locationProvider:GpsMyLocationProvider
    private val usersViewModel:UsersViewModel by activityViewModels()
    private val questionViewModel:QuestionsViewModel by activityViewModels()
    private var difficultyFilter:ArrayList<String> = ArrayList<String>()
    private var categoryFilter:ArrayList<String> = ArrayList<String>()
    private lateinit var allQuestions:ArrayList<Question>
    private lateinit var filteredQuestions:ArrayList<Question>
    private var startDateString:String=""
    private var endDateString:String=""
    private lateinit var startDateTextView:TextView
    private lateinit var endDateTextView:TextView
    private lateinit var startDate:Date
    private lateinit var endDate:Date
    private lateinit var radiusEditText:EditText
    private var radius:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.nav_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.nav_homeMap -> {findNavController().navigate(R.id.mainMapFragment)
                true
            }
            R.id.nav_addQuestion -> { findNavController().navigate(R.id.addQuestionFragment)
               true}
            R.id.nav_logout ->{firebaseAuth.signOut()
                val i: Intent = Intent(context,LoginActivity::class.java)
                startActivity(i)
                //finish()
                true
            }
            R.id.nav_listQuestion->{findNavController().navigate(R.id.questionListFragment)
            true}
            R.id.nav_rankList->{findNavController().navigate(R.id.usersListFragment)
            true}
            R.id.nav_profile->{findNavController().navigate(R.id.profileFragment)
                true }
            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d("LIFECYCLE","onActivityCreated")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("LIFECYCLE","onCreateView")

        val view=inflater.inflate(R.layout.fragment_main_map, container, false)
        view.findViewById<FloatingActionButton>(R.id.fabFilter).setOnClickListener {
            openDialog()
        }

        return view

    }

    fun drawPin(question:Question){
        val pin = Marker(map)
        val result:FloatArray= FloatArray(1)
        var lat:Double=0.0
        var lon:Double=0.0
        LocationWizard.instance?.findMostAccurateLocation()?.let {
            lat = it.latitude
            lon = it.longitude
        }
        Location.distanceBetween(question.latitude.toDouble(),question.longitude.toDouble(),lat,lon,result)

       /* usersViewModel._users.value?.let { itUser->
           val user =  itUser.find { it.id==firebaseAuth.currentUser!!.uid}
        }*/


        val user=usersViewModel._users.value?.find { it.id==firebaseAuth.currentUser!!.uid }
        //if (user!!.answeredQuestions==null)
            //user.answeredQuestions=ArrayList<String>()

        pin.setDefaultIcon()
        pin.setOnMarkerClickListener { marker, mapView ->
            val dialog=AlertDialog.Builder(context)
            dialog.setTitle("Question confirmation")
            var textMessage:String

            if(user?.answeredQuestions!=null && user!!.answeredQuestions.contains(question.id))
            {
                textMessage="You already answered this question!"
                dialog.setNegativeButton("OK",DialogInterface.OnClickListener { dialog, i ->
                    dialog.cancel()
                })
            }
            else {
                if (result[0] <= 10) {
                    val user =
                        usersViewModel._users.value!!.find { it.id == firebaseAuth.currentUser!!.uid }
                    if (question.user == user!!.id) {
                        textMessage = "You can't answer this question because you added it!"
                        dialog.setNegativeButton(
                            "OK",
                            DialogInterface.OnClickListener { dialog, i ->
                                dialog.cancel()
                            })
                    } else {
                        textMessage =
                            "Are you sure you want to take this question?\nQuestion catagory: " + question.category.uppercase() + "\nQuestion difficulty: " + question.difficulty.uppercase()
                        dialog.setPositiveButton(
                            "YES",
                            DialogInterface.OnClickListener { dialog, id ->
                                val bundle: Bundle = Bundle()
                                bundle.putString("id", question.id)
                                bundle.putString("question", question.question)
                                bundle.putString("answer1", question.answ1)
                                bundle.putString("answer2", question.answ2)
                                bundle.putString("answer3", question.answ3)
                                bundle.putString("answer4", question.answ4)
                                bundle.putString("correctAnswer", question.correctAnsw)
                                bundle.putString("image", question.image)
                                findNavController().navigate(R.id.questionFragment, bundle)
                                dialog.cancel()

                            })
                        dialog.setNegativeButton(
                            "NO",
                            DialogInterface.OnClickListener { dialog, id ->
                                dialog.cancel()
                            })
                    }


                } else {
                    textMessage =
                        "You can't answer this question, it is not in radius of 10 meters from your location!!\nQuestion catagory: " + question.category.uppercase() + "\nQuestion difficulty: " + question.difficulty.uppercase()

                    dialog.setNegativeButton("OK", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                    })
                }
            }
            dialog.setMessage(textMessage)

            var alert=dialog.create()
            alert.show()
            true
        }

        val loc = GeoPoint(question.latitude.toDouble(), question.longitude.toDouble())
        pin.position = loc
        pin.id = "question"

        pin.icon=resources.getDrawable(R.drawable.pin)
        pin.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)


        map.overlays.add(pin)
    }

fun drawAllPins(questions:ArrayList<Question>){
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    var lat:Double=0.0
    var lon:Double=0.0
    LocationWizard.instance?.findMostAccurateLocation()?.let {
        lat = it.latitude
        lon = it.longitude
    }

    val result:FloatArray=FloatArray(1)
    questions.forEach {

        if(startDateString!="" && endDateString!="")
        {

            if(sdf.parse(it.date).after(startDate) && sdf.parse(it.date).before(endDate))
            {
                if(radius=="")
                {
                    drawPin(it)
                }
                else{
                    Location.distanceBetween(lat,lon,it.latitude.toDouble(),it.longitude.toDouble(),result)
                    if(result[0]<=radius.toFloat())
                    {
                        //Toast.makeText(context, result[0].toString(), Toast.LENGTH_SHORT).show()
                        drawPin(it)
                    }
                }
            }
            else{
            }

        }
        else{
            if(radius=="")
            {
                drawPin(it)
            }
            else{

                Location.distanceBetween(lat,lon,it.latitude.toDouble(),it.longitude.toDouble(),result)
                if(result[0]<=radius.toFloat())
                {
                    //Toast.makeText(context, result[0].toString(), Toast.LENGTH_SHORT).show()
                    drawPin(it)
                }
            }
        }
        }

    }

    fun openDialog(){
        val dialog=Dialog(context!!)
        val view=layoutInflater.inflate(R.layout.filter_layout,null,false)

        startDateTextView=view.findViewById(R.id.startDate)
        endDateTextView=view.findViewById(R.id.endDate)
        radiusEditText=view.findViewById<EditText>(R.id.locationRadius)
        if(radius!="")
            radiusEditText.setText(radius)
        if(difficultyFilter.contains("easy"))
            view.findViewById<CheckBox>(R.id.easy).isChecked=true

        if(difficultyFilter.contains("medium"))
            view.findViewById<CheckBox>(R.id.medium).isChecked=true

        if(difficultyFilter.contains("hard"))
            view.findViewById<CheckBox>(R.id.hard).isChecked=true

        if(categoryFilter.contains("sport"))
            view.findViewById<CheckBox>(R.id.sport).isChecked=true

        if(categoryFilter.contains("art"))
            view.findViewById<CheckBox>(R.id.art).isChecked=true

        if(categoryFilter.contains("geography"))
            view.findViewById<CheckBox>(R.id.geography).isChecked=true

        if(categoryFilter.contains("history"))
            view.findViewById<CheckBox>(R.id.history).isChecked=true

        if(categoryFilter.contains("music"))
            view.findViewById<CheckBox>(R.id.music).isChecked=true

        if(categoryFilter.contains("science"))
            view.findViewById<CheckBox>(R.id.science).isChecked=true

        if(startDateString!="" && endDateString!="")
        {
            startDateTextView.text=startDateString
            endDateTextView.text=endDateString
        }

        view.findViewById<Button>(R.id.btnPickDate).setOnClickListener {
            openDatePickerDialog()
        }


        view.findViewById<Button>(R.id.btnApplyFilter).setOnClickListener {
            difficultyFilter.clear()
            categoryFilter.clear()
            radius=radiusEditText.text.toString()

            if(view.findViewById<CheckBox>(R.id.easy).isChecked)
                difficultyFilter.add("easy")
            if(view.findViewById<CheckBox>(R.id.medium).isChecked)
                difficultyFilter.add("medium")
            if(view.findViewById<CheckBox>(R.id.hard).isChecked)
                difficultyFilter.add("hard")

            if(view.findViewById<CheckBox>(R.id.sport).isChecked)
                categoryFilter.add("sport")
            if(view.findViewById<CheckBox>(R.id.art).isChecked)
                categoryFilter.add("art")
            if(view.findViewById<CheckBox>(R.id.music).isChecked)
                categoryFilter.add("music")
            if(view.findViewById<CheckBox>(R.id.geography).isChecked)
                categoryFilter.add("geography")
            if(view.findViewById<CheckBox>(R.id.science).isChecked)
                categoryFilter.add("science")
            if(view.findViewById<CheckBox>(R.id.history).isChecked)
                categoryFilter.add("history")

            removeAllPins()

            radius=radiusEditText.text.toString()

            if(categoryFilter.size!=0 && difficultyFilter.size!=0 )
                drawFilteredCategoryDifficulty()
            if(categoryFilter.size!=0 && difficultyFilter.size==0 )
                drawFilteredCategory()
            if(categoryFilter.size==0 && difficultyFilter.size!=0 )
                drawFilteredDifficulty()
            if(categoryFilter.size==0 && difficultyFilter.size==0 )
                {
                    if(questionViewModel._questions.value!=null)
                        drawAllPins(questionViewModel._questions.value!!)
            }



            dialog.hide()
        }

        view.findViewById<Button>(R.id.btnClearFilters).setOnClickListener {
            difficultyFilter.clear()
            categoryFilter.clear()
            startDateString=""
            endDateString=""
            startDateTextView.text=""
            endDateTextView.text=""
            startDate=Date()
            endDate=Date()
            radius=""
            radiusEditText.setText("")

            view.findViewById<CheckBox>(R.id.easy).isChecked=false
            view.findViewById<CheckBox>(R.id.medium).isChecked=false
            view.findViewById<CheckBox>(R.id.hard).isChecked=false
            view.findViewById<CheckBox>(R.id.sport).isChecked=false
            view.findViewById<CheckBox>(R.id.art).isChecked=false
            view.findViewById<CheckBox>(R.id.history).isChecked=false
            view.findViewById<CheckBox>(R.id.science).isChecked=false
            view.findViewById<CheckBox>(R.id.music).isChecked=false
            view.findViewById<CheckBox>(R.id.geography).isChecked=false
            view.findViewById<TextView>(R.id.startDate).text=""
            view.findViewById<TextView>(R.id.endDate).text=""

            removeAllPins()
            if(questionViewModel._questions.value!=null)
                drawAllPins(questionViewModel._questions.value!!)
            dialog.hide()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    fun openDatePickerDialog(){
        val builder= MaterialDatePicker.Builder.dateRangePicker().build()
        builder.addOnPositiveButtonClickListener {selection->
            val startdate=selection.first
            val enddate=selection.second

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            startDateString = sdf.format(Date(startdate))
            endDateString = sdf.format(Date(enddate))
            startDate=Date(startdate)
            endDate=Date(enddate)

            startDateTextView.text=startDateString
            endDateTextView.text=endDateString
        }



        builder.show(fragmentManager!!,"Date picker")
    }

    fun drawFilteredDifficulty(){
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        var lat:Double=0.0
        var lon:Double=0.0
        LocationWizard.instance?.findMostAccurateLocation()?.let {
            lat = it.latitude
            lon = it.longitude
        }
        val result:FloatArray=FloatArray(1)
        allQuestions.forEach{
            if(difficultyFilter.contains(it.difficulty.lowercase())){
                if(startDateString!="" && endDateString!="")
                {

                    if(sdf.parse(it.date).after(startDate) && sdf.parse(it.date).before(endDate)) {
                        if(radius=="")
                        {
                            drawPin(it)
                        }
                        else{
                            Location.distanceBetween(lat,lon,it.latitude.toDouble(),it.longitude.toDouble(),result)
                            if(result[0]<=radius.toFloat())
                                drawPin(it)
                        }
                    }
                    else{

                    }

                }
                else{
                    if(radius=="")
                    {
                        drawPin(it)
                    }
                    else{
                        Location.distanceBetween(lat,lon,it.latitude.toDouble(),it.longitude.toDouble(),result)
                        if(result[0]<=radius.toFloat())
                            drawPin(it)
                    }
                }

            }
            else{
            }
        }
    }

    fun drawFilteredCategory(){
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        var lat:Double=0.0
        var lon:Double=0.0
        LocationWizard.instance?.findMostAccurateLocation()?.let {
            lat = it.latitude
            lon = it.longitude
        }

        val result:FloatArray= FloatArray(1)
        allQuestions.forEach{
            if(categoryFilter.contains(it.category.lowercase())){
                if(startDateString!="" && endDateString!="")
                {
                    if(sdf.parse(it.date).after(startDate) && sdf.parse(it.date).before(endDate)) {
                        if(radius=="")
                        {
                            drawPin(it)
                        }
                        else{
                            Location.distanceBetween(lat,lon,it.latitude.toDouble(),it.longitude.toDouble(),result)
                            if(result[0]<=radius.toFloat())
                                drawPin(it)
                        }
                    }
                    else{

                    }

                }
                else{
                    if(radius=="")
                    {
                        drawPin(it)
                    }
                    else{
                        Location.distanceBetween(lat,lon,it.latitude.toDouble(),it.longitude.toDouble(),result)
                        if(result[0]<=radius.toFloat())
                            drawPin(it)
                    }
                }

            }
        }
    }
    fun drawFilteredCategoryDifficulty(){
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        filteredQuestions=ArrayList<Question>()
        var lat:Double=0.0
        var lon:Double=0.0
        //radius=radiusEditText.text.toString()
        LocationWizard.instance?.findMostAccurateLocation()?.let {
            lat = it.latitude
            lon = it.longitude
        }
        val result:FloatArray= FloatArray(1)

        allQuestions.forEach{
            if(difficultyFilter.contains(it.difficulty.lowercase())&&categoryFilter.contains(it.category.lowercase())){
                if(startDateString!="" && endDateString!="")
                {
                    if(sdf.parse(it.date).after(startDate) && sdf.parse(it.date).before(endDate)) {
                        if(radius=="")
                        {
                            drawPin(it)
                        }
                        else{
                            Location.distanceBetween(lat,lon,it.latitude.toDouble(),it.longitude.toDouble(),result)
                            if(result[0]<=radius.toFloat())
                                drawPin(it)
                        }
                    }
                    else{

                    }

                }
                else{
                    if(radius=="")
                    {
                        drawPin(it)
                    }
                    else{
                        Location.distanceBetween(lat,lon,it.latitude.toDouble(),it.longitude.toDouble(),result)
                        if(result[0]<=radius.toFloat())
                            drawPin(it)
                    }
                }

            }
        }
    }

    fun removeAllPins(){

        map.overlays.forEach{

            if(it is Marker && it.id=="question")
                map.overlays.remove(it)
        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNav=activity!!.findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.visibility=View.VISIBLE

        val act=activity as AppCompatActivity
        act.getSupportActionBar()!!.setDisplayHomeAsUpEnabled(false)
        act.onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {

            }
        })

        val v= layoutInflater.inflate(R.layout.filter_layout,null,false)
        radiusEditText=v.findViewById<EditText>(R.id.locationRadius)

        if(savedInstanceState!=null)
        {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            categoryFilter= savedInstanceState.getStringArrayList("categoryFilter") as ArrayList<String>
            difficultyFilter=savedInstanceState.getStringArrayList("difficultyFilter") as ArrayList<String>
            startDateString=savedInstanceState.getString("startDate")!!
            endDateString=savedInstanceState.getString("endDate")!!
            radius=savedInstanceState.getString("raduis")!!

            if(startDateString!=""&&endDateString!="") {
                startDate = sdf.parse(startDateString)
                endDate = sdf.parse(endDateString)
            }

        }


        if(questionViewModel._questions.value==null)
        {
            allQuestions= ArrayList<Question>()
        }
        else
        {
            allQuestions=questionViewModel._questions.value!!
        }

        val activity=activity as MainActivity
        val id=resources.getIdentifier("mainMapFragment", "id", context!!.packageName)
        activity.setSelectedNavBarId(id)

        //inicijalizacija mape
        var ctx:Context?=activity?.applicationContext
       org.osmdroid.config.Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx!!))
        map=requireView().findViewById<MapView>(R.id.map)
        map.setMultiTouchControls(true)

        if(ActivityCompat.checkSelfPermission(requireActivity(),android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireActivity(),android.Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
        else{
            setMyLocationOverlay()
        }
        map.controller.setZoom(15.0)
        val startPoint=GeoPoint(43.3209,21.8958)
        map.controller.setCenter(startPoint)
        firebaseAuth=FirebaseAuth.getInstance()

        questionViewModel.getAllQuestions()
        usersViewModel.getAllUsers()

        questionViewModel._questions.observe(viewLifecycleOwner, Observer {
            val v= layoutInflater.inflate(R.layout.filter_layout,null,false)
            radiusEditText=v.findViewById<EditText>(R.id.locationRadius)

            allQuestions=it
            removeAllPins()
            if(categoryFilter.size!=0 && difficultyFilter.size!=0 )
                drawFilteredCategoryDifficulty()
            if(categoryFilter.size!=0 && difficultyFilter.size==0 )
                drawFilteredCategory()
            if(categoryFilter.size==0 && difficultyFilter.size!=0 )
                drawFilteredDifficulty()
            if(categoryFilter.size==0 && difficultyFilter.size==0 )
            {
                //removeAllPins()
                if(questionViewModel._questions.value!=null)
                    drawAllPins(questionViewModel._questions.value!!)
            }
        })
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {isGranted: Boolean->
            if(isGranted) {
                setMyLocationOverlay()
            }
        }

    private fun setMyLocationOverlay(){
        myLocationOverlay=MyLocationNewOverlay(GpsMyLocationProvider(activity),map)
        myLocationOverlay.enableMyLocation()
        //myLocationOverlay.enableFollowLocation()
        map.overlays.add(myLocationOverlay)
    }


    override fun onStart() {
        super.onStart()
        Log.d("LIFECYCLE","onStart")
    }

    override fun onResume() {
        super.onResume()

        map.onResume()
        //org.osmdroid.config.Configuration.getInstance().load(context,PreferenceManager.getDefaultSharedPreferences(context!!))
        //myLocationOverlay.enableMyLocation()
        Log.d("LIFECYCLE","onResume")
    }

    override fun onPause() {
        super.onPause()
       // map.onPause()
        //myLocationOverlay.disableMyLocation()
        Log.d("LIFECYCLE","onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("LIFECYCLE","onStop")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("LIFECYCLE","onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("LIFECYCLE","onDestroy")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("LIFECYCLE","onDetach")
    }

    override fun onSaveInstanceState(outState: Bundle) {

        outState.putStringArrayList("categoryFilter",categoryFilter)
        outState.putStringArrayList("difficultyFilter",difficultyFilter)
        outState.putString("startDate",startDateString)
        outState.putString("endDate",endDateString)
        outState.putString("raduis",radius)
        super.onSaveInstanceState(outState)
    }

}
