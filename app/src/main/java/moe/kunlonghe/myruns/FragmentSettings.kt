package moe.kunlonghe.myruns

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView

class FragmentSettings : Fragment(), MyDialog.UnitPreferenceDialogListener {
    
    private lateinit var unitDescriptionTextView: TextView
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        
        val userProfileLayout = view.findViewById<LinearLayout>(R.id.userProfileLayout)
        userProfileLayout.setOnClickListener {
            val intent = Intent(requireContext(), UserProfileActivity::class.java)
            startActivity(intent)
        }
        
        val privacySettingLayout = view.findViewById<LinearLayout>(R.id.privacySettingLayout)
        val privacyCheckBox = view.findViewById<CheckBox>(R.id.privacyCheckBox)
        
        privacySettingLayout.setOnClickListener {
            privacyCheckBox.isChecked = !privacyCheckBox.isChecked
        }
        
        val unitPreferenceLayout = view.findViewById<LinearLayout>(R.id.unitPreferenceLayout)
        unitPreferenceLayout.setOnClickListener {
            // Show unit preference dialog
            showUnitPreferenceDialog()
        }
        
        unitDescriptionTextView = view.findViewById(R.id.unitDescriptionTextView)
        
        updateUnitPreferenceDisplay()
        
        return view
    }

    private fun showUnitPreferenceDialog() {
        val dialog = MyDialog()
        val bundle = Bundle()
        bundle.putInt(MyDialog.DIALOG_KEY, MyDialog.UNIT_PREFERENCE_DIALOG)
        dialog.arguments = bundle
        dialog.setUnitPreferenceDialogListener(this)
        dialog.show(parentFragmentManager, "UnitPreferenceDialog")
    }

    override fun onUnitSelected(isMetric: Boolean) {
        updateUnitPreferenceDisplay()
    }
    
    private fun updateUnitPreferenceDisplay() {
        val sharedPrefs = requireActivity().getSharedPreferences(MyDialog.SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val isMetric = sharedPrefs.getBoolean(MyDialog.UNIT_PREFERENCE_KEY, true)
        
        val unitText = if (isMetric) "Metric (Kilometers)" else "Imperial (Miles)"
        unitDescriptionTextView.text = unitText
    }
}