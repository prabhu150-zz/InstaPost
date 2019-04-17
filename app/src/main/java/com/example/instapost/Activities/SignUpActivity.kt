package com.example.instapost.Activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.instapost.Model.User
import com.example.instapost.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.signup_page.*
import java.util.regex.Pattern


class SignUpActivity : AppCompatActivity() {

    private val IMAGE_REQUEST = 11
    private lateinit var auth: FirebaseAuth
    private val Tag = "downloadUrl"

    private var imageURI: Uri? = null
    private var currentUpload: StorageTask<UploadTask.TaskSnapshot>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_page)
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            redirectToDashboard()
        }

    }


    private fun logit(message: String) {
        Log.d(Tag, message)
    }

    fun notifyUser(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, id ->
            }
        val alert = builder.create()
        alert.show()
    }


    fun notifyByToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun validateEmail(email: String): Boolean {
        val pattern = Pattern.compile("^(.+)@(.+)$")
        return pattern.matcher(email).matches()
    }


    private fun validateUserInput(): Boolean {

        val validSignup =
            !(TextUtils.isEmpty(name.text.toString()) || TextUtils.isEmpty(email.text.toString()) || TextUtils.isEmpty(
                nickname.text.toString()
            ) || TextUtils.isEmpty(password.text.toString()) || !(password.text.toString() == confirmPassword.text.toString() && password.text.toString().length > 5) || !validateEmail(
                email.text.toString()
            ))

        logit("All fields are valid: $validSignup")
        return validSignup

    }


    private fun checkIfEmpty(txtView: TextView, error: String) {
        txtView.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus)
                if (txtView.text.toString().isEmpty())
                    txtView.error = error
        }

    }

    private fun checkEmailFormat() {
        email.error = null
        email.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (email.text.toString().isEmpty())
                    email.error = getString(R.string.blank_email_error)
                else if (!validateEmail(email.text.toString()))
                    email.error = getString(R.string.invalid_email_error)
            }

        }
    }


    private fun checkPasswordsMatch() {
        password.error = null
        password.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {

                if (password.text.toString().isEmpty()) {
                    password.error = getString(R.string.blank_password_error)
                } else if (password.text.toString().length < 6)
                    password.error = getString(R.string.password_invalid_error)
            }


            confirmPassword.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    if (confirmPassword.text.toString().isEmpty() && password.text.toString().isNotEmpty())
                        confirmPassword.error = getString(R.string.blank_confirmpassword_error)
                    else if (password.text.toString() != confirmPassword.text.toString())
                        confirmPassword.error = getString(R.string.password_dont_match_error)
                }


            }


        }
    }


    private fun storeUserDetails() {


        val databaseReference = FirebaseDatabase.getInstance().reference.child("/users/")
        val userID = databaseReference.push().key ?: ""

        if (userID.isEmpty())
            throw IllegalStateException("Database Reference Error!")

        if (imageURI != null) {
            logit("uploading an image...")
            uploadImageToStorage(userID)
        } else {
            pushToRealTimeDb(userID, "")
        }

    }

    private fun pushToRealTimeDb(userID: String, profilePic: String) {

        val guestLogin = User(
            userID,
            name.text.toString(),
            email.text.toString(),
            password.text.toString(),
            nickname.text.toString()
        )

        if (profilePic.isNotEmpty())
            guestLogin.profilePic = profilePic

        logit("Current user details ${guestLogin.nickName}")
        FirebaseDatabase.getInstance().reference.child("/users/").child(userID).setValue(guestLogin)
    }

    private fun getFileExtension(uri: Uri?): String? {
        val cR = contentResolver
        val mime = MimeTypeMap.getSingleton()
        if (uri != null)
            return mime.getExtensionFromMimeType(cR.getType(uri))
        return ""
    }

    private fun uploadImageToStorage(userID: String) {

        val storageRef: StorageReference = FirebaseStorage.getInstance().reference.child("/profile-pics/")

        val fileName = System.currentTimeMillis().toString() + "" + getFileExtension(imageURI ?: Uri.EMPTY)

        val fileReference = storageRef.child(fileName) // gets a file

        if (imageURI == null)
            return

        currentUpload = fileReference.putFile(imageURI ?: Uri.EMPTY) // load an image
            .addOnSuccessListener {


                val handler = Handler()
                handler.postDelayed({ createUserprogress.progress = 0 }, 500)

                logit("Uploaded image successfully!")

                Toast.makeText(this@SignUpActivity, "Upload successful", Toast.LENGTH_LONG).show()

                storageRef.child(fileName).downloadUrl.addOnSuccessListener {
                    logit("download url: $it")
                    pushToRealTimeDb(userID, it.toString())
                }

                createUserprogress.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                notifyByToast(e.message.toString())
                createUserprogress.visibility = View.GONE
            }
            .addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                createUserprogress.progress = progress.toInt()
            }

        return

    }

    override fun onStart() {
        signUpCurrentUser()

        sign_in_button.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        super.onStart()
    }


    private fun redirectToDashboard() {
        val intent = Intent(this, FragmentManager::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        // prevents back from logout
        //TODO back button should be fixed, so it doesnt log you out
        startActivity(intent)
    }


    private fun createNewUser() {
        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(
                email.text.toString(), password.text.toString()
            ).addOnCompleteListener {

                createUserprogress.visibility = View.GONE

                if (it.isSuccessful) {
                    logit("New user added to auth!")
                    storeUserDetails()
                    signin()

                } else {
                    logit("Failed to create user error: ${it.exception?.message.toString()}")
                    notifyByToast("Failed Signup: ${it.exception?.message.toString()}")
                }
            }

    }

    private fun signUpCurrentUser() {

        checkIfEmpty(name, getString(R.string.name_error))
        checkIfEmpty(nickname, getString(R.string.blank_nickname_error))
        checkIfEmpty(confirmPassword, getString(R.string.blank_confirmpassword_error))

        checkEmailFormat()
        checkPasswordsMatch()

        sign_up_button.setOnClickListener {

            if (validateUserInput()) {
                createUserprogress.visibility = View.VISIBLE

                FirebaseDatabase.getInstance().reference.child("users").orderByChild("nickName")
                    .equalTo(nickname.text.toString()).addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                                logit("Database Error ${p0.message}")
                            }

                            override fun onDataChange(p0: DataSnapshot) {
                                if (p0.childrenCount > 0) {
                                    createUserprogress.visibility = View.GONE
                                    nickname.error = "That one is taken!"
                                    notifyByToast("Please select a different nickname!")
                                } else {
                                    createUserprogress.visibility = View.VISIBLE
                                    logit("Creating user with unique nickname!")
                                    createNewUser()
                                }

                            }
                        }
                    )

            } else {
                notifyByToast("Improper fields, please re-enter values")
            }
        }

        select_image_button.setOnClickListener {
            pickFromGallery()
        }

    }


    private fun signin() {
        auth.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
            .addOnSuccessListener {
                logit("User signed in!")
                redirectToDashboard()
            }
            .addOnFailureListener {
                logit("Couldnt Login! Reason: ${it.message}")
                notifyByToast("Couldnt Login! Reason: ${it.message}")
            }
    }


    private fun pickFromGallery() {
        val gallery = Intent()
        gallery.type = "image/*"
        gallery.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(gallery, IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null)
            if (data.data != null) {
                imageURI = data.data
                previewProfilePic.visibility = View.VISIBLE
                Picasso.get().load(imageURI).into(previewProfilePic)
                select_image_button.visibility = View.INVISIBLE
            } else {
                notifyByToast("No File Selected!")
            }
    }


}


