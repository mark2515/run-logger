package moe.kunlonghe.myruns

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import moe.kunlonghe.myruns.database.*
import java.util.*

class ManualEntryActivity : AppCompatActivity(), 
    MyDialog.CommentsDialogListener,
    MyDialog.DatePickerDialogListener,
    MyDialog.TimePickerDialogListener,
    MyDialog.NumberInputDialogListener {
    
    private lateinit var textViewDate: TextView
    private lateinit var textViewTime: TextView
    private lateinit var textViewDuration: TextView
    private lateinit var textViewDistance: TextView
    private lateinit var textViewCalories: TextView
    private lateinit var textViewHeartRate: TextView
    private lateinit var textViewComment: TextView
    private lateinit var buttonSave: Button
    private lateinit var buttonCancel: Button
    
    private var inputType: String? = null
    private var activityType: String? = null
    
    // Calendar instance for date picker
    private val calendar = Calendar.getInstance()
    
    // ViewModel for database operations
    private lateinit var exerciseViewModel: ExerciseViewModel
    
    // Store current values
    private var duration: Double = 0.0
    private var distance: Double = 0.0
    private var calories: Double = 0.0
    private var heartRate: Double = 0.0
    private var comment: String = ""
    
    companion object {
        private const val DATE_DIALOG_TAG = "date_dialog"
        private const val TIME_DIALOG_TAG = "time_dialog"
        private const val DURATION_DIALOG_TAG = "duration_dialog"
        private const val DISTANCE_DIALOG_TAG = "distance_dialog"
        private const val CALORIES_DIALOG_TAG = "calories_dialog"
        private const val HEART_RATE_DIALOG_TAG = "heart_rate_dialog"
        private const val COMMENTS_DIALOG_TAG = "comments_dialog"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_entry)
        
        // Get data from intent
        inputType = intent.getStringExtra("INPUT_TYPE")
        activityType = intent.getStringExtra("ACTIVITY_TYPE")
        
        // Initialize views
        initializeViews()
        
        // Setup buttons
        setupButtons()
        
        // Restore dialog listeners after configuration change
        restoreDialogListeners()
        
        // Setup database and ViewModel
        val database = ExerciseDatabase.getInstance(applicationContext)
        val databaseDao = database.exerciseEntryDao
        val repository = ExerciseRepository(databaseDao)
        val viewModelFactory = ExerciseViewModelFactory(repository)
        exerciseViewModel = ViewModelProvider(this, viewModelFactory).get(ExerciseViewModel::class.java)
        
        supportActionBar?.title = "Manual Entry"
    }
    
    private fun restoreDialogListeners() {
        // Restore listeners for any existing dialogs
        supportFragmentManager.findFragmentByTag(DATE_DIALOG_TAG)?.let { dialog ->
            (dialog as? MyDialog)?.setDatePickerDialogListener(this)
        }
        
        supportFragmentManager.findFragmentByTag(TIME_DIALOG_TAG)?.let { dialog ->
            (dialog as? MyDialog)?.setTimePickerDialogListener(this)
        }
        
        supportFragmentManager.findFragmentByTag(DURATION_DIALOG_TAG)?.let { dialog ->
            (dialog as? MyDialog)?.setNumberInputDialogListener(this)
        }
        
        supportFragmentManager.findFragmentByTag(DISTANCE_DIALOG_TAG)?.let { dialog ->
            (dialog as? MyDialog)?.setNumberInputDialogListener(this)
        }
        
        supportFragmentManager.findFragmentByTag(CALORIES_DIALOG_TAG)?.let { dialog ->
            (dialog as? MyDialog)?.setNumberInputDialogListener(this)
        }
        
        supportFragmentManager.findFragmentByTag(HEART_RATE_DIALOG_TAG)?.let { dialog ->
            (dialog as? MyDialog)?.setNumberInputDialogListener(this)
        }
        
        supportFragmentManager.findFragmentByTag(COMMENTS_DIALOG_TAG)?.let { dialog ->
            (dialog as? MyDialog)?.setCommentsDialogListener(this)
        }
    }
    
    private fun initializeViews() {
        textViewDate = findViewById(R.id.textview_date)
        textViewTime = findViewById(R.id.textview_time)
        textViewDuration = findViewById(R.id.textview_duration)
        textViewDistance = findViewById(R.id.textview_distance)
        textViewCalories = findViewById(R.id.textview_calories)
        textViewHeartRate = findViewById(R.id.textview_heart_rate)
        textViewComment = findViewById(R.id.textview_comment)
        buttonSave = findViewById(R.id.button_save)
        buttonCancel = findViewById(R.id.button_cancel)
        
        textViewDate.setOnClickListener {
            showDatePickerDialog()
        }
        
        textViewTime.setOnClickListener {
            showTimePickerDialog()
        }
        
        textViewDuration.setOnClickListener {
            showNumberInputDialog("Duration", "mins", DURATION_DIALOG_TAG)
        }
        
        textViewDistance.setOnClickListener {
            showNumberInputDialog("Distance", "miles", DISTANCE_DIALOG_TAG)
        }
        
        textViewCalories.setOnClickListener {
            showNumberInputDialog("Calories", "cals", CALORIES_DIALOG_TAG)
        }
        
        textViewHeartRate.setOnClickListener {
            showNumberInputDialog("Heart Rate", "bpm", HEART_RATE_DIALOG_TAG)
        }
        
        textViewComment.setOnClickListener {
            showCommentsDialog()
        }
    }
    
    private fun setupButtons() {
        buttonSave.setOnClickListener {
            saveExerciseEntry()
        }
        
        buttonCancel.setOnClickListener {
            finish()
        }
    }
    
    private fun saveExerciseEntry() {
        // Create exercise entry
        val entry = ExerciseEntry().apply {
            this.inputType = UnitConverter.getInputTypeInt(this@ManualEntryActivity.inputType ?: "Manual Entry")
            this.activityType = UnitConverter.getActivityTypeInt(this@ManualEntryActivity.activityType ?: "Running")
            this.dateTime = calendar.timeInMillis
            this.duration = this@ManualEntryActivity.duration * 60 // Convert minutes to seconds
            this.distance = this@ManualEntryActivity.distance // Already in miles
            this.calorie = this@ManualEntryActivity.calories
            this.heartRate = this@ManualEntryActivity.heartRate
            this.comment = this@ManualEntryActivity.comment
        }
        
        // Insert into database
        exerciseViewModel.insert(entry)
        
        Toast.makeText(this, "Exercise entry saved", Toast.LENGTH_SHORT).show()
        finish()
    }
    
    private fun showDatePickerDialog() {
        if (supportFragmentManager.findFragmentByTag(DATE_DIALOG_TAG) != null) {
            return
        }
        
        val dialog = MyDialog()
        val bundle = Bundle()
        bundle.putInt(MyDialog.DIALOG_KEY, MyDialog.DATE_PICKER_DIALOG)
        dialog.arguments = bundle
        dialog.setDatePickerDialogListener(this)
        dialog.show(supportFragmentManager, DATE_DIALOG_TAG)
    }
    
    private fun showTimePickerDialog() {
        if (supportFragmentManager.findFragmentByTag(TIME_DIALOG_TAG) != null) {
            return
        }
        
        val dialog = MyDialog()
        val bundle = Bundle()
        bundle.putInt(MyDialog.DIALOG_KEY, MyDialog.TIME_PICKER_DIALOG)
        dialog.arguments = bundle
        dialog.setTimePickerDialogListener(this)
        dialog.show(supportFragmentManager, TIME_DIALOG_TAG)
    }
    
    private fun showNumberInputDialog(title: String, unit: String, tag: String) {
        if (supportFragmentManager.findFragmentByTag(tag) != null) {
            return
        }
        
        val dialog = MyDialog()
        val bundle = Bundle()
        bundle.putInt(MyDialog.DIALOG_KEY, MyDialog.NUMBER_INPUT_DIALOG)
        bundle.putString(MyDialog.DIALOG_TITLE_KEY, title)
        bundle.putString(MyDialog.DIALOG_UNIT_KEY, unit)
        dialog.arguments = bundle
        dialog.setNumberInputDialogListener(this)
        dialog.show(supportFragmentManager, tag)
    }
    
    private fun showCommentsDialog() {
        if (supportFragmentManager.findFragmentByTag(COMMENTS_DIALOG_TAG) != null) {
            return
        }
        
        val dialog = MyDialog()
        val bundle = Bundle()
        bundle.putInt(MyDialog.DIALOG_KEY, MyDialog.COMMENTS_DIALOG_NO_PERSIST)
        dialog.arguments = bundle
        dialog.setCommentsDialogListener(this)
        dialog.show(supportFragmentManager, COMMENTS_DIALOG_TAG)
    }

    override fun onCommentsUpdated(comments: String) {
        comment = comments
        textViewComment.text = if (comments.isNotEmpty()) comments else ""
    }
    
    override fun onDateSet(year: Int, month: Int, dayOfMonth: Int) {
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        updateDateTimeDisplay()
    }
    
    override fun onTimeSet(hourOfDay: Int, minute: Int) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        updateDateTimeDisplay()
    }
    
    private fun updateDateTimeDisplay() {
        textViewDate.text = UnitConverter.formatDate(calendar.timeInMillis)
        textViewTime.text = UnitConverter.formatTime(calendar.timeInMillis)
    }
    
    override fun onNumberSet(value: String) {
        val currentDialog = supportFragmentManager.fragments.find { 
            it is MyDialog && it.isVisible 
        } as? MyDialog
        
        currentDialog?.let { dialog ->
            val tag = supportFragmentManager.fragments.find { it == dialog }?.tag
            when (tag) {
                DURATION_DIALOG_TAG -> {
                    duration = value.toDoubleOrNull() ?: 0.0
                    textViewDuration.text = "$value mins"
                }
                DISTANCE_DIALOG_TAG -> {
                    distance = value.toDoubleOrNull() ?: 0.0
                    textViewDistance.text = "$value miles"
                }
                CALORIES_DIALOG_TAG -> {
                    calories = value.toDoubleOrNull() ?: 0.0
                    textViewCalories.text = "$value cals"
                }
                HEART_RATE_DIALOG_TAG -> {
                    heartRate = value.toDoubleOrNull() ?: 0.0
                    textViewHeartRate.text = "$value bpm"
                }
            }
        }
    }
}