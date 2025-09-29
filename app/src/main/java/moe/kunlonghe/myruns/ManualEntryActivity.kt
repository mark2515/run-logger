package moe.kunlonghe.myruns

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ManualEntryActivity : AppCompatActivity() {
    
    private lateinit var editTextDate: EditText
    private lateinit var editTextTime: EditText
    private lateinit var editTextDuration: EditText
    private lateinit var editTextDistance: EditText
    private lateinit var editTextCalories: EditText
    private lateinit var editTextHeartRate: EditText
    private lateinit var editTextComment: EditText
    private lateinit var buttonSave: Button
    private lateinit var buttonCancel: Button
    
    private var inputType: String? = null
    private var activityType: String? = null
    
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
        editTextDate = findViewById(R.id.edittext_date)
        editTextTime = findViewById(R.id.edittext_time)
        editTextDuration = findViewById(R.id.edittext_duration)
        editTextDistance = findViewById(R.id.edittext_distance)
        editTextCalories = findViewById(R.id.edittext_calories)
        editTextHeartRate = findViewById(R.id.edittext_heart_rate)
        editTextComment = findViewById(R.id.edittext_comment)
        buttonSave = findViewById(R.id.button_save)
        buttonCancel = findViewById(R.id.button_cancel)
    }
    
    private fun setupButtons() {
        buttonSave.setOnClickListener {
            if (validateInput()) {
                saveEntry()
                finish()
            }
        }
        
        buttonCancel.setOnClickListener {
            finish()
        }
    }
    
    private fun validateInput(): Boolean {
        if (editTextDuration.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter duration", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (editTextDistance.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter distance", Toast.LENGTH_SHORT).show()
            return false
        }
        
        return true
    }
    
    private fun saveEntry() {
        val date = editTextDate.text.toString()
        val time = editTextTime.text.toString()
        val duration = editTextDuration.text.toString()
        val distance = editTextDistance.text.toString()
        val calories = editTextCalories.text.toString()
        val heartRate = editTextHeartRate.text.toString()
        val comment = editTextComment.text.toString()
        
        Toast.makeText(this, "Entry saved successfully!", Toast.LENGTH_SHORT).show()
    }
}