package moe.kunlonghe.myruns

import android.os.Bundle
import android.widget.Button
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
    }
    
    private fun setupButtons() {
        buttonSave.setOnClickListener {
            finish()
        }
        
        buttonCancel.setOnClickListener {
            // Cancel and go back
            finish()
        }
    }
}
