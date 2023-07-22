package com.aryasurya.snapfolio2.ui


import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aryasurya.snapfolio2.AddDataActivity
import com.aryasurya.snapfolio2.DataAdapter
import com.aryasurya.snapfolio2.EditProfileActivity
import com.aryasurya.snapfolio2.R
import com.aryasurya.snapfolio2.data.ItemData
import com.aryasurya.snapfolio2.data.User
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.IOException

class AccountFragment : Fragment() {

    private lateinit var photoProfile: ImageView
    private lateinit var progressDialog: ProgressDialog
    private lateinit var db: FirebaseFirestore
    private lateinit var itemData: MutableList<ItemData>
    private lateinit var recyclerView: RecyclerView
    private lateinit var dataAdapter: DataAdapter

    companion object {
        private const val TAG = "AccountFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        photoProfile = view.findViewById(R.id.photo_profile)
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Menampilkan profile")
        progressDialog.setCancelable(false)

        db = Firebase.firestore
        itemData = mutableListOf()

        // RecylerView
        recyclerView = view.findViewById(R.id.list_posted)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Menampilkan data")
        dataAdapter = DataAdapter(requireContext(), itemData)
        dataAdapter.setDialog(object : DataAdapter.Dialog {
            override fun onClick(pos: Int) {
                val optionActions = arrayOf<CharSequence>("Edit", "Hapus")
                val dialogBuilder = AlertDialog.Builder(requireContext())
                dialogBuilder.setItems(optionActions) { _, i ->
                    when(i) {
                        0 -> {
                            val update = Intent(requireContext(), AddDataActivity::class.java)
                            update.putExtra(AddDataActivity.EXTRA_ID, itemData[pos].id)
                            update.putExtra(AddDataActivity.EXTRA_TITLE, itemData[pos].title)
                            update.putExtra(AddDataActivity.EXTRA_DESC, itemData[pos].description)
                            update.putExtra(AddDataActivity.EXTRA_IMG, itemData[pos].image)
                            startActivity(update)

                        }
                        1 -> {
                            deleteData(itemData[pos].id)
                        }
                    }
                }
                dialogBuilder.show()
            }
        })


        val btnChangeImage = view.findViewById<Button>(R.id.btn_change_photo_profile)
        btnChangeImage.setOnClickListener{
            selectImage()
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val decoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = dataAdapter

        userId?.let { FirebaseDatabase.getInstance().reference.child("users").child(it) }
            ?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (isAdded) { // Periksa apakah Fragment terhubung ke Activity
                        progressDialog.dismiss()
                    }

                    if (snapshot.exists()) {
                        val user = snapshot.getValue(User::class.java)
                        Log.d(TAG, "data user $user")
                        val username = user?.username
                        val loadPhotoProfile = user?.photoUrl
                        Log.d(TAG, "Ini dia $loadPhotoProfile")
                        context?.let {
                            Glide.with(it)
                                .load(loadPhotoProfile)
                                .into(photoProfile)
                        }
                        val nameProfileTextView = view.findViewById<TextView>(R.id.name_profile)
                        nameProfileTextView.text = username
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    if (isAdded) { // Periksa apakah Fragment terhubung ke Activity
                        progressDialog.dismiss()
                    }

                    Log.e(TAG, "Failed to read user data: ${error.message}")
                }

            })


        val btnAddPost: Button = view.findViewById(R.id.btn_add_post)
        btnAddPost.setOnClickListener {
            val intentToAddData = Intent(requireContext(), AddDataActivity::class.java)
            startActivity(intentToAddData)
        }

        val btnEditProfile: Button = view.findViewById(R.id.btn_edit_profile)
        btnEditProfile.setOnClickListener {
            val intentToEditProfile = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intentToEditProfile)
        }
    }

    override fun onStart() {
        super.onStart()
        getData()
    }

    private fun selectImage() {
        val optionActions = arrayOf<CharSequence>("Take Photo", "Choose from library", "Cancel")
        val dialogBuilder = AlertDialog.Builder(requireContext())
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
                    startActivityForResult(Intent.createChooser(pick, "select image"), 20)
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

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                10 -> {
                    val imageBitmap: Bitmap? = data?.extras?.get("data") as? Bitmap
                    imageBitmap?.let {
                        uploadImageToFirebase(it)
                    }
                }
                20 -> {
                    val selectedImage: Uri? = data?.data
                    selectedImage?.let {
                        try {
                            val imageBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, it)
                            uploadImageToFirebase(imageBitmap)
                        } catch (e: IOException) {
                            e.printStackTrace()
                            Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun uploadImageToFirebase(imageBitmap: Bitmap) {
        progressDialog.setMessage("Uploading Image...")

        if (isAdded) { // Periksa apakah Fragment terhubung ke Activity
            progressDialog.show() // Menampilkan progressDialog
        }
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val storageReference = FirebaseStorage.getInstance().reference.child("profile_photos").child(userId!!)

        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData: ByteArray = baos.toByteArray()

        storageReference.putBytes(imageData)
            .addOnSuccessListener { _ ->
                storageReference.downloadUrl.addOnSuccessListener { downloadUri ->
                    val photoUrl = downloadUri.toString()

                    // Update URL foto profile di database
                    val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)
                    userRef.child("photoUrl").setValue(photoUrl)
                        .addOnSuccessListener {
                            // Berhasil mengunggah dan memperbarui URL foto
                            photoProfile?.let { it1 ->
                                Glide.with(requireContext())
                                    .load(photoUrl)
                                    .into(it1)
                            }
                            progressDialog.dismiss() // Menyembunyikan progressDialog
                            Toast.makeText(requireContext(), "Photo uploaded successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            progressDialog.dismiss() // Menyembunyikan progressDialog
                            // Gagal memperbarui URL foto
                            Toast.makeText(requireContext(), "Failed to update photoUrl: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                // Gagal mengunggah gambar ke Firebase Storage
                Toast.makeText(requireContext(), "Failed to upload photo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val usersCollection = FirebaseFirestore.getInstance().collection("users")
        Log.d(TAG, "getData() called")

        if (isAdded) { // Periksa apakah Fragment terhubung ke Activity
            progressDialog.show()
        }

        usersCollection.whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                itemData.clear()

                for (documentSnapshot in querySnapshot) {
                    val id = documentSnapshot.id
                    val title = documentSnapshot.getString("title") ?: ""
                    val desc = documentSnapshot.getString("description") ?: ""
                    val img = documentSnapshot.getString("img") ?: ""

                    val dataItems = ItemData(id, title, desc, img)
                    itemData.add(dataItems)
                }

                dataAdapter.notifyDataSetChanged()
                Log.d(TAG, "Documents retrieved: ${querySnapshot.size()}")
                progressDialog.dismiss()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
                progressDialog.dismiss()
            }
    }

    private fun deleteData(id: String) {
        progressDialog.show()
        db.collection("users").document(id)
            .delete()
            .addOnSuccessListener {
                getData()
                progressDialog.dismiss()
                Log.d(TAG, "DocumentSnapshot successfully deleted!")
            }
            .addOnFailureListener { e ->
                getData()
                progressDialog.dismiss()
                Log.w(TAG, "Error deleting document", e)
            }
    }


}