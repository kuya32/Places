package com.macode.places.activities

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.macode.places.R
import com.macode.places.databinding.ActivityPlaceDetailBinding
import com.macode.places.models.PlaceModel

class PlaceDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlaceDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaceDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val toolbar = findViewById<Toolbar>(R.id.placeDetailToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#3da4ab")))

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        var placeDetailModel: PlaceModel? = null

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            placeDetailModel = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS)
        }

        if (placeDetailModel != null) {
            supportActionBar!!.title = placeDetailModel.title
            binding.placeDetailImage.setImageURI(Uri.parse(placeDetailModel.image))
            binding.placeDetailTitle.text = placeDetailModel.title
            binding.placeDetailDescription.text = placeDetailModel.description
        }
    }
}