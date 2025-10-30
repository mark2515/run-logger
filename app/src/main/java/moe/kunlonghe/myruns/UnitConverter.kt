package moe.kunlonghe.myruns

import android.content.Context
import moe.kunlonghe.myruns.database.ExerciseEntry
import java.text.SimpleDateFormat
import java.util.*

object UnitConverter {
    // Conversion constants
    private const val MILES_TO_KM = 1.60934
    private const val FEET_TO_METERS = 0.3048

    fun isMetric(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences("MyRunsPrefs", Context.MODE_PRIVATE)
        val unitPref = sharedPref.getString("unit_preference", "Imperial")
        return unitPref == "Metric"
    }

    fun milesToKm(miles: Double): Double {
        return miles * MILES_TO_KM
    }

    fun kmToMiles(km: Double): Double {
        return km / MILES_TO_KM
    }

    fun feetToMeters(feet: Double): Double {
        return feet * FEET_TO_METERS
    }

    fun metersToFeet(meters: Double): Double {
        return meters / FEET_TO_METERS
    }

    fun formatDistance(context: Context, distanceInMiles: Double): String {
        return if (isMetric(context)) {
            String.format("%.2f Kilometers", milesToKm(distanceInMiles))
        } else {
            String.format("%.2f Miles", distanceInMiles)
        }
    }

    fun formatClimb(context: Context, climbInFeet: Double): String {
        return if (isMetric(context)) {
            String.format("%.2f Meters", feetToMeters(climbInFeet))
        } else {
            String.format("%.2f Feet", climbInFeet)
        }
    }

    fun formatSpeed(context: Context, speedInMph: Double): String {
        return if (isMetric(context)) {
            String.format("%.2f km/h", milesToKm(speedInMph))
        } else {
            String.format("%.2f mph", speedInMph)
        }
    }

    fun formatPace(context: Context, paceInMinPerMile: Double): String {
        return if (isMetric(context)) {
            val paceInMinPerKm = paceInMinPerMile / MILES_TO_KM
            val minutes = paceInMinPerKm.toInt()
            val seconds = ((paceInMinPerKm - minutes) * 60).toInt()
            String.format("%d:%02d min/km", minutes, seconds)
        } else {
            val minutes = paceInMinPerMile.toInt()
            val seconds = ((paceInMinPerMile - minutes) * 60).toInt()
            String.format("%d:%02d min/mile", minutes, seconds)
        }
    }

    fun formatDuration(durationInSeconds: Double): String {
        val hours = (durationInSeconds / 3600).toInt()
        val minutes = ((durationInSeconds % 3600) / 60).toInt()
        val seconds = (durationInSeconds % 60).toInt()
        
        return if (hours > 0) {
            String.format("%dh %dm %ds", hours, minutes, seconds)
        } else if (minutes > 0) {
            String.format("%dm %ds", minutes, seconds)
        } else {
            String.format("%ds", seconds)
        }
    }

    fun formatDurationInMinutes(durationInSeconds: Double): String {
        val minutes = (durationInSeconds / 60).toInt()
        return "$minutes mins"
    }

    fun formatCalories(calories: Double): String {
        return String.format("%.0f cals", calories)
    }

    fun formatHeartRate(heartRate: Double): String {
        return String.format("%.0f bpm", heartRate)
    }

    fun formatDateTime(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("MMM dd yyyy, h:mm a", Locale.getDefault())
        return format.format(date)
    }

    fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return format.format(date)
    }

    fun formatTime(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("h:mm a", Locale.getDefault())
        return format.format(date)
    }

    fun getActivityTypeName(activityType: Int): String {
        return when (activityType) {
            ExerciseEntry.ACTIVITY_TYPE_RUNNING -> "Running"
            ExerciseEntry.ACTIVITY_TYPE_WALKING -> "Walking"
            ExerciseEntry.ACTIVITY_TYPE_STANDING -> "Standing"
            ExerciseEntry.ACTIVITY_TYPE_CYCLING -> "Cycling"
            ExerciseEntry.ACTIVITY_TYPE_HIKING -> "Hiking"
            ExerciseEntry.ACTIVITY_TYPE_DOWNHILL_SKIING -> "Downhill Skiing"
            ExerciseEntry.ACTIVITY_TYPE_CROSS_COUNTRY_SKIING -> "Cross-Country Skiing"
            ExerciseEntry.ACTIVITY_TYPE_SNOWBOARDING -> "Snowboarding"
            ExerciseEntry.ACTIVITY_TYPE_SKATING -> "Skating"
            ExerciseEntry.ACTIVITY_TYPE_SWIMMING -> "Swimming"
            ExerciseEntry.ACTIVITY_TYPE_MOUNTAIN_BIKING -> "Mountain Biking"
            ExerciseEntry.ACTIVITY_TYPE_WHEELCHAIR -> "Wheelchair"
            ExerciseEntry.ACTIVITY_TYPE_ELLIPTICAL -> "Elliptical"
            ExerciseEntry.ACTIVITY_TYPE_OTHER -> "Other"
            else -> "Unknown"
        }
    }

    fun getInputTypeName(inputType: Int): String {
        return when (inputType) {
            ExerciseEntry.INPUT_TYPE_MANUAL -> "Manual Entry"
            ExerciseEntry.INPUT_TYPE_GPS -> "GPS"
            ExerciseEntry.INPUT_TYPE_AUTOMATIC -> "Automatic"
            else -> "Unknown"
        }
    }

    fun getActivityTypeInt(activityTypeName: String): Int {
        return when (activityTypeName) {
            "Running" -> ExerciseEntry.ACTIVITY_TYPE_RUNNING
            "Walking" -> ExerciseEntry.ACTIVITY_TYPE_WALKING
            "Standing" -> ExerciseEntry.ACTIVITY_TYPE_STANDING
            "Cycling" -> ExerciseEntry.ACTIVITY_TYPE_CYCLING
            "Hiking" -> ExerciseEntry.ACTIVITY_TYPE_HIKING
            "Downhill Skiing" -> ExerciseEntry.ACTIVITY_TYPE_DOWNHILL_SKIING
            "Cross-Country Skiing" -> ExerciseEntry.ACTIVITY_TYPE_CROSS_COUNTRY_SKIING
            "Snowboarding" -> ExerciseEntry.ACTIVITY_TYPE_SNOWBOARDING
            "Skating" -> ExerciseEntry.ACTIVITY_TYPE_SKATING
            "Swimming" -> ExerciseEntry.ACTIVITY_TYPE_SWIMMING
            "Mountain Biking" -> ExerciseEntry.ACTIVITY_TYPE_MOUNTAIN_BIKING
            "Wheelchair" -> ExerciseEntry.ACTIVITY_TYPE_WHEELCHAIR
            "Elliptical" -> ExerciseEntry.ACTIVITY_TYPE_ELLIPTICAL
            "Other" -> ExerciseEntry.ACTIVITY_TYPE_OTHER
            else -> ExerciseEntry.ACTIVITY_TYPE_RUNNING
        }
    }

    fun getInputTypeInt(inputTypeName: String): Int {
        return when (inputTypeName) {
            "Manual Entry" -> ExerciseEntry.INPUT_TYPE_MANUAL
            "GPS" -> ExerciseEntry.INPUT_TYPE_GPS
            "Automatic" -> ExerciseEntry.INPUT_TYPE_AUTOMATIC
            else -> ExerciseEntry.INPUT_TYPE_MANUAL
        }
    }
}

