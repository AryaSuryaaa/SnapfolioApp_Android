package com.aryasurya.snapfolio2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.aryasurya.snapfolio2.data.User
import com.aryasurya.snapfolio2.databinding.ActivityRegistBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RegistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar
    private lateinit var database: DatabaseReference

    companion object {
        private const val TAG = "EmailPassword"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressBar = findViewById(R.id.progressBar)

        with(binding) {
            btnRegist.setOnClickListener {
                val email = binding.email.text.toString()
                val password = binding.password.text.toString()
                createAccount(email, password)
            }
        }

        // Initialize Firebase Auth
        auth = Firebase.auth
        database = Firebase.database.reference

        binding.toLogin.setOnClickListener{
            val intentToLogin = Intent(this@RegistActivity, LoginActivity::class.java)
            startActivity(intentToLogin)
        }

    }


    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            reload()
        }
    }

    private fun createAccount(email: String, password: String) {
        Log.d(TAG, "createAccount:$email")
        if (!validateForm()){ return }

        progressBar.visibility = View.VISIBLE

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    var user = auth.currentUser
                    val email = binding.email.text.toString()
                    val username = binding.username.text.toString()
                    val telephone = binding.telephone.text.toString()
                    if (user != null) {
                        writeNewUser(user.uid,username, email, telephone)
                        Toast.makeText(this, "Register Susccess", Toast.LENGTH_SHORT).show()
                        val intentToMainActivity = Intent(this@RegistActivity, MainActivity::class.java)
                        startActivity(intentToMainActivity)
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    var errorMessage = "Authentication failed."

                    if ( task.exception is FirebaseAuthUserCollisionException) {
                        errorMessage = "Email already exists. Please use a different email."
                    }

                    Toast.makeText(
                        baseContext,
                        errorMessage,
                        Toast.LENGTH_SHORT,
                    ).show()
                }
                progressBar.visibility = View.INVISIBLE
            }
    }

    fun writeNewUser(userId: String, name: String, email: String, telephone: String) {
        val user = User( name, "", email, telephone, "")

        database.child("users").child(userId).setValue(user)
    }

    private fun reload() {
        auth.currentUser!!.reload().addOnCompleteListener { task ->
            if (task.isSuccessful) {

                Toast.makeText(applicationContext, "Reload successful!", Toast.LENGTH_SHORT).show()
            } else {
                Log.e(TAG, "reload", task.exception)
                Toast.makeText(applicationContext, "Failed to reload user.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateForm(): Boolean {
        var valid = true

        val email = binding.email.text.toString()
        if (TextUtils.isEmpty(email)) {
            binding.layoutEmail.error = "Required!"
            valid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutEmail.error = "Invalid email address!"
        }
        else {
            binding.email.error = null
        }

        val password = binding.password.text.toString()
        if (TextUtils.isEmpty(password)) {
            binding.layoutPassword.error = "Required!"
            valid = false
        } else {
            binding.password.error = null
        }

        return valid
    }

}