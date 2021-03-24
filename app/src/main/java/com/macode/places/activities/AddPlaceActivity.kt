package com.macode.places.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputLayout
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.macode.places.R
import com.macode.places.database.DatabaseHandler
import com.macode.places.databinding.ActivityAddPlaceBinding
import com.macode.places.models.PlaceModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class AddPlaceActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "PlacesAppImages"
    }

    private lateinit var binding: ActivityAddPlaceBinding
    private var calendar = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var saveImageToInternalStorage: Uri? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private var placeDetails: PlaceModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPlaceBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val toolbar = findViewById<Toolbar>(R.id.addPlaceToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add a place"
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#fe8a71")))

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            placeDetails = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS)
        }

        dateSetListener = DatePickerDialog.OnDateSetListener {
                view, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        updateDateInView()

        if (placeDetails != null) {
            supportActionBar?.title = "Edit place"
            binding.titleEditInput.setText(placeDetails!!.title)
            binding.descriptionEditInput.setText(placeDetails!!.description)
            binding.dateEditInput.setText(placeDetails!!.date)
            binding.locationEditInput.setText(placeDetails!!.location)
            latitude = placeDetails!!.latitude
            longitude = placeDetails!!.longitude

            saveImageToInternalStorage = (Uri.parse(placeDetails!!.image))
            binding.appCompatImageView.setImageURI(saveImageToInternalStorage)
            binding.savePlaceButton.text = "Update"
        }

        binding.dateEditInput.setOnClickListener(this)
        binding.addImageTextButton.setOnClickListener(this)
        binding.savePlaceButton.setOnClickListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.dateEditInput -> {
                val dateDialog = DatePickerDialog(
                    this@AddPlaceActivity,
                    dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH))

                dateDialog.show()
                dateDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#fe8a71"))
                dateDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#fe8a71"))
            }
            R.id.addImageTextButton -> {
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera")
                pictureDialog.setItems(pictureDialogItems) {
                    _, which ->
                    when(which) {
                        0 -> choosePhotoFromGallery()
                        1 -> takePhotoWithCamera()
                    }
                }
                pictureDialog.show()
            }
            R.id.savePlaceButton -> {
                when {
                    binding.titleEditInput.text.isNullOrEmpty() -> {
                        showError(binding.titleInput, "Please enter title!")
                    }
                    binding.descriptionEditInput.text.isNullOrEmpty() -> {
                        showError(binding.descriptionInput, "Please enter description!")
                    }
                    binding.locationEditInput.text.isNullOrEmpty() -> {
                        showError(binding.locationInput, "Please select a location!")
                    }
                    saveImageToInternalStorage == null -> {
                        Toast.makeText(this@AddPlaceActivity, "Please select an image!", Toast.LENGTH_SHORT).show()
                    } else -> {
                        val placeModel = PlaceModel(
                                if (placeDetails == null) 0 else placeDetails!!.id,
                                binding.titleEditInput.text.toString(),
                                saveImageToInternalStorage.toString(),
                                binding.descriptionEditInput.text.toString(),
                                binding.dateEditInput.text.toString(),
                                binding.locationEditInput.text.toString(),
                                latitude,
                                longitude
                        )
                        val dbHandler = DatabaseHandler(this)
                        if (placeDetails == null) {
                            val addPlace = dbHandler.addPlace(placeModel)
                            if (addPlace > 0) {
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        } else {
                            val updatePlace = dbHandler.updatePlace(placeModel)
                            if (updatePlace > 0) {
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }

                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                if (data != null) {
                    val contentURI = data.data
                    try {
                        val selectedImageBitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.contentResolver,
                            contentURI!!
                        ))
                        saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)
                        Log.i("Saved image: ", "Path :: $saveImageToInternalStorage")
                        binding.appCompatImageView.setImageBitmap(selectedImageBitmap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this@AddPlaceActivity, "Failed to load the image!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (requestCode == CAMERA) {
                if (data != null) {
                    val bitmap: Bitmap = data!!.extras!!.get("data") as Bitmap
                    saveImageToInternalStorage = saveImageToInternalStorage(bitmap)
                    Log.i("Saved image: ", "Path :: $saveImageToInternalStorage")
                    binding.appCompatImageView.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun choosePhotoFromGallery() {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY)
                }
            }

            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }

    private fun takePhotoWithCamera() {
        Dexter.withContext(this).withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        ).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val galleryIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(galleryIntent, CAMERA)
                }
            }

            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this).setMessage("It looks like you have turned off permission required for this feature. " +
                "It can be enabled under Application Settings.")
            .setPositiveButton("Go to settings")
            {_, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel")
            {dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun updateDateInView() {
        val format = "MM.dd.yyyy"
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        binding.dateEditInput.setText(sdf.format(calendar.time).toString())
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.png")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return Uri.parse(file.absolutePath)
    }

    private fun showError(layout: TextInputLayout, text: String) {
        layout.error = text
        layout.requestFocus()
    }
}