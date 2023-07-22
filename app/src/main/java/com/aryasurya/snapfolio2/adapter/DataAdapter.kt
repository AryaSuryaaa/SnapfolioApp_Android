package com.aryasurya.snapfolio2

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aryasurya.snapfolio2.R
import com.aryasurya.snapfolio2.data.ItemData
import com.bumptech.glide.Glide

class DataAdapter(private val context: Context, private val dataItems: List<ItemData>) :
    RecyclerView.Adapter<DataAdapter.AppHolder>() {

    private var dialog: Dialog? = null
    interface Dialog {
        fun onClick(pos: Int)
    }

    fun getDialog(): Dialog? {
        return dialog
    }

    fun setDialog(dialog: Dialog) {
        this.dialog = dialog
    }

    inner class AppHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var title: TextView
        lateinit var desc: TextView
        lateinit var imageView: ImageView


        init {
            title = itemView.findViewById(R.id.title_item)
            desc = itemView.findViewById(R.id.description_item)
            imageView = itemView.findViewById(R.id.img_item)
            itemView.setOnClickListener{
                dialog?.onClick(layoutPosition)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataAdapter.AppHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_data, parent, false)
        return AppHolder(v)
    }

    override fun onBindViewHolder(holder: DataAdapter.AppHolder, position: Int) {
        val item = dataItems[position]
        holder.title.text = item.title
        holder.desc.text = item.description
        Glide.with(context)
            .load(item.image)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return dataItems.size
    }
}