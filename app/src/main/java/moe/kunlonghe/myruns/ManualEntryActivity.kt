package moe.kunlonghe.myruns

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class ManualEntryActivity : AppCompatActivity() {
    
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
        
        supportActionBar?.title = "Manual Entry"
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
        
        // Set current date as default
        updateDateDisplay()
    }
    
    private fun setupButtons() {
        buttonSave.setOnClickListener {
            finish()
        }
        
        buttonCancel.setOnClickListener {
            finish()
        }
    }
    
    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        datePickerDialog.show()
    }
    
    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        textViewDate.text = dateFormat.format(calendar.time)
    }
}