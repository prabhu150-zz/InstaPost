package com.example.firebase

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.signup_page.*

class SignUpActivity : AppCompatActivity() {

    //    private lateinit var auth : FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var databaseRef: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_page)


    }


    fun validateUserInput(): Boolean {
        // validate users here...

        return true
    }

    fun showErrorMessages(messages: String) {

        // make a list of messages to display

    }


    override fun onStart() {
        super.onStart()


        FirebaseApp.initializeApp(this)

        sign_up_button.setOnClickListener {

            if (validateUserInput()) {
                // make a new user object
                // add it to our current database

                databaseRef = FirebaseDatabase.getInstance().getReference("users")

                val userID = databaseRef.push().key ?: ""

                if (userID.isEmpty())
                    throw Exception("Database Reference Error!")


                val currentUser =
                    User(userID, email.text.toString(), password.text.toString(), nickname.text.toString())

                databaseRef.child(userID).setValue(currentUser)
            }
        }


    }

}
