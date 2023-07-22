package com.aryasurya.snapfolio2

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.findNavController
import com.aryasurya.snapfolio2.databinding.ActivityAddDataBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Date

class AddDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddDataBinding
    private lateinit var imageView: ImageView
    private  val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private lateinit var progressDialog: ProgressDialog
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "AddDataAcivity"
        const val EXTRA_ID = "id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_DESC = "desc"
        const val EXTRA_IMG = "img"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this@AddDataActivity)
        progressDialog.setTitle("Sedang diproses...")

        imageView = findViewById(R.id.add_image)
        imageView.setOnClickListener{
            selectImage()
        }

        val updateOption = intent
        if (updateOption != null && updateOption.hasExtra(EXTRA_ID)) {
            val titleText = updateOption.getStringExtra(EXTRA_TITLE)
            val description = updateOption.getStringExtra(EXTRA_DESC)
            val image = updateOption.getStringExtra(EXTRA_IMG)
            Glide.with(applicationContext)
                .load(image)
                .into(imageView)

            binding.titleInput.setText(titleText)
            binding.descriptionInput.setText(description)
        }

        binding.btnPost.setOnClickListener {
            val title = binding.titleInput.text.toString().trim()
            val description = binding.descriptionInput.text.toString().trim()

            if (title.isNotEmpty() && description.isNotEmpty()) {
                upload(title, description)
            } else {
                Toast.makeText(this, "Semua data harus diisi!!", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun saveData(title: String, description: String, img: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Create a new user with a first and last name
        val user = hashMapOf(
            "title" to title,
            "description" to description,
            "img" to img,
            "userId" to userId
        )

        // Mengambil ID jika  user melakukan edit post
        val id = intent.getStringExtra(EXTRA_ID)

        if (id != null) {
            db.collection("users").document(id)
                .set(user)
                .addOnSuccessListener {
                    Log.d(TAG, "Data berhasil diperbarui")
                    Toast.makeText(this, "Data berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    navigateToHomeFragment()
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error perbarui data", e)
                    finish()
                }
        } else {
            // Add a new document with a generated ID
            db.collection("users")
                .add(user)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                    Toast.makeText(this, "data berhasil ditambah", Toast.LENGTH_SHORT).show()
                    navigateToHomeFragment()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }
        }
    }

    private fun upload(title: String, description: String) {
        progressDialog.show()
        imageView = findViewById(R.id.add_image)


        // Get the data from an ImageView as bytes
        imageView.isDrawingCacheEnabled = true
        imageView.buildDrawingCache()
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()


        val referenceStorage: StorageReference = storage.getReference("images")
            .child("IMG"+ Date().time + ".jpg")

        var uploadTask = referenceStorage.putBytes(data)

        uploadTask.addOnFailureListener { exception ->
            // Handle unsuccessful uploads
            Toast.makeText(applicationContext, "Data gagal diupload", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...

            taskSnapshot.metadata?.reference?.downloadUrl?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    saveData(title, description, downloadUri.toString())
                    Log.d(TAG, "ini link gambar $downloadUri")
                } else {
                    // Handle unsuccessful download URL retrieval
                }
            }
            Toast.makeText(applicationContext, "Data berhasil diupload", Toast.LENGTH_LONG).show()
            progressDialog.dismiss()
        }


    }

    private fun navigateToHomeFragment() {
        finish()
    }

    private fun selectImage() {
        val optionActions = arrayOf<CharSequence>("Take photo", "Choose from library", "Cancel")
        val dialogBuilder = AlertDialog.Builder(this@AddDataActivity)
        dialogBuilder.setTitle(R.string.app_name)
        dialogBuilder.setIcon(R.mipmap.ic_launcher)
        dialogBuilder.setItems(optionActions) { dialogInterface, i ->
            when(i) {
                0 -> {
                    val take = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(take, 10)
                }
                1 -> {
                    val pick = Intent(Intent.ACTION_PICK)
                    pick.setType("image/*")
                    startActivityForResult(Intent.createChooser(pick, "Select Image"), 20)
                }
                2 -> {
                    dialogInterface.dismiss()
                }
            }
        }
        dialogBuilder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 20 && resultCode == RESULT_OK && data != null) {
            val path: Uri? = data.data
            val thread = Thread{
                try {
                    val inputStream = path?.let { contentResolver.openInputStream(it) }
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    imageView.post {
                        imageView.setImageBitmap(bitmap)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            thread.start()
        }
        if (requestCode == 10 && resultCode == RESULT_OK) {
            val extras: Bundle? = data?.extras!!

            val thread = Thread{
                val bitmap = extras?.get("data") as? Bitmap
                imageView?.post {
                    imageView.setImageBitmap(bitmap)
                }
            }
            thread.start()
        }
    }
}