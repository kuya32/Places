package com.macode.places.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.macode.places.R
import com.macode.places.adapters.PlacesAdapter
import com.macode.places.database.DatabaseHandler
import com.macode.places.databinding.ActivityMainBinding
import com.macode.places.models.PlaceModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    companion object {
        var ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        val EXTRA_PLACE_DETAILS = "extraPlaceDetails"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val toolbar = findViewById<Toolbar>(R.id.mainToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Happy Places"

        binding.addPlaceFab.setOnClickListener {
            val intent = Intent(this, AddPlaceActivity::class.java)
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }

        getPlacesListFromLocalDB()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                getPlacesListFromLocalDB()
            } else {
                Log.e("Activity", "Cancelled or backed pressed")
            }
        }
    }

    private fun setupPlacesRecyclerView(placeList: ArrayList<PlaceModel>) {
        binding.placesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.placesRecyclerView.setHasFixedSize(true)

        val placesAdapter = PlacesAdapter(this, placeList)
        binding.placesRecyclerView.adapter = placesAdapter

        placesAdapter.setOnclickListener(object: PlacesAdapter.OnClickListener {
            override fun onClick(position: Int, model: PlaceModel) {
                val intent = Intent(this@MainActivity, PlaceDetailActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent)
            }
        })
    }

    private fun getPlacesListFromLocalDB() {
        val dbHandler = DatabaseHandler(this)
        val getPlaceList: ArrayList<PlaceModel> = dbHandler.getPlacesList()

        if(getPlaceList.size > 0) {
            binding.placesRecyclerView.visibility = View.VISIBLE
            binding.noPlacesAvailableText.visibility = View.GONE
            setupPlacesRecyclerView(getPlaceList)
        } else {
            binding.placesRecyclerView.visibility = View.GONE
            binding.noPlacesAvailableText.visibility = View.VISIBLE
        }
    }
}