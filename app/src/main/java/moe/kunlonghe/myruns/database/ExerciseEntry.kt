package moe.kunlonghe.myruns.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "exercise_table")
data class ExerciseEntry(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "input_type")
    var inputType: Int = 0,

    @ColumnInfo(name = "activity_type")
    var activityType: Int = 0,

    @ColumnInfo(name = "date_time")
    var dateTime: Long = 0L,

    @ColumnInfo(name = "duration")
    var duration: Double = 0.0,

    @ColumnInfo(name = "distance")
    var distance: Double = 0.0,

    @ColumnInfo(name = "avg_pace")
    var avgPace: Double = 0.0,

    @ColumnInfo(name = "avg_speed")
    var avgSpeed: Double = 0.0,

    @ColumnInfo(name = "calorie")
    var calorie: Double = 0.0,

    @ColumnInfo(name = "climb")
    var climb: Double = 0.0,

    @ColumnInfo(name = "heart_rate")
    var heartRate: Double = 0.0,

    @ColumnInfo(name = "comment")
    var comment: String = "",

    @ColumnInfo(name = "location_list", typeAffinity = ColumnInfo.BLOB)
    var locationList: ByteArray? = null
) {
    companion object {
        // Input type constants
        const val INPUT_TYPE_MANUAL = 0
        const val INPUT_TYPE_GPS = 1
        const val INPUT_TYPE_AUTOMATIC = 2

        // Activity type constants
        const val ACTIVITY_TYPE_RUNNING = 0
        const val ACTIVITY_TYPE_WALKING = 1
        const val ACTIVITY_TYPE_STANDING = 2
        const val ACTIVITY_TYPE_CYCLING = 3
        const val ACTIVITY_TYPE_HIKING = 4
        const val ACTIVITY_TYPE_DOWNHILL_SKIING = 5
        const val ACTIVITY_TYPE_CROSS_COUNTRY_SKIING = 6
        const val ACTIVITY_TYPE_SNOWBOARDING = 7
        const val ACTIVITY_TYPE_SKATING = 8
        const val ACTIVITY_TYPE_SWIMMING = 9
        const val ACTIVITY_TYPE_MOUNTAIN_BIKING = 10
        const val ACTIVITY_TYPE_WHEELCHAIR = 11
        const val ACTIVITY_TYPE_ELLIPTICAL = 12
        const val ACTIVITY_TYPE_OTHER = 13
    }
}