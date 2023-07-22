package com.aryasurya.snapfolio2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import com.aryasurya.snapfolio2.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSaveEditProfile.setOnClickListener {
            val editName = binding.editNama.text.toString()
            val editDescription = binding.editDesc.text.toString()
            updateProfile(editName, editDescription)
        }

    }

    private fun updateProfile(newName: String?, newDesc: String?) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { userId ->
            val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)
            userRef.child("username").setValue(newName)
            userRef.child("description").setValue(newDesc)
                .addOnSuccessListener{
                    Toast.makeText(this, "Profile update successfully", Toast.LENGTH_SHORT).show()
                    navigateToAccountFragment()
                }
                .addOnFailureListener{ e ->
                    Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun navigateToAccountFragment() {
        val navController = findNavController(R.id.nav_host_fragment)
        navController.navigate(R.id.navigation_account)
    }
}