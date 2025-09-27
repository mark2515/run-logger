package moe.kunlonghe.myruns

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import java.io.File
import java.io.FileOutputStream

class UserProfileActivity : AppCompatActivity(), MyDialog.ProfilePhotoDialogListener {
    
    companion object {
        private const val TAG = "UserProfileActivity"
        private const val PREFS_NAME = "UserProfilePrefs"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_PHONE = "phone"
        private const val KEY_GENDER = "gender"
        private const val KEY_CLASS = "class"
        private const val KEY_MAJOR = "major"
        private const val KEY_HAS_AVATAR = "has_avatar"
        private const val BUNDLE_TEMP_IMG_URI = "temp_img_uri"
        private const val BUNDLE_HAS_UNSAVED_IMAGE = "has_unsaved_image"
        private const val BUNDLE_TEMP_IMAGE_PATH = "temp_image_path"
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
    private lateinit var imageView: ImageView
    private lateinit var btnChangePhoto: Button
    
    private lateinit var sharedPreferences: SharedPreferences
    private var tempImgUri: Uri? = null
    private lateinit var myViewModel: MyViewModel
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var galleryResult: ActivityResultLauncher<Intent>
    
    private val tempImgFileName = "xd_temp_img.jpg"
    private val savedImgFileName = "user_avatar.jpg"
    private val stateImgFileName = "state_temp_img.jpg"
    
    private var originalAvatar: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate() called")
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_profile)
        
        setupWindowInsets()
        
        initializeViews()
        initializeSharedPreferences()
        loadUserProfile()
        setupClickListeners()
        setupCamera()
        
        // Restore state if available
        savedInstanceState?.let {
            val uriString = it.getString(BUNDLE_TEMP_IMG_URI)
            if (uriString != null) {
                tempImgUri = Uri.parse(uriString)
            }
            
            val hasUnsavedImage = it.getBoolean(BUNDLE_HAS_UNSAVED_IMAGE, false)
            val tempImagePath = it.getString(BUNDLE_TEMP_IMAGE_PATH)
            
            if (hasUnsavedImage && tempImagePath != null) {
                myViewModel.hasUnsavedImage = hasUnsavedImage
                myViewModel.tempImagePath = tempImagePath
                restoreUnsavedImage(tempImagePath)
            }
        }
        
