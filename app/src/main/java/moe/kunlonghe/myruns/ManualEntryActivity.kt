package moe.kunlonghe.myruns

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
            finish()
        }
        
        buttonCancel.setOnClickListener {
            finish()
        }
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
    }
    
    override fun onDateSet(year: Int, month: Int, dayOfMonth: Int) {
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
    }
    
    override fun onTimeSet(hourOfDay: Int, minute: Int) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
    }
    
    override fun onNumberSet(value: String) {
        val currentDialog = supportFragmentManager.fragments.find { 
            it is MyDialog && it.isVisible 
        } as? MyDialog
        
        currentDialog?.let { dialog ->
            val tag = supportFragmentManager.fragments.find { it == dialog }?.tag
            when (tag) {
                DURATION_DIALOG_TAG -> {
                    textViewDuration.text = "$value mins"
                }
                DISTANCE_DIALOG_TAG -> {
                    textViewDistance.text = "$value miles"
                }
                CALORIES_DIALOG_TAG -> {
                    textViewCalories.text = "$value cals"
                }
                HEART_RATE_DIALOG_TAG -> {
                    textViewHeartRate.text = "$value bpm"
                }
            }
        }
    }
}