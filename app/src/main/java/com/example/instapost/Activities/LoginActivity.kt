package com.example.instapost.Activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.instapost.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_signin.*
import java.util.regex.Pattern


class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private val Tag: String = "LoginActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        mAuth = FirebaseAuth.getInstance()

        if (mAuth.currentUser != null) {
            redirectToDashBoard()
        }
    }

    fun notifyByToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun redirectToDashBoard() {
        val intent = Intent(this, FragmentManager::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) // prevents back button to log you out
        startActivity(intent)
    }


    fun validateEmail(email: String): Boolean {
        val pattern = Pattern.compile("^(.+)@(.+)$")
        return pattern.matcher(email).matches()
    }

    fun validateUserInput(): Boolean {

        val validSignup =
            !((TextUtils.isEmpty(email.text.toString())
                    ) || TextUtils.isEmpty(password.text.toString()) || !(password.text.toString().length > 5) || !validateEmail(
                email.text.toString()
            ))

        Log.d(Tag, "All fields are valid: $validSignup")
        return validSignup

    }


    private fun checkPassword() {
        password.error = null
        password.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (password.text.toString().isEmpty() || password.text.toString().length < 6)
                    password.error = getString(R.string.password_invalid_error)

            }

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


    override fun onStart() {
        super.onStart()

        register_redirect.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        sign_in_button.setOnClickListener {

            checkEmailFormat()
            checkPassword()

            if (validateUserInput())
                mAuth.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
                    .addOnCompleteListener {

                        if (it.isSuccessful) {
                            notifyByToast("Login Successful!")
                            redirectToDashBoard()
                        } else {
                            notifyByToast("Login Failed: ${it.exception?.message}")
                        }

                    }
            else
                notifyByToast("Improper fields! Please re-enter values")
        }
    }

}
