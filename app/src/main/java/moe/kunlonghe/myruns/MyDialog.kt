package moe.kunlonghe.myruns

import androidx.appcompat.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import java.util.*

class MyDialog : DialogFragment(), View.OnClickListener {
    companion object {
        const val DIALOG_KEY = "dialog"
        const val TEST_DIALOG = 1
        const val PROFILE_PHOTO_DIALOG = 2
        const val UNIT_PREFERENCE_DIALOG = 3
        const val COMMENTS_DIALOG = 4
        const val DATE_PICKER_DIALOG = 5
        const val TIME_PICKER_DIALOG = 6
        const val NUMBER_INPUT_DIALOG = 7
        const val COMMENTS_DIALOG_NO_PERSIST = 8
        
        const val UNIT_PREFERENCE_KEY = "unit_preference"
        const val COMMENTS_KEY = "user_comments"
        const val PRIVACY_KEY = "privacy_setting"
        const val SHARED_PREFS_NAME = "MyRunsPrefs"
        const val DIALOG_TITLE_KEY = "dialog_title"
        const val DIALOG_UNIT_KEY = "dialog_unit"
    }

    interface ProfilePhotoDialogListener {
        fun onOpenCamera()
        fun onSelectFromGallery()
    }

    interface UnitPreferenceDialogListener {
        fun onUnitSelected(isMetric: Boolean)
    }

    interface CommentsDialogListener {
        fun onCommentsUpdated(comments: String)
    }

    interface DatePickerDialogListener {
        fun onDateSet(year: Int, month: Int, dayOfMonth: Int)
    }

    interface TimePickerDialogListener {
        fun onTimeSet(hourOfDay: Int, minute: Int)
    }

    interface NumberInputDialogListener {
        fun onNumberSet(value: String)
    }

    private var profilePhotoListener: ProfilePhotoDialogListener? = null
    private var unitPreferenceListener: UnitPreferenceDialogListener? = null
    private var commentsListener: CommentsDialogListener? = null
    private var datePickerListener: DatePickerDialogListener? = null
    private var timePickerListener: TimePickerDialogListener? = null
    private var numberInputListener: NumberInputDialogListener? = null

    fun setProfilePhotoDialogListener(listener: ProfilePhotoDialogListener) {
        profilePhotoListener = listener
    }

    fun setUnitPreferenceDialogListener(listener: UnitPreferenceDialogListener) {
        unitPreferenceListener = listener
    }

    fun setCommentsDialogListener(listener: CommentsDialogListener) {
        commentsListener = listener
    }

    fun setDatePickerDialogListener(listener: DatePickerDialogListener) {
        datePickerListener = listener
    }

    fun setTimePickerDialogListener(listener: TimePickerDialogListener) {
        timePickerListener = listener
    }

