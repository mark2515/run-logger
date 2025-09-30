package moe.kunlonghe.myruns

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView

class FragmentSettings : Fragment(), MyDialog.UnitPreferenceDialogListener, MyDialog.CommentsDialogListener {
    
    private lateinit var commentsDescriptionTextView: TextView
    
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
        
        // Restore privacy setting from SharedPreferences
        val sharedPrefs = requireActivity().getSharedPreferences(MyDialog.SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        privacyCheckBox.isChecked = sharedPrefs.getBoolean(MyDialog.PRIVACY_KEY, false)
        
        privacySettingLayout.setOnClickListener {
            privacyCheckBox.isChecked = !privacyCheckBox.isChecked
            // Save privacy setting to SharedPreferences
            sharedPrefs.edit().putBoolean(MyDialog.PRIVACY_KEY, privacyCheckBox.isChecked).apply()
        }
        
        val unitPreferenceLayout = view.findViewById<LinearLayout>(R.id.unitPreferenceLayout)
        unitPreferenceLayout.setOnClickListener {
            // Show unit preference dialog
            showUnitPreferenceDialog()
        }
        
        val commentsLayout = view.findViewById<LinearLayout>(R.id.commentsLayout)
        commentsLayout.setOnClickListener {
            showCommentsDialog()
        }
        
        commentsDescriptionTextView = view.findViewById(R.id.commentsDescriptionTextView)
        
        // Webpage functionality
        val webpageLayout = view.findViewById<LinearLayout>(R.id.webpageLayout)
        webpageLayout.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.sfu.ca/computing.html"))
            startActivity(intent)
        }
        
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

    private fun showCommentsDialog() {
        val dialog = MyDialog()
        val bundle = Bundle()
        bundle.putInt(MyDialog.DIALOG_KEY, MyDialog.COMMENTS_DIALOG)
        dialog.arguments = bundle
        dialog.setCommentsDialogListener(this)
        dialog.show(parentFragmentManager, "CommentsDialog")
    }

    override fun onUnitSelected(isMetric: Boolean) {
        // Unit selection is saved in MyDialog
    }

    override fun onCommentsUpdated(comments: String) {
        // Comments dialog callback
    }
}