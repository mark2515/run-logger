package moe.kunlonghe.myruns

import androidx.appcompat.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment

class MyDialog : DialogFragment(), View.OnClickListener {
    companion object {
        const val DIALOG_KEY = "dialog"
        const val TEST_DIALOG = 1
        const val PROFILE_PHOTO_DIALOG = 2
        const val UNIT_PREFERENCE_DIALOG = 3
        const val UNIT_PREFERENCE_KEY = "unit_preference"
        const val SHARED_PREFS_NAME = "MyRunsPrefs"
    }

    interface ProfilePhotoDialogListener {
        fun onOpenCamera()
        fun onSelectFromGallery()
    }

    interface UnitPreferenceDialogListener {
        fun onUnitSelected(isMetric: Boolean)
    }

    private var profilePhotoListener: ProfilePhotoDialogListener? = null
    private var unitPreferenceListener: UnitPreferenceDialogListener? = null

    fun setProfilePhotoDialogListener(listener: ProfilePhotoDialogListener) {
        profilePhotoListener = listener
    }

    fun setUnitPreferenceDialogListener(listener: UnitPreferenceDialogListener) {
        unitPreferenceListener = listener
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
                
                // Load saved preference
                val sharedPrefs = requireActivity().getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
                val isMetric = sharedPrefs.getBoolean(UNIT_PREFERENCE_KEY, true)
                
                // Set the saved selection
                if (isMetric) {
                    radioMetric.isChecked = true
                } else {
                    radioImperial.isChecked = true
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