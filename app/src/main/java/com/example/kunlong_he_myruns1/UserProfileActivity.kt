package com.example.kunlong_he_myruns1

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class UserProfileActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "UserProfileActivity"
        private const val PREFS_NAME = "UserProfilePrefs"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_PHONE = "phone"
        private const val KEY_GENDER = "gender"
        private const val KEY_CLASS = "class"
        private const val KEY_MAJOR = "major"
    }
    
    private lateinit var editTextName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPhone: EditText
    private lateinit var radioGroupGender: RadioGroup
    private lateinit var radioButtonFemale: RadioButton
    private lateinit var radioButtonMale: RadioButton
    private lateinit var editTextClass: EditText
    private lateinit var editTextMajor: EditText
    private lateinit var buttonSave: Button
    private lateinit var buttonCancel: Button
    
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate() called")
        setContentView(R.layout.activity_user_profile)
        
        initializeViews()
        initializeSharedPreferences()
        loadUserProfile()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        editTextName = findViewById(R.id.editTextName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPhone = findViewById(R.id.editTextPhone)
        radioGroupGender = findViewById(R.id.radioGroupGender)
        radioButtonFemale = findViewById(R.id.radioButtonFemale)
        radioButtonMale = findViewById(R.id.radioButtonMale)
        editTextClass = findViewById(R.id.editTextClass)
        editTextMajor = findViewById(R.id.editTextMajor)
        buttonSave = findViewById(R.id.buttonSave)
        buttonCancel = findViewById(R.id.buttonCancel)
    }
    
    private fun initializeSharedPreferences() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    private fun loadUserProfile() {        
        val savedName = sharedPreferences.getString(KEY_NAME, "")
        val savedEmail = sharedPreferences.getString(KEY_EMAIL, "")
        val savedPhone = sharedPreferences.getString(KEY_PHONE, "")
        val savedGender = sharedPreferences.getString(KEY_GENDER, "")
        val savedClass = sharedPreferences.getString(KEY_CLASS, "")
        val savedMajor = sharedPreferences.getString(KEY_MAJOR, "")
        
        editTextName.setText(savedName)
        editTextEmail.setText(savedEmail)
        editTextPhone.setText(savedPhone)
        editTextClass.setText(savedClass)
        editTextMajor.setText(savedMajor)
        
        when (savedGender) {
            "Female" -> radioButtonFemale.isChecked = true
            "Male" -> radioButtonMale.isChecked = true
        }
    }
    
    private fun setupClickListeners() {        
        buttonSave.setOnClickListener {
            // Log.d(TAG, "Save button clicked")
            saveUserProfile()
        }
        
        buttonCancel.setOnClickListener {
            // Log.d(TAG, "Cancel button clicked")
            finish()
        }
    }
    
    private fun saveUserProfile() {        
        val name = editTextName.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val phone = editTextPhone.text.toString().trim()
        val classYear = editTextClass.text.toString().trim()
        val major = editTextMajor.text.toString().trim()
        
        val selectedGenderId = radioGroupGender.checkedRadioButtonId
        val gender = when (selectedGenderId) {
            R.id.radioButtonFemale -> "Female"
            R.id.radioButtonMale -> "Male"
            else -> ""
        }
                
        val editor = sharedPreferences.edit()
        editor.putString(KEY_NAME, name)
        editor.putString(KEY_EMAIL, email)
        editor.putString(KEY_PHONE, phone)
        editor.putString(KEY_GENDER, gender)
        editor.putString(KEY_CLASS, classYear)
        editor.putString(KEY_MAJOR, major)
        editor.apply()
                
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        finish()
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
    }
    
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() called")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
    }
}