        myViewModel.userImage.observe(this) { bitmap ->
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            } else {
                imageView.setImageResource(R.drawable.default_avatar)
            }
        }
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        
        // Save URI
        tempImgUri?.let {
            outState.putString(BUNDLE_TEMP_IMG_URI, it.toString())
        }
        
        // Save unsaved image state
        outState.putBoolean(BUNDLE_HAS_UNSAVED_IMAGE, myViewModel.hasUnsavedImage)
        
        if (myViewModel.hasUnsavedImage && myViewModel.userImage.value != null) {
            val stateImagePath = saveImageToStateFile(myViewModel.userImage.value!!)
            if (stateImagePath != null) {
                outState.putString(BUNDLE_TEMP_IMAGE_PATH, stateImagePath)
                myViewModel.tempImagePath = stateImagePath
            }
        }
    }
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, "onConfigurationChanged() called")
    }
    
    private fun setupWindowInsets() {
        val rootView = findViewById<ScrollView>(R.id.scrollView)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                top = systemBars.top,
                bottom = systemBars.bottom
            )
            insets
        }
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
        imageView = findViewById(R.id.imageProfile)
        btnChangePhoto = findViewById(R.id.btnChangePhoto)
    }
    
    private fun setupCamera() {
        Util.requestAllPermissions(this)

        if (tempImgUri == null) {
            val tempImgFile = File(getExternalFilesDir(null), tempImgFileName)
            tempImgUri = FileProvider.getUriForFile(this, "moe.kunlonghe.myruns.fileprovider", tempImgFile)
        }

        // Camera result launcher
        cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                tempImgUri?.let { uri ->
                    try {
                        val bitmap = Util.getBitmap(this, uri)
                        myViewModel.userImage.value = bitmap
                        myViewModel.hasUnsavedImage = true
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing camera result", e)
                        Toast.makeText(this, "Error processing photo", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Gallery result launcher
        galleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    try {
                        val bitmap = Util.getBitmap(this, uri)
                        myViewModel.userImage.value = bitmap
                        myViewModel.hasUnsavedImage = true
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing gallery result", e)
                        Toast.makeText(this, "Error processing photo", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        myViewModel = ViewModelProvider(this)[MyViewModel::class.java]
        
        // Load saved avatar if exists
        loadSavedAvatar()
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
            saveUserProfile()
        }
        
        buttonCancel.setOnClickListener {
            restoreOriginalAvatar()
            cleanupTempFiles()
            finish()
        }
        
        btnChangePhoto.setOnClickListener {
            showProfilePhotoDialog()
        }
    }
    
    private fun showProfilePhotoDialog() {
        val dialog = MyDialog()
        val bundle = Bundle()
        bundle.putInt(MyDialog.DIALOG_KEY, MyDialog.PROFILE_PHOTO_DIALOG)
        dialog.arguments = bundle
        dialog.setProfilePhotoDialogListener(this)
        dialog.show(supportFragmentManager, "ProfilePhotoDialog")
    }
    
    override fun onOpenCamera() {
        tempImgUri?.let { uri ->
            val tempImgFile = File(getExternalFilesDir(null), tempImgFileName)
            if (!tempImgFile.exists()) {
                try {
                    tempImgFile.createNewFile()
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating temp file", e)
                    Toast.makeText(this, "Error preparing camera", Toast.LENGTH_SHORT).show()
                    return
                }
            }
            
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            cameraResult.launch(intent)
        }
    }
    
    override fun onSelectFromGallery() {
        if (!Util.hasMediaPermission(this)) {
            Toast.makeText(this, "Storage permission denied. Cannot access gallery.", Toast.LENGTH_LONG).show()
            return
        }
        
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        galleryResult.launch(intent)
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
        
        saveAvatarPermanently()
                
        val editor = sharedPreferences.edit()
        editor.putString(KEY_NAME, name)
        editor.putString(KEY_EMAIL, email)
        editor.putString(KEY_PHONE, phone)
        editor.putString(KEY_GENDER, gender)
        editor.putString(KEY_CLASS, classYear)
        editor.putString(KEY_MAJOR, major)
        editor.apply()
        
        myViewModel.hasUnsavedImage = false
        cleanupTempFiles()
                
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        finish()
    }
    
    private fun saveAvatarPermanently() {
        val currentAvatar = myViewModel.userImage.value
        if (currentAvatar != null) {
            try {
                val savedImgFile = File(getExternalFilesDir(null), savedImgFileName)
                val fileOutputStream = FileOutputStream(savedImgFile)
                currentAvatar.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
                
                val editor = sharedPreferences.edit()
                editor.putBoolean(KEY_HAS_AVATAR, true)
                editor.apply()
            } catch (e: Exception) {
                Log.e(TAG, "Error saving avatar", e)
                Toast.makeText(this, "Error saving avatar", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun restoreOriginalAvatar() {
        if (originalAvatar != null) {
            myViewModel.userImage.value = originalAvatar
        } else {
            myViewModel.userImage.value = null
            imageView.setImageResource(R.drawable.default_avatar)
        }
        myViewModel.hasUnsavedImage = false
    }
    
    private fun loadSavedAvatar() {
        val hasAvatar = sharedPreferences.getBoolean(KEY_HAS_AVATAR, false)
        if (hasAvatar) {
            try {
                val savedImgFile = File(getExternalFilesDir(null), savedImgFileName)
                if (savedImgFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(savedImgFile.absolutePath)
                    originalAvatar = bitmap
                    myViewModel.userImage.value = bitmap
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading saved avatar", e)
            }
        }
    }
    
    private fun saveImageToStateFile(bitmap: Bitmap): String? {
        return try {
            val stateImgFile = File(getExternalFilesDir(null), stateImgFileName)
            val fileOutputStream = FileOutputStream(stateImgFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
            stateImgFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error saving state image", e)
            null
        }
    }
    
    private fun restoreUnsavedImage(imagePath: String) {
        try {
            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imagePath)
                if (bitmap != null) {
                    myViewModel.userImage.value = bitmap
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring unsaved image", e)
        }
    }
    
    private fun cleanupTempFiles() {
        try {
            // Clean up camera temp file
            val tempImgFile = File(getExternalFilesDir(null), tempImgFileName)
            if (tempImgFile.exists()) {
                tempImgFile.delete()
            }
            
            // Clean up state temp file
            val stateImgFile = File(getExternalFilesDir(null), stateImgFileName)
            if (stateImgFile.exists()) {
                stateImgFile.delete()
            }
            
            myViewModel.tempImagePath = null
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up temp files", e)
        }
    }
    
    override fun onBackPressed() {
        restoreOriginalAvatar()
        cleanupTempFiles()
        super.onBackPressed()
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
        
        if (!myViewModel.hasUnsavedImage || isFinishing) {
            cleanupTempFiles()
        }
    }
}
