package com.macode.places.activities

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.macode.places.R
import com.macode.places.databinding.ActivityPlaceDetailBinding
import com.macode.places.models.PlaceModel

class PlaceDetailActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityPlaceDetailBinding
    private var placeDetailModel: PlaceModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaceDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val toolbar = findViewById<Toolbar>(R.id.placeDetailToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#FFBB86FC")))

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            placeDetailModel = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS)
        }

        if (placeDetailModel != null) {
            supportActionBar!!.title = placeDetailModel!!.title
            binding.placeDetailImage.setImageURI(Uri.parse(placeDetailModel!!.image))
            binding.placeDetailTitle.text = placeDetailModel!!.title
            binding.placeDetailDescription.text = placeDetailModel!!.description
        }

        val mapsFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.googleMaps) as SupportMapFragment
        mapsFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMaps: GoogleMap?) {
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            placeDetailModel = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS)
        }

        val itemLat = placeDetailModel!!.latitude
        val itemLong = placeDetailModel!!.longitude

        googleMaps?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(itemLat, itemLong), 12f))
        googleMaps?.addMarker(
            MarkerOptions().position(LatLng(itemLat, itemLong)).title(
                placeDetailModel!!.location
            )
        )

    }

}