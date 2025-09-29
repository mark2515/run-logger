package moe.kunlonghe.myruns

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MapActivity : AppCompatActivity() {
    
    private lateinit var buttonSave: Button
    private lateinit var buttonCancel: Button
    private var inputType: String? = null
    private var activityType: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        
        // Get data from intent
        inputType = intent.getStringExtra("INPUT_TYPE")
        activityType = intent.getStringExtra("ACTIVITY_TYPE")
        
        // Initialize buttons
        buttonSave = findViewById(R.id.button_save)
        buttonCancel = findViewById(R.id.button_cancel)
        
        // Setup button listeners
        setupButtons()
        
        // Show toast with the selected options
        Toast.makeText(this, "Starting $activityType with $inputType", Toast.LENGTH_SHORT).show()
    }
    
    private fun setupButtons() {
        buttonSave.setOnClickListener {
            Toast.makeText(this, "Save functionality to be implemented", Toast.LENGTH_SHORT).show()
            finish()
        }
        
        buttonCancel.setOnClickListener {
            // Cancel and go back
            finish()
        }
    }
}
