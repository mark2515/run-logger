package moe.kunlonghe.myruns

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.kunlonghe.myruns.database.*

class DisplayEntryActivity : AppCompatActivity() {
    private lateinit var tvInputType: TextView
    private lateinit var tvActivityType: TextView
    private lateinit var tvDateTime: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvCalories: TextView
    private lateinit var tvHeartRate: TextView
    private lateinit var tvComment: TextView
    
    private lateinit var exerciseViewModel: ExerciseViewModel
    private var entryId: Long = -1
    private var currentEntry: ExerciseEntry? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_entry)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Entry Details"
        
        // Get entry ID from intent
        entryId = intent.getLongExtra("ENTRY_ID", -1)
        
        if (entryId == -1L) {
            Toast.makeText(this, "Error: Invalid entry ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Initialize views
        initializeViews()
        
        // Setup database and ViewModel
        val database = ExerciseDatabase.getInstance(applicationContext)
        val databaseDao = database.exerciseEntryDao
        val repository = ExerciseRepository(databaseDao)
        val viewModelFactory = ExerciseViewModelFactory(repository)
        exerciseViewModel = ViewModelProvider(this, viewModelFactory).get(ExerciseViewModel::class.java)
        
        // Load entry data
        loadEntryData()
    }
    
    private fun initializeViews() {
        tvInputType = findViewById(R.id.tv_input_type)
        tvActivityType = findViewById(R.id.tv_activity_type)
        tvDateTime = findViewById(R.id.tv_date_time)
        tvDuration = findViewById(R.id.tv_duration)
        tvDistance = findViewById(R.id.tv_distance)
        tvCalories = findViewById(R.id.tv_calories)
        tvHeartRate = findViewById(R.id.tv_heart_rate)
        tvComment = findViewById(R.id.tv_comment)
    }
    
    private fun loadEntryData() {
        lifecycleScope.launch {
            val entry = withContext(Dispatchers.IO) {
                val database = ExerciseDatabase.getInstance(this@DisplayEntryActivity.applicationContext)
                val dao = database.exerciseEntryDao
                dao.getExerciseEntry(entryId)
            }
            
            if (entry != null) {
                currentEntry = entry
                displayEntry(entry)
            } else {
                Toast.makeText(
                    this@DisplayEntryActivity, 
                    "Error: Entry not found", 
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }
    
    private fun displayEntry(entry: ExerciseEntry) {
        tvInputType.text = UnitConverter.getInputTypeName(entry.inputType)
        tvActivityType.text = UnitConverter.getActivityTypeName(entry.activityType)
        tvDateTime.text = UnitConverter.formatDateTime(entry.dateTime)
        
        // Display duration
        tvDuration.text = if (entry.duration > 0) {
            UnitConverter.formatDurationInMinutes(entry.duration)
        } else {
            "N/A"
        }
        
        // Display distance
        tvDistance.text = if (entry.distance > 0) {
            UnitConverter.formatDistance(this, entry.distance)
        } else {
            "N/A"
        }
        
        // Display calories
        tvCalories.text = if (entry.calorie > 0) {
            UnitConverter.formatCalories(entry.calorie)
        } else {
            "N/A"
        }
        
        // Display heart rate
        tvHeartRate.text = if (entry.heartRate > 0) {
            UnitConverter.formatHeartRate(entry.heartRate)
        } else {
            "N/A"
        }
        
        // Display comment
        tvComment.text = if (entry.comment.isNotEmpty()) {
            entry.comment
        } else {
            "No comment"
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_display_entry, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_delete -> {
                deleteEntry()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun deleteEntry() {
        if (entryId != -1L) {
            exerciseViewModel.delete(entryId)
            Toast.makeText(this, "Entry deleted", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}