    fun setNumberInputDialogListener(listener: NumberInputDialogListener) {
        numberInputListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var ret: Dialog
        val bundle = arguments
        val dialogId = bundle?.getInt(DIALOG_KEY)
        
        when (dialogId) {
            TEST_DIALOG -> {
                val builder = AlertDialog.Builder(requireActivity())
                val view: View = requireActivity().layoutInflater.inflate(R.layout.fragment_my_dialog,
                    null)
                builder.setView(view)
                builder.setTitle("my title")
                builder.setPositiveButton("ok") { _, _ ->
                    // Handle ok click
                }
                builder.setNegativeButton("cancel") { _, _ ->
                    // Handle cancel click
                }
                ret = builder.create()
            }
            
            PROFILE_PHOTO_DIALOG -> {
                val builder = AlertDialog.Builder(requireActivity())
                val view: View = requireActivity().layoutInflater.inflate(R.layout.fragment_my_dialog,
                    null)
                
                val btnOpenCamera = view.findViewById<Button>(R.id.btnOpenCamera)
                val btnSelectGallery = view.findViewById<Button>(R.id.btnSelectGallery)
                
                btnOpenCamera.setOnClickListener(this)
                btnSelectGallery.setOnClickListener(this)
                
                builder.setView(view)
                builder.setTitle("Pick Profile Picture")
                ret = builder.create()
            }

            UNIT_PREFERENCE_DIALOG -> {
                val builder = AlertDialog.Builder(requireActivity())
                val view: View = requireActivity().layoutInflater.inflate(R.layout.fragment_unit_preference_dialog, null)
                
                val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroupUnits)
                val radioMetric = view.findViewById<RadioButton>(R.id.radioMetric)
                val radioImperial = view.findViewById<RadioButton>(R.id.radioImperial)
                
                val sharedPrefs = requireActivity().getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
                if (sharedPrefs.contains(UNIT_PREFERENCE_KEY)) {
                    val isMetric = sharedPrefs.getBoolean(UNIT_PREFERENCE_KEY, false)
                    // Set the saved selection
                    if (isMetric) {
                        radioMetric.isChecked = true
                    } else {
                        radioImperial.isChecked = true
                    }
                }
                
                radioGroup.setOnCheckedChangeListener { _, checkedId ->
                    when (checkedId) {
                        R.id.radioMetric -> {
                            sharedPrefs.edit().putBoolean(UNIT_PREFERENCE_KEY, true).apply()
                            unitPreferenceListener?.onUnitSelected(true)
                            dismiss()
                        }
                        R.id.radioImperial -> {
                            sharedPrefs.edit().putBoolean(UNIT_PREFERENCE_KEY, false).apply()
                            unitPreferenceListener?.onUnitSelected(false)
                            dismiss()
                        }
                    }
                }
                
                builder.setView(view)
                builder.setTitle("Unit Preference")
                builder.setNegativeButton("CANCEL") { _, _ ->
                    // Handle cancel click
                }
                ret = builder.create()
            }

            COMMENTS_DIALOG -> {
                val builder = AlertDialog.Builder(requireActivity())
                val view: View = requireActivity().layoutInflater.inflate(R.layout.fragment_comments_dialog, null)
                
                val editTextComments = view.findViewById<android.widget.EditText>(R.id.editTextComments)
                
                val sharedPrefs = requireActivity().getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
                val savedComments = sharedPrefs.getString(COMMENTS_KEY, "")
                editTextComments.setText(savedComments)
                
                builder.setView(view)
                builder.setTitle("Comments")
                builder.setPositiveButton("OK") { _, _ ->
                    val comments = editTextComments.text.toString()
                    sharedPrefs.edit().putString(COMMENTS_KEY, comments).apply()
                    commentsListener?.onCommentsUpdated(comments)
                }
                builder.setNegativeButton("CANCEL") { _, _ ->
                    // Handle cancel click
                }
                ret = builder.create()
            }

            COMMENTS_DIALOG_NO_PERSIST -> {
                val builder = AlertDialog.Builder(requireActivity())
                val view: View = requireActivity().layoutInflater.inflate(R.layout.fragment_comments_dialog, null)
                
                val editTextComments = view.findViewById<android.widget.EditText>(R.id.editTextComments)
                editTextComments.setText("")
                
                builder.setView(view)
                builder.setTitle("Comments")
                builder.setPositiveButton("OK") { _, _ ->
                    val comments = editTextComments.text.toString()
                    commentsListener?.onCommentsUpdated(comments)
                }
                builder.setNegativeButton("CANCEL") { _, _ ->
                }
                ret = builder.create()
            }

            DATE_PICKER_DIALOG -> {
                val calendar = Calendar.getInstance()
                val datePickerDialog = DatePickerDialog(
                    requireContext(),
                    { _, year, month, dayOfMonth ->
                        datePickerListener?.onDateSet(year, month, dayOfMonth)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                ret = datePickerDialog
            }

            TIME_PICKER_DIALOG -> {
                val calendar = Calendar.getInstance()
                val timePickerDialog = TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        timePickerListener?.onTimeSet(hourOfDay, minute)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                )
                ret = timePickerDialog
            }

            NUMBER_INPUT_DIALOG -> {
                val title = bundle?.getString(DIALOG_TITLE_KEY) ?: "Input"
                val unit = bundle?.getString(DIALOG_UNIT_KEY) ?: ""
                
                val editText = EditText(requireContext())
                editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                
                val builder = AlertDialog.Builder(requireContext())
                    .setTitle(title)
                    .setView(editText)
                    .setPositiveButton("OK") { _, _ ->
                        val value = editText.text.toString()
                        if (value.isNotEmpty()) {
                            numberInputListener?.onNumberSet(value)
                        }
                    }
                    .setNegativeButton("CANCEL", null)
                ret = builder.create()
            }
            
            else -> {
                ret = super.onCreateDialog(savedInstanceState)
            }
        }
        return ret
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnOpenCamera -> {
                profilePhotoListener?.onOpenCamera()
                dismiss()
            }
            R.id.btnSelectGallery -> {
                profilePhotoListener?.onSelectFromGallery()
                dismiss()
            }
        }
    }
}