package com.example.mosisproject

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import com.example.mosisproject.data.Question
import com.example.mosisproject.model.QuestionsViewModel
import com.example.mosisproject.model.UsersViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddQuestionFragment : Fragment() ,AdapterView.OnItemSelectedListener{

    private lateinit var liveDatabase:DatabaseReference
    private lateinit var currLocation: Location
    private lateinit var  fusedLocationProvider: FusedLocationProviderClient
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var imgView:ImageView
    private var imgUri:Uri?=null
    private var imgName:String?=""
    private  lateinit var storageRef: StorageReference
    lateinit var questionDifficulty:String
    lateinit var questionCategory:String
    lateinit var question:String
    lateinit var answ1:String
    lateinit var answ2:String
    lateinit var answ3:String
    lateinit var answ4:String
    lateinit var correctAnsw:String
    private var lon:Double=0.0
    private var lat:Double=0.0
    private lateinit var view:View
    private val questionViewModel: QuestionsViewModel by activityViewModels()
    private val usersViewModel: UsersViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val bottomNav=activity!!.findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.visibility=View.VISIBLE

        lat=0.0
        lon=0.0
        LocationWizard.instance?.findMostAccurateLocation()?.let{
            lat=it.latitude
            lon=it.longitude
        }

        view=inflater.inflate(R.layout.fragment_add_question, container, false)

        if(savedInstanceState!=null){
             val id=savedInstanceState.getInt("rbtnId")
            val rbtn=view.findViewById<RadioButton>(id)
            if(rbtn!=null)
                rbtn.isChecked=true
            if(savedInstanceState.getString("imgUri")!=null && savedInstanceState.getString("imgName")!=null)
            {
                imgUri=Uri.parse(savedInstanceState.getString("imgUri"))
                view.findViewById<ImageView>(R.id.questionImage).setImageURI(imgUri)
                imgName=savedInstanceState.getString("imgName")!!
            }

        }

        storageRef= FirebaseStorage.getInstance().reference
        firebaseAuth=FirebaseAuth.getInstance()
        fusedLocationProvider= LocationServices.getFusedLocationProviderClient(this.context!!)


        liveDatabase=FirebaseDatabase.getInstance().getReference("Questions")

        imgView=view.findViewById<ImageView>(R.id.questionImage)
        view.findViewById<ImageView>(R.id.questionImage)
            .setOnClickListener {
                showPictureDialog()
            }

        val categorySpinner:Spinner=view.findViewById(R.id.questionCategory)
        ArrayAdapter.createFromResource(context!!,
        R.array.questionCategory,android.R.layout.simple_spinner_item).also { adapter->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter=adapter
        }
        categorySpinner.onItemSelectedListener=this

        val difficultySpinner:Spinner=view.findViewById(R.id.questionDifficulty)
        ArrayAdapter.createFromResource(context!!,
            R.array.questionDifficulty,android.R.layout.simple_spinner_item).also { adapter->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            difficultySpinner.adapter=adapter
        }

        difficultySpinner.onItemSelectedListener=this
        view.findViewById<Button>(R.id.uploadQuestion).setOnClickListener {

            val user=usersViewModel._users.value!!.find { it.id==firebaseAuth.currentUser!!.uid }
            user!!.points+=10
            usersViewModel.updateUser(user!!)

            question=view.findViewById<EditText>(R.id.editTextQuestion).text.toString()
            answ1=view.findViewById<EditText>(R.id.Answ1).text.toString()
            answ2=view.findViewById<EditText>(R.id.Answ2).text.toString()
            answ3=view.findViewById<EditText>(R.id.Answ3).text.toString()
            answ4=view.findViewById<EditText>(R.id.Answ4).text.toString()
            val rdGrp=view.findViewById<RadioGroup>(R.id.radioGroup)

            val selectedId=rdGrp.checkedRadioButtonId

            val radioBtn=view.findViewById<RadioButton>(selectedId)
            val res=resources
            if(radioBtn!=null)
            {
                val id=res.getIdentifier("Answ"+radioBtn.text.toString(),"id",context!!.packageName)
                correctAnsw = view.findViewById<EditText>(id).text.toString()
            }
            else
                correctAnsw=""

            if(question!="" && answ1!=""&&answ2!=""&&answ3!=""&&answ4!=""&&correctAnsw!=""&&questionDifficulty!=""&&questionCategory!="")
            {
                if(view.findViewById<ImageView>(R.id.questionImage).drawable==null) {
                    val key = liveDatabase.push().key
                    val date = Calendar.getInstance().time
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                    val q: Question = Question(
                        key!!,
                        firebaseAuth.currentUser?.uid!!,
                        question,
                        correctAnsw,
                        answ1,
                        answ2,
                        answ3,
                        answ4,
                        questionCategory.toLowerCase(),
                        questionDifficulty,
                        imgName,
                        lon.toString(),
                        lat.toString(),
                        sdf.format(date)
                    )

                    questionViewModel.addQuestion(q)

                    parentFragmentManager.popBackStack()
                }
                else{
                    view.findViewById<ProgressBar>(R.id.addImageLoader).visibility=View.VISIBLE
                    uploadImageToFirebase(imgUri!!)
                }

            }
            else{
                Toast.makeText(context, "Morate popuniti sva polja", Toast.LENGTH_SHORT).show()
            }

        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    private fun showPictureDialog(){
        val picDialog= AlertDialog.Builder(this.context!!)
        picDialog.setTitle("Select Action")
        val picDialogItems= arrayOf("Select photo from gallery","Capture photo from camera")

        val act=activity as AppCompatActivity
        act.onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {

            }
        })

        picDialog.setItems(picDialogItems){
                dialog,which->
            when (which){
                0->chooseFromGallery()
                1->takePicWithCamera()
            }
        }

        picDialog.show()
    }

    private fun chooseFromGallery(){
        val i= Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(i,1)
    }

    private fun takePicWithCamera(){
        dispatchTakePictureIntent()
    }

    lateinit var currphotoPath:String

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currphotoPath = absolutePath
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(this.context!!.packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {

                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this.context!!,
                        "com.example.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, 0)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==0){
            if(resultCode== Activity.RESULT_OK){
                var f: File = File(currphotoPath)
                imgView.setImageURI(Uri.fromFile(f))
                imgView.background=null
                Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
                    val f = File(currphotoPath)
                    mediaScanIntent.data = Uri.fromFile(f)
                    this.context!!.sendBroadcast(mediaScanIntent)
                }
                imgUri= Uri.fromFile(f)
            }
        }

        if(requestCode==1){
            if(resultCode== Activity.RESULT_OK){
                imgUri=data?.data!!
                var path:String=data?.data!!.toString()
                val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                val imgFileName="JPEG_"+timeStamp+"."+path.substring(path.lastIndexOf(".")+1)
                imgView.setImageURI(imgUri)
                imgView.background=null

            }
        }
    }
    private fun uploadImageToFirebase(fileUri: Uri) {

        imgName=firebaseAuth.currentUser!!.uid+"_"+SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val image: StorageReference =storageRef.child("Question images/"+imgName)
        image.putFile(fileUri).addOnCompleteListener() {
            if(it.isSuccessful){
                FirebaseStorage.getInstance().reference.child("Question images/$imgName").downloadUrl.addOnCompleteListener {
                    val url=it.result.toString()
                        val key=liveDatabase.push().key
                        val date=Calendar.getInstance().time
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                        val q:Question=Question(key!!,firebaseAuth.currentUser?.uid!!,question,correctAnsw,answ1,answ2,answ3,answ4,
                            questionCategory.lowercase(),questionDifficulty,url,lon.toString(),lat.toString(),sdf.format(date))

                        questionViewModel.addQuestion(q)
                        view.findViewById<ProgressBar>(R.id.addImageLoader).visibility=View.GONE
                        parentFragmentManager.popBackStack()

                }
            }
            else{
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val spinner=parent as Spinner

        if(spinner.id==R.id.questionCategory)
        {
            questionCategory=spinner.selectedItem.toString()
        }
       if(spinner.id==R.id.questionDifficulty) {
           questionDifficulty = spinner.selectedItem.toString()
       }

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

    override fun onSaveInstanceState(outState: Bundle) {

        val rdGrp=view.findViewById<RadioGroup>(R.id.radioGroup)
        val selectedId=rdGrp.checkedRadioButtonId
        outState.putString("imgUri", imgUri.toString())
        outState.putString("imgName", imgName)
        outState.putInt("rbtnId",selectedId)

        super.onSaveInstanceState(outState)
    }
}