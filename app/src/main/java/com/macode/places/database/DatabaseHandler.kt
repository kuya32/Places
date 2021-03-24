package com.macode.places.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.macode.places.models.PlaceModel

class DatabaseHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "Places.db"
        private val TABLE_PLACES = "places"
        private val KEY_ID = "_id"
        private val KEY_TITLE = "title"
        private val KEY_IMAGE = "image"
        private val KEY_DESCRIPTION = "description"
        private val KEY_DATE = "date"
        private val KEY_LOCATION = "location"
        private val KEY_LATITUDE = "latitude"
        private val KEY_LONGITUDE = "longitude"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_PLACES_TABLE = ("CREATE TABLE $TABLE_PLACES($KEY_ID INTEGER PRIMARY KEY, $KEY_TITLE TEXT," +
                "$KEY_IMAGE TEXT, $KEY_DESCRIPTION TEXT, $KEY_DATE TEXT, $KEY_LOCATION TEXT, $KEY_LATITUDE TEXT," +
                "$KEY_LONGITUDE TEXT)")
        db?.execSQL(CREATE_PLACES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PLACES")
        onCreate(db)
    }

    fun addPlace(place: PlaceModel): Long {
        val values = ContentValues()
        values.put(KEY_TITLE, place.title)
        values.put(KEY_IMAGE, place.image)
        values.put(KEY_DESCRIPTION, place.description)
        values.put(KEY_DATE, place.date)
        values.put(KEY_LOCATION, place.location)
        values.put(KEY_LATITUDE, place.latitude)
        values.put(KEY_LONGITUDE, place.longitude)
        val db = this.writableDatabase
        val result = db.insert(TABLE_PLACES, null, values)
        db.close()
        return result
    }

    fun getPlacesList() : ArrayList<PlaceModel> {
        val placeList = ArrayList<PlaceModel>()
        val selectQuery = "SELECT * FROM $TABLE_PLACES"
        val db = this.readableDatabase

        try {
            val cursor = db.rawQuery(selectQuery, null)

            if (cursor.moveToFirst()) {
                do {
                    val place = PlaceModel(
                            cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                            cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                            cursor.getString(cursor.getColumnIndex(KEY_IMAGE)),
                            cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)),
                            cursor.getString(cursor.getColumnIndex(KEY_DATE)),
                            cursor.getString(cursor.getColumnIndex(KEY_LOCATION)),
                            cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)),
                            cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE))
                    )
                    placeList.add(place)
                } while (cursor.moveToNext())
            }
        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }

        return placeList
    }
}