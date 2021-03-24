package com.macode.places.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.macode.places.database.DatabaseHandler
import com.macode.places.databinding.SingleItemPlaceBinding
import com.macode.places.models.PlaceModel

open class PlacesAdapter(private val context: Context, private var list: ArrayList<PlaceModel>) :
    RecyclerView.Adapter<PlacesAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: SingleItemPlaceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SingleItemPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        with(holder) {
            binding.singlePlaceImage.setImageURI(Uri.parse(model.image))
            binding.singlePlaceTitle.text = model.title
            binding.singlePlaceDescription.text = model.description
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }
}