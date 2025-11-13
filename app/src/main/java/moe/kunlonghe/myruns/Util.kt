package moe.kunlonghe.myruns

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import com.google.android.gms.maps.model.LatLng
import java.io.IOException

object Util {
    fun requestAllPermissions(activity: Activity?) {
        if (Build.VERSION.SDK_INT < 23) return
        val a = activity ?: return

        val needed = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(a, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.CAMERA)
        }

        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(a, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                needed.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
            if (ContextCompat.checkSelfPermission(a, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                needed.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
        } else {
            if (ContextCompat.checkSelfPermission(a, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                needed.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        // Location permissions
        if (ContextCompat.checkSelfPermission(a, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(a, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(a, needed.toTypedArray(), 100)
        }
    }
    
    fun hasMediaPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun getBitmap(context: Context, imgUri: Uri): Bitmap {
        var bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(imgUri))
            ?: throw IllegalArgumentException("Unable to decode image from URI")
        
        if (bitmap.isRecycled) {
            throw IllegalStateException("Bitmap is recycled")
        }
        
        val matrix = Matrix()
        
        val rotation = getImageRotation(context, imgUri)
        if (rotation != 0f) {
            matrix.postRotate(rotation)
        }
        
        return try {
            val ret = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (ret != bitmap) {
                bitmap.recycle()
            }
            ret
        } catch (e: OutOfMemoryError) {
            bitmap.recycle()
            throw e
        }
    }
    
    private fun getImageRotation(context: Context, imgUri: Uri): Float {
        return try {
            context.contentResolver.openInputStream(imgUri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
        } catch (e: IOException) {
            0f
        }
    }
    
    // Location utilities
    fun serializeLocationList(locations: ArrayList<LatLng>): ByteArray? {
        if (locations.isEmpty()) return null
        
        return try {
            val stringBuilder = StringBuilder()
            for (latLng in locations) {
                stringBuilder.append("${latLng.latitude},${latLng.longitude};")
            }
            stringBuilder.toString().toByteArray()
        } catch (e: Exception) {
            null
        }
    }
    
    fun deserializeLocationList(data: ByteArray): ArrayList<LatLng> {
        val locations = ArrayList<LatLng>()
        try {
            val dataStr = String(data)
            val points = dataStr.split(";")
            for (point in points) {
                if (point.isNotEmpty()) {
                    val coords = point.split(",")
                    if (coords.size == 2) {
                        val lat = coords[0].toDouble()
                        val lng = coords[1].toDouble()
                        locations.add(LatLng(lat, lng))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return locations
    }
    
    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, 
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
