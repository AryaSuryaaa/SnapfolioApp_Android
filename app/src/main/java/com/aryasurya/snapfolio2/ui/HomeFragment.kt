package com.aryasurya.snapfolio2.ui

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aryasurya.snapfolio2.AddDataActivity
import com.aryasurya.snapfolio2.DataAdapter
import com.aryasurya.snapfolio2.R
import com.aryasurya.snapfolio2.data.ItemData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {

    private lateinit var progressDialog: ProgressDialog
    private lateinit var recyclerView: RecyclerView
    private lateinit var dataAdapter: DataAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var itemData: MutableList<ItemData>

    companion object {
        private  const val TAG = "HomeFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = Firebase.firestore
        itemData = mutableListOf()

        // RecylerView
        recyclerView = view.findViewById(R.id.list_posted)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Menampilkan data")
        dataAdapter = DataAdapter(requireContext(), itemData)


        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val decoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = dataAdapter

    }

    override fun onStart() {
        super.onStart()
        getData()
    }

    private fun getData() {
        Log.d(TAG, "getData() called")
        progressDialog.show()
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                itemData.clear()

                for (document in result) {
                    val id = document.id
                    val title = document.getString("title") ?: ""
                    val desc = document.getString("description") ?: ""
                    val img = document.getString("img") ?: ""

                    val dataItems = ItemData(id,title, desc, img)
                    itemData.add(dataItems)
                    dataAdapter.notifyDataSetChanged()
                    Log.d(TAG, "${document.id} => ${document.data}")
                }

                progressDialog.dismiss()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
                progressDialog.dismiss()
            }
    }



}

