package com.example.mosisproject

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.mosisproject.data.Question
import com.example.mosisproject.model.QuestionsViewModel
import com.example.mosisproject.model.UsersViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class QuestionListFragment : Fragment() {

    private val questionViewModel: QuestionsViewModel by activityViewModels()
    private val usersViewModel:UsersViewModel by activityViewModels()
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var questions:ArrayList<HashMap<String,Any>>
    private lateinit var allQuestions:ArrayList<Question>
    private lateinit var filteredQuestions:ArrayList<HashMap<String,Any>>
    private var difficultyFilter:ArrayList<String> = ArrayList<String>()
    private var categoryFilter:ArrayList<String> =ArrayList<String>()
    private lateinit var view:View
    private var startDateString:String=""
    private var endDateString:String=""
    private var startDate:Date=Date()
    private var endDate:Date=Date()
    private lateinit var startDateTextView:TextView
    private lateinit var endDateTextView:TextView
    private lateinit var myAdapter:MyQuestionListAdapter
    private lateinit var radiusEditText: EditText
    private var radius:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        firebaseAuth=FirebaseAuth.getInstance()

        val bottomNav=activity!!.findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.visibility=View.VISIBLE
       // bottomNav.selectedItemId=R.id.bottom_nav_question_list
        val activity=activity as MainActivity
        val id=resources.getIdentifier("questionListFragment", "id", context!!.packageName)
        activity.setSelectedNavBarId(id)

        val act=activity as AppCompatActivity
        act.onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {

            }
        })

        view=inflater.inflate(R.layout.fragment_question_list, container, false)

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
        startDateTextView=view.findViewById(R.id.startDateTextView)
        endDateTextView=view.findViewById(R.id.endDateTextView)
        radiusEditText=view.findViewById<EditText>(R.id.locationRadius)
        if(radius!="")
            radiusEditText.setText(radius)
        if(startDateString!="" && endDateString!="")
        {
            startDateTextView.setText(startDateString)
            endDateTextView.setText(endDateString)
        }

        if (questionViewModel.questions.value!=null)
            allQuestions=questionViewModel._questions.value!!
        else
            allQuestions= ArrayList<Question>()


        view.findViewById<TextView>(R.id.filterTextView).setOnClickListener {
            if(view.findViewById<LinearLayout>(R.id.filterBox).visibility==View.VISIBLE)
            {
                view.findViewById<LinearLayout>(R.id.filterBox).visibility=View.GONE
                view.findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView).visibility=View.VISIBLE
                view.findViewById<ScrollView>(R.id.scrollView).visibility=View.VISIBLE
                view.findViewById<LinearLayout>(R.id.linearLayout).visibility=View.VISIBLE
            }
            else
            {
                view.findViewById<LinearLayout>(R.id.filterBox).visibility=View.VISIBLE
                view.findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView).visibility=View.GONE
                view.findViewById<ScrollView>(R.id.scrollView).visibility=View.GONE
                view.findViewById<LinearLayout>(R.id.linearLayout).visibility=View.GONE
            }
        }

        view.findViewById<Button>(R.id.btnClearFilters).setOnClickListener {
            difficultyFilter.clear()
            categoryFilter.clear()
            startDateString=""
            endDateString=""
            view.findViewById<CheckBox>(R.id.easy).isChecked=false
            view.findViewById<CheckBox>(R.id.medium).isChecked=false
            view.findViewById<CheckBox>(R.id.hard).isChecked=false
            view.findViewById<CheckBox>(R.id.sport).isChecked=false
            view.findViewById<CheckBox>(R.id.art).isChecked=false
            view.findViewById<CheckBox>(R.id.history).isChecked=false
            view.findViewById<CheckBox>(R.id.science).isChecked=false
            view.findViewById<CheckBox>(R.id.music).isChecked=false
            view.findViewById<CheckBox>(R.id.geography).isChecked=false
            startDateTextView.text=""
            endDateTextView.text=""
            radius=""
            radiusEditText.setText("")

            claerFilter()
            view.findViewById<LinearLayout>(R.id.filterBox).visibility=View.GONE
            view.findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView).visibility=View.VISIBLE
            view.findViewById<ScrollView>(R.id.scrollView).visibility=View.VISIBLE
            view.findViewById<LinearLayout>(R.id.linearLayout).visibility=View.VISIBLE
        }

        view.findViewById<Button>(R.id.btnPickDate).setOnClickListener {
            openDatePickerDialog()
        }
        view.findViewById<Button>(R.id.btnApplyFilter).setOnClickListener {

            difficultyFilter.clear()
            categoryFilter.clear()

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
            radius=radiusEditText.text.toString()

           checkFilters()


            view.findViewById<LinearLayout>(R.id.filterBox).visibility=View.GONE
            view.findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView).visibility=View.VISIBLE
            view.findViewById<ScrollView>(R.id.scrollView).visibility=View.VISIBLE
            view.findViewById<LinearLayout>(R.id.linearLayout).visibility=View.VISIBLE
        }


        usersViewModel._users.observe(viewLifecycleOwner, Observer {
            if(questionViewModel._questions.value!=null) {
                allQuestions=questionViewModel.questions.value!!
                checkFilters()
            }
        })


        questionViewModel._questions.observe(viewLifecycleOwner, Observer {


            if(questionViewModel._questions.value!=null) {
                allQuestions=questionViewModel.questions.value!!
                usersViewModel.getAllUsers()
                checkFilters()
                }
        })
        return view
    }

    fun checkFilters(){
        if(categoryFilter.size!=0 &&difficultyFilter.size!=0&& startDateString!=""&&endDateString!=""&&radius!="")
            filterAll()

        if(categoryFilter.size!=0 &&difficultyFilter.size==0&& startDateString!=""&&endDateString!=""&&radius!="")
            filterCategoryDateRadius()

        if(categoryFilter.size==0 &&difficultyFilter.size!=0&& startDateString!=""&&endDateString!=""&&radius!="")
            filterDificultyDateRadius()


        if(categoryFilter.size==0 &&difficultyFilter.size==0&& startDateString!=""&&endDateString!=""&&radius!="")
            filterDateRadius()

        if(categoryFilter.size==0 &&difficultyFilter.size==0&& startDateString!=""&&endDateString!=""&&radius=="")
            filterDate()

        if(categoryFilter.size==0 &&difficultyFilter.size==0&& startDateString==""&&endDateString==""&&radius!="")
            filterRadius()

        if(categoryFilter.size!=0 &&difficultyFilter.size!=0&& startDateString!=""&&endDateString!=""&&radius=="")
            filterCategoryDificultyDate()

        if(categoryFilter.size!=0 &&difficultyFilter.size==0&& startDateString!=""&&endDateString!=""&&radius=="")
            filterCategoryDate()

        if(categoryFilter.size==0 &&difficultyFilter.size!=0&& startDateString!=""&&endDateString!=""&&radius=="")
            filterDificultyDate()

        if(categoryFilter.size!=0 &&difficultyFilter.size!=0&& startDateString==""&&endDateString==""&&radius!="")
            filterCategoryDificultyRadius()

        if(categoryFilter.size!=0 &&difficultyFilter.size==0&& startDateString==""&&endDateString==""&&radius!="")
            filterCategoryRadius()

        if(categoryFilter.size==0 &&difficultyFilter.size!=0&& startDateString==""&&endDateString==""&&radius!="")
            filterDifficultyRadius()

        if(categoryFilter.size!=0 && difficultyFilter.size!=0&& startDateString==""&&endDateString==""&&radius=="" )
            filterCategoryDificulty()
        if(categoryFilter.size!=0 && difficultyFilter.size==0 && startDateString==""&&endDateString==""&&radius=="")
            filterCategory()
        if(categoryFilter.size==0 && difficultyFilter.size!=0 && startDateString==""&&endDateString==""&&radius=="")
            filterDificulty()
        if(categoryFilter.size==0 && difficultyFilter.size==0 && startDateString==""&&endDateString==""&&radius=="") {
            claerFilter()
        }
    }

    fun openDatePickerDialog(){
        val builder= MaterialDatePicker.Builder.dateRangePicker().build()
        builder.addOnPositiveButtonClickListener {selection->
            val startdate=selection.first
            val enddate=selection.second

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            startDateString = sdf.format(Date(startdate))
            endDateString = sdf.format(Date(enddate))
            startDate= Date(startdate)
            endDate= Date(enddate)

            startDateTextView.text=startDateString
            endDateTextView.text=endDateString
        }

        builder.show(fragmentManager!!,"Date picker")
    }

    fun filterDificulty() {
        filteredQuestions = ArrayList<HashMap<String, Any>>()
        allQuestions.forEach {
            if(difficultyFilter.contains(it.difficulty.lowercase()))
            {
                addQuestion(it)
            }
        }
        createAndSetAdapter()

        createANdSetSearchView()
    }

    fun filterCategory() {
        filteredQuestions = ArrayList<HashMap<String, Any>>()
        allQuestions.forEach {
            if(categoryFilter.contains(it.category.lowercase()))
            {
                addQuestion(it)
            }
        }
        createAndSetAdapter()

        createANdSetSearchView()
    }

    fun filterCategoryDificulty() {
        filteredQuestions = ArrayList<HashMap<String, Any>>()
        allQuestions.forEach {
            if(categoryFilter.contains(it.category.lowercase())&&difficultyFilter.contains(it.difficulty.lowercase()))
            {
                addQuestion(it)
            }
        }
        createAndSetAdapter()

        createANdSetSearchView()


    }

    fun claerFilter() {
        filteredQuestions = ArrayList<HashMap<String, Any>>()
        allQuestions.forEach {

           addQuestion(it)
        }

        createAndSetAdapter()

        createANdSetSearchView()

    }
    fun filterCategoryDateRadius(){
        filteredQuestions = ArrayList<HashMap<String, Any>>()
        var lat:Double=0.0
        var lon:Double=0.0
        radius=radiusEditText.text.toString()
        LocationWizard.instance?.findMostAccurateLocation()?.let {
            lat = it.latitude
            lon = it.longitude
        }
        val result:FloatArray= FloatArray(1)
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        allQuestions.forEach {
            Location.distanceBetween(lat,lon,it.latitude.toDouble(),it.longitude.toDouble(),result)
            if(sdf.parse(it.date).after(startDate) && sdf.parse(it.date).before(endDate)&&
                categoryFilter.contains(it.category.lowercase())&&result[0]<=radius.toFloat())
            {
                addQuestion(it)

            }
        }
        createAndSetAdapter()

        createANdSetSearchView()
    }
    fun filterAll(){
        filteredQuestions = ArrayList<HashMap<String, Any>>()
        var lat:Double=0.0
        var lon:Double=0.0
        radius=radiusEditText.text.toString()
        LocationWizard.instance?.findMostAccurateLocation()?.let {
            lat = it.latitude
            lon = it.longitude
        }
        val result:FloatArray= FloatArray(1)
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        allQuestions.forEach {
            Location.distanceBetween(lat,lon,it.latitude.toDouble(),it.longitude.toDouble(),result)
                if(sdf.parse(it.date).after(startDate) && sdf.parse(it.date).before(endDate)&&
                    categoryFilter.contains(it.category.toLowerCase())&&difficultyFilter.contains(it.difficulty.toLowerCase())&&result[0]<=radius.toFloat())
                {
                    addQuestion(it)

                }
        }
        createAndSetAdapter()

        createANdSetSearchView()
    }
    fun filterCategoryDificultyDate(){
        filteredQuestions = ArrayList<HashMap<String, Any>>()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        allQuestions.forEach {
            if(sdf.parse(it.date).after(startDate) && sdf.parse(it.date).before(endDate)&&
                categoryFilter.contains(it.category.toLowerCase())&&difficultyFilter.contains(it.difficulty.toLowerCase()))
            {
                addQuestion(it)

            }
        }
        createAndSetAdapter()

        createANdSetSearchView()
    }
    fun filterCategoryDate(){
        filteredQuestions = ArrayList<HashMap<String, Any>>()

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        allQuestions.forEach {
            if(sdf.parse(it.date).after(startDate) && sdf.parse(it.date).before(endDate)&& categoryFilter.contains(it.category.toLowerCase()))
            {
                addQuestion(it)

            }
        }
        createAndSetAdapter()

        createANdSetSearchView()
    }
    fun filterDificultyDate(){
        filteredQuestions = ArrayList<HashMap<String, Any>>()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        allQuestions.forEach {
            if(sdf.parse(it.date).after(startDate) && sdf.parse(it.date).before(endDate)&& difficultyFilter.contains(it.difficulty.toLowerCase()))
            {
                addQuestion(it)

            }
        }
        createAndSetAdapter()

        createANdSetSearchView()
    }
    fun filterCategoryDificultyRadius(){
        filteredQuestions = ArrayList<HashMap<String, Any>>()
        var lat:Double=0.0
        var lon:Double=0.0
        radius=radiusEditText.text.toString()
        LocationWizard.instance?.findMostAccurateLocation()?.let {
            lat = it.latitude
            lon = it.longitude
        }
        val result:FloatArray= FloatArray(1)

        allQuestions.forEach {
            Location.distanceBetween(lat,lon,it.latitude.toDouble(),it.longitude.toDouble(),result)
            if(categoryFilter.contains(it.category.toLowerCase())&&difficultyFilter.contains(it.difficulty.toLowerCase())&&result[0]<=radius.toFloat())
            {
                addQuestion(it)

            }
        }
        createAndSetAdapter()

        createANdSetSearchView()
    }
    fun filterCategoryRadius(){
        filteredQuestions = ArrayList<HashMap<String, Any>>()
        var lat:Double=0.0
        var lon:Double=0.0
        radius=radiusEditText.text.toString()
        LocationWizard.instance?.findMostAccurateLocation()?.let {
            lat = it.latitude
            lon = it.longitude
        }
        val result:FloatArray= FloatArray(1)
        allQuestions.forEach {
            Location.distanceBetween(lat,lon,it.latitude.toDouble(),it.longitude.toDouble(),result)
            if(categoryFilter.contains(it.category.toLowerCase())&&result[0]<=radius.toFloat())
            {
                addQuestion(it)

            }
        }
        createAndSetAdapter()

        createANdSetSearchView()
    }
    fun filterDifficultyRadius(){
        filteredQuestions = ArrayList<HashMap<String, Any>>()
        var lat:Double=0.0
        var lon:Double=0.0
        radius=radiusEditText.text.toString()
        LocationWizard.instance?.findMostAccurateLocation()?.let {
            lat = it.latitude
            lon = it.longitude
        }
        val result:FloatArray= FloatArray(1)
        allQuestions.forEach {
            Location.distanceBetween(lat,lon,it.latitude.toDouble(),it.longitude.toDouble(),result)
            if(difficultyFilter.contains(it.difficulty.toLowerCase())&&result[0]<=radius.toFloat())
            {
                addQuestion(it)

            }
        }
        createAndSetAdapter()

        createANdSetSearchView()
    }

    fun filterDificultyDateRadius(){
        filteredQuestions = ArrayList<HashMap<String, Any>>()
        var lat:Double=0.0
        var lon:Double=0.0
        radius=radiusEditText.text.toString()
        LocationWizard.instance?.findMostAccurateLocation()?.let {
            lat = it.latitude
            lon = it.longitude
        }
        val result:FloatArray= FloatArray(1)
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        allQuestions.forEach {
            Location.distanceBetween(lat,lon,it.latitude.toDouble(),it.longitude.toDouble(),result)
            if(sdf.parse(it.date).after(startDate) && sdf.parse(it.date).before(endDate)&&
                difficultyFilter.contains(it.difficulty.toLowerCase())&&result[0]<=radius.toFloat())
            {
                addQuestion(it)

            }
        }
        createAndSetAdapter()

        createANdSetSearchView()
    }

    fun filterRadius(){
        filteredQuestions = ArrayList<HashMap<String, Any>>()
        var lat:Double=0.0
        var lon:Double=0.0
        radius=radiusEditText.text.toString()
        LocationWizard.instance?.findMostAccurateLocation()?.let {
            lat = it.latitude
            lon = it.longitude
        }
        val result:FloatArray= FloatArray(1)

        allQuestions.forEach {
            Location.distanceBetween(lat,lon,it.latitude.toDouble(),it.longitude.toDouble(),result)
            if(result[0]<=radius.toFloat())
            {
                Toast.makeText(context, lat.toString()+" "+lon.toString(), Toast.LENGTH_SHORT).show()
                addQuestion(it)

            }
        }
        createAndSetAdapter()

        createANdSetSearchView()
    }

    fun filterDate(){
        filteredQuestions = ArrayList<HashMap<String, Any>>()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        allQuestions.forEach {
            if(sdf.parse(it.date).after(startDate) && sdf.parse(it.date).before(endDate))
            {
                addQuestion(it)

            }
        }
        createAndSetAdapter()

        createANdSetSearchView()
    }

    fun filterDateRadius(){
        filteredQuestions = ArrayList<HashMap<String, Any>>()
        var lat:Double=0.0
        var lon:Double=0.0
        radius=radiusEditText.text.toString()
        LocationWizard.instance?.findMostAccurateLocation()?.let {
            lat = it.latitude
            lon = it.longitude
        }
        val result:FloatArray= FloatArray(1)
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        allQuestions.forEach {
            Location.distanceBetween(lat,lon,it.latitude.toDouble(),it.longitude.toDouble(),result)
            if(sdf.parse(it.date).after(startDate) && sdf.parse(it.date).before(endDate)&&result[0]<=radius.toFloat())
            {
                addQuestion(it)
            }
        }
        createAndSetAdapter()

        createANdSetSearchView()
    }

    fun addQuestion(question:Question)
    {


        val q: HashMap<String, Any> = HashMap<String, Any>()
        q.put("question", question)
        val icon =
            (resources.getIdentifier(question.category, "drawable", context!!.packageName))
        q.put("icon", icon)

        val author =
            usersViewModel._users.value!!.find { it.id == question.user }
        q.put("username", author!!.email)

        val user =
            usersViewModel._users.value!!.find { it.id ==firebaseAuth.currentUser!!.uid }

        if (user!!.answeredQuestions == null)
            user.answeredQuestions = ArrayList<String>()
            if (user!!.answeredQuestions.contains(question.id)) {
                q.put("answered", "Question answered")
            } else {
                if(author.id==user.id)
                    q.put("answered","You addded this question")
                else
                    q.put("answered", "Click to answer")
            }


        filteredQuestions.add(q)
    }

    fun createAndSetAdapter(){

        val questionList=requireView().findViewById<ListView>(R.id.question_list)
        myAdapter=MyQuestionListAdapter(activity!!, filteredQuestions)
        questionList.adapter=myAdapter


        questionList.setOnItemClickListener { adapterView, view, i, l ->
            val el=myAdapter.getItem(i) as HashMap<String,Any>
            val q=el.get("question") as Question

            val result:FloatArray= FloatArray(1)
            var lat:Double=0.0
            var lon:Double=0.0
            LocationWizard.instance?.findMostAccurateLocation()?.let {
                lat = it.latitude
                lon = it.longitude
            }
            Location.distanceBetween(q.latitude.toDouble(),q.longitude.toDouble(),lat,lon,result)


            if(el.get("answered")=="Click to answer")
            {
                if(result[0]<=10) {
                    val question = el.get("question") as Question
                    val bundle = Bundle()
                    bundle.putString("id", question.id)
                    bundle.putString("question", question.question)
                    bundle.putString("answer1", question.answ1)
                    bundle.putString("answer2", question.answ2)
                    bundle.putString("answer3", question.answ3)
                    bundle.putString("answer4", question.answ4)
                    bundle.putString("correctAnswer", question.correctAnsw)
                    bundle.putString("image", question.image)
                    findNavController().navigate(R.id.questionFragment, bundle)
                }
                else
                {
                    val dialog= AlertDialog.Builder(context)
                    dialog.setTitle("Question information")
                    dialog.setMessage("To answer this question you must get closer to it's location!!!")
                    dialog.setPositiveButton("OK",DialogInterface.OnClickListener{dialog,id->
                        dialog.cancel()
                    })
                    dialog.create().show()
                }

            }
        }
    }

    fun createANdSetSearchView() {
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

   override fun onSaveInstanceState(outState: Bundle) {

        outState.putStringArrayList("categoryFilter",categoryFilter)
        outState.putStringArrayList("difficultyFilter",difficultyFilter)
        outState.putString("startDate",startDateString)
        outState.putString("endDate",endDateString)
        outState.putString("raduis",radius)
        super.onSaveInstanceState(outState)
    }
}