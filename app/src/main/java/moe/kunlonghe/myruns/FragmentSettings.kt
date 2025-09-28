package moe.kunlonghe.myruns

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout

class FragmentSettings : Fragment() {
    
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
        
        return view
    }
}