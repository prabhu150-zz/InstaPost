package com.example.instapost.Fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.instapost.Activities.FragmentManager
import com.example.instapost.Model.HashTag
import com.example.instapost.Model.Post
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
import kotlinx.android.synthetic.main.create_post.*
import java.util.regex.Pattern

class CreatePost : Fragment() {


    private val IMAGE_REQUEST = 1

    private var currentUpload: StorageTask<UploadTask.TaskSnapshot>? = null
    private var mImageUri: Uri? = null
    private val LogTag = "createPostActivity"
    private lateinit var currentPost: Post


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.create_post, null)
    }

    fun logDebug(message: String) {
        Log.d(LogTag, message)
    }

    fun toastNotication(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }


    private fun pickFromGallery() {
        val gallery = Intent()
        gallery.type = "image/*"
        gallery.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(gallery, IMAGE_REQUEST)
    }

    private fun checkIfEmpty(txtView: TextView, error: String) {
        txtView.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus)
                if (txtView.text.toString().isEmpty())
                    txtView.error = error
                else
                    txtView.error = null
        }

    }

    override fun onStart() {
        super.onStart()

        checkIfEmpty(caption, "Please enter a caption")
        checkIfEmpty(hashtags, "At least one hashtag")


        select_image_button.setOnClickListener {
            pickFromGallery()

            if (mImageUri == null) {
                toastNotication("Please pick a file to post!")
                return@setOnClickListener
            }
        }

        upload_button.setOnClickListener {

            if (mImageUri == null) {
                toastNotication("Please upload a file to post!")
                return@setOnClickListener
            }

            if (validateUserInput()) {
                uploadFile()
                logDebug("New post detected!")
            } else {
                toastNotication("Please fill all the fields!")
            }

        }

    }

    private fun validateUserInput(): Boolean {
        val validPost = caption.text.isNotBlank() && hashtags.text.isNotBlank()
        logDebug("New post all fields status: $validPost")
        return validPost
    }

    private fun createAndSaveNewPost(downloadURL: String) {
        val userDbRef = FirebaseDatabase.getInstance().reference.child("/users/")
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
        logDebug("Current User email: $userEmail")

        if (userEmail.isEmpty())
            throw Exception("Invalid User!")

        var currentUser = User()

        val postID = userDbRef.push().key ?: ""
        currentPost =
            Post(
                postID,
                caption.text.toString(),
                System.currentTimeMillis().toString(), // used later to rearrange pics
                downloadURL
            )

        checkForHashTags(currentPost)

        val query3 = FirebaseDatabase.getInstance().getReference("/users/")
            .orderByChild("email")
            .equalTo(userEmail)


        val postListener = object : ValueEventListener {
            override fun onDataChange(tasks: DataSnapshot) {
                if (tasks.exists())

                    for (currentTask in tasks.children) {
                        currentUser = currentTask.getValue(User::class.java) ?: User()
                        logDebug("Inside on data change module")
                        toastNotication("Got current user nickname ${currentUser.nickName}")
                    }

                updateUserInRealTimeDb(currentUser, currentPost)
                logDebug("User nickname : ${currentUser.userID}")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                logDebug("""Database error:${databaseError.message}""")
            }
        }

        query3.addListenerForSingleValueEvent(postListener)
    }


    private fun checkForHashTags(currentPost: Post) {
        val hashtagsList = getAllHashTags(hashtags.text.toString())
        val hashDbRef = FirebaseDatabase.getInstance().reference.child("/hashtags/")

        for (currentHashTag in hashtagsList) {
            val hashTagQuery = hashDbRef
                .orderByChild("hashTagText")
                .equalTo(currentHashTag)

            val hashTagListener = object : ValueEventListener {

                override fun onDataChange(hashtagData: DataSnapshot) {

                    if (hashtagData.childrenCount > 0) // hashtag seen before
                    {
                        var updatedHashTag: HashTag? = null
                        for (currentTask in hashtagData.children) {
                            updatedHashTag = currentTask.getValue(HashTag::class.java)
                            logDebug("Updated hashtag #${updatedHashTag?.hashTagText}")
                            toastNotication("Current hashtag count ${updatedHashTag?.getPostActivity()}")
                        }
                        updateHashTagCountInDataBase(updatedHashTag, currentPost)
                    } else {
                        logDebug("Didnt find hashtag!")
                        addNewHashTagToDb(HashTag(currentHashTag), currentPost)
                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    logDebug("""Database error:${databaseError.message}""")
                }
            }

            hashTagQuery.addListenerForSingleValueEvent(hashTagListener)
        }


    }


    private fun addNewHashTagToDb(currentHashTag: HashTag, currentPost: Post) {
        val hashDbRef = FirebaseDatabase.getInstance().reference.child("/hashtags/")

        currentHashTag.addPost(currentPost)
        logDebug("Adding new hashtag #${currentHashTag.hashTagText}")
        hashDbRef.child(currentHashTag.hashTagText).setValue(currentHashTag)
    }


    private fun updateHashTagCountInDataBase(updatedHashTag: HashTag?, currentPost: Post) {

        if (updatedHashTag == null)
            return

        val hashDbRef = FirebaseDatabase.getInstance().reference.child("/hashtags/")

        updatedHashTag.addPost(currentPost)
        hashDbRef.child(updatedHashTag.hashTagText).setValue(updatedHashTag)
    }

    private fun updateUserInRealTimeDb(currentUser: User, currentPost: Post) {
        val userDbRef = FirebaseDatabase.getInstance().reference.child("/users/")

        currentUser.posts.add(currentPost)

        userDbRef.child(currentUser.userID).setValue(currentUser)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    logDebug("User has been updated. Number of user posts: ${currentUser.getPostActivity()}")
                    toastNotication("Status updated!")
                    redirectToDashboard()
                } else
                    logDebug("Didnt work. Reason ${it.exception?.message.toString()}")
            }
    }


    private fun redirectToDashboard() {
        val dashboardFragment = DashBoard()
        val args = Bundle()
        args.putInt("request", 11)
        dashboardFragment.arguments = args
        (activity as FragmentManager).replaceFragment(dashboardFragment)
    }

    fun getAllHashTags(userInput: String): ArrayList<String> {

        val hashTagPattern = Pattern.compile("#([a-zA-Z0-9_]+)")
        val matcher = hashTagPattern.matcher(userInput)

        val hashtags = arrayListOf<String>()

        while (matcher.find())
            hashtags.add(matcher.group(1))

        return hashtags

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null)
            if (data.data != null)
                mImageUri = data.data
            else
                logDebug("No File Selected!")

        Picasso.get().load(mImageUri).into(selected_image)

    }

    private fun getFileExtension(uri: Uri?): String? {
        val cR = activity?.contentResolver
        val mime = MimeTypeMap.getSingleton()
        if (uri != null)
            return mime.getExtensionFromMimeType(cR?.getType(uri))
        return Uri.EMPTY.toString()
    }

    private fun uploadFile() {


        val storageRef: StorageReference = FirebaseStorage.getInstance().reference.child("/posts/")

        val fileName = System.currentTimeMillis().toString() + "" + getFileExtension(mImageUri)

        logDebug("Potential filename $fileName and ImageURI $mImageUri")

        val fileReference = storageRef.child(fileName) // gets a file

        currentUpload = fileReference.putFile(mImageUri ?: Uri.EMPTY) // load an image
            .addOnSuccessListener {
                uploadPostProgress.visibility = View.VISIBLE
                logDebug("Uploaded image successfully!")
                val handler = Handler()
                handler.postDelayed({ uploadPostProgress.progress = 0 }, 500)

                Toast.makeText(activity, "Upload successful", Toast.LENGTH_LONG).show()

                storageRef.child(fileName).downloadUrl.addOnSuccessListener {
                    logDebug("download url: $it")
                    createAndSaveNewPost(it.toString())

                }
                uploadPostProgress.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                toastNotication(e.message.toString())
                uploadPostProgress.visibility = View.GONE

            }
            .addOnProgressListener { taskSnapshot ->

                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                uploadPostProgress.progress = progress.toInt()
            }

        uploadPostProgress.visibility = View.GONE

        return
    }


}




