package com.example.mosisproject

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.mosisproject.data.User
import com.example.mosisproject.databinding.ActivitySignupBinding
import com.example.mosisproject.model.UsersViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SignupActivity : AppCompatActivity() {

    private lateinit var binding:ActivitySignupBinding
    private lateinit var firebaseAuth:FirebaseAuth
    private lateinit var firestore:FirebaseFirestore
    private  lateinit var storageRef: StorageReference
    private var imgName:String=""
    private var imgUri:Uri?=null
    private val usersViewModel:UsersViewModel by viewModels()
    lateinit var email:String
    lateinit var pass:String
    lateinit var confirmpass:String
    lateinit var firstname:String
    lateinit var lastname:String
    lateinit var phone:String
    lateinit var img:ImageView
    lateinit var userid:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(savedInstanceState!=null){
            if(savedInstanceState.getString("imgUri")!=null && savedInstanceState.getString("imgName")!=null)
            {
                imgUri=Uri.parse(savedInstanceState.getString("imgUri"))
                binding.addPicture.setImageURI(imgUri)
                imgName=savedInstanceState.getString("imgName")!!
            }
            else{
            }
        }
        firebaseAuth=FirebaseAuth.getInstance()
        firestore=FirebaseFirestore.getInstance()
        storageRef=FirebaseStorage.getInstance().reference

        if(firebaseAuth.currentUser!=null){
            val i:Intent=Intent(this,MainActivity::class.java)
            startActivity(i)
            finish()
        }

        binding.addPicture.setOnClickListener {
            showPictureDialog()
        }

        binding.addPicBtn.setOnClickListener {
            showPictureDialog()
        }
        binding.signupBtn.setOnClickListener{


             email=binding.signupEmail.text.toString()
             pass=binding.signupPassword.text.toString()
             confirmpass=binding.signupPassconfirm.text.toString()
             firstname=binding.signupFirstname.text.toString()
             lastname=binding.signupLastname.text.toString()
             phone=binding.signupPhone.text.toString()
             img=binding.addPicture


            if(email.isNotEmpty() && pass.isNotEmpty() && confirmpass.isNotEmpty() && firstname.isNotEmpty()&&lastname.isNotEmpty()&&phone.isNotEmpty()&& img.drawable!=null) {
                if(pass==confirmpass) {
                    firebaseAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener {
                        if(it.isSuccessful){
                                binding.loader.visibility= View.VISIBLE
                                userid=firebaseAuth.currentUser!!.uid
                                uploadImageToFirebase(imgUri!!)
                            }
                        else
                        {
                            Toast.makeText(this,it.exception!!.message,Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else{
                    Toast.makeText(this,"Passwords not matching",Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(this,"Must fill all fields",Toast.LENGTH_SHORT).show()
            }
        }

        binding.loginRedirectText.setOnClickListener {
            val loginIntent=Intent(this,LoginActivity::class.java)
            startActivity(loginIntent)
            finish()
        }

    }

    private fun chooseFromGallery(){
        val i=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(i,1)
    }

    private fun takePicWithCamera(){
       //val camera_intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        //startActivityForResult(camera_intent,0)
        dispatchTakePictureIntent()
    }

    lateinit var currphotoPath:String

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        //val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
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
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {

                    Toast.makeText(applicationContext, "failed", Toast.LENGTH_SHORT).show()
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
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
            if(resultCode==Activity.RESULT_OK){
               var f:File=File(currphotoPath)
                binding.addPicture.setImageURI(Uri.fromFile(f))
                Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
                    val f = File(currphotoPath)
                    mediaScanIntent.data = Uri.fromFile(f)
                    sendBroadcast(mediaScanIntent)
                }
                imgUri= Uri.fromFile(f)
            }
        }

        if(requestCode==1){
            if(resultCode==Activity.RESULT_OK){
                imgUri=data?.data!!
                var path:String=data?.data!!.toString()
                val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                val imgFileName="JPEG_"+timeStamp+"."+path.substring(path.lastIndexOf(".")+1)
                binding.addPicture.setImageURI(imgUri)

                }
            }
        }

    private fun uploadImageToFirebase(fileUri: Uri) {

        imgName="JPEG"+SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val image:StorageReference=storageRef.child("Profile images/"+imgName)
        image.putFile(fileUri).addOnCompleteListener() {
            if(it.isSuccessful){

                FirebaseStorage.getInstance().reference.child("Profile images/$imgName").downloadUrl.addOnCompleteListener {
                    val name=it.result.toString()
                    var user: User = User(
                        userid,
                        email,
                        pass,
                        firstname,
                        lastname,
                        phone,
                        name,
                        0
                    )
                    usersViewModel.addUser(user) {
                        if(it==null ) {
                            val intent= Intent(this,LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                            binding.loader.visibility= View.GONE
                        } else {
                            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                        }
                    }

                }
            }
            else{
                Toast.makeText(this,it.exception!!.message,Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPictureDialog(){
        val picDialog=AlertDialog.Builder(this)
        picDialog.setTitle("Select Action")
        val picDialogItems= arrayOf("Select photo from gallery","Capture photo from camera")

        picDialog.setItems(picDialogItems){
            dialog,which->
            when (which){
                0->chooseFromGallery()
                1->takePicWithCamera()
            }
        }

        picDialog.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
            outState.putString("imgUri", imgUri.toString())
            outState.putString("imgName", imgName)
        super.onSaveInstanceState(outState)
    }
}
