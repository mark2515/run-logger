package moe.kunlonghe.myruns

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner

class FragmentStart : Fragment() {
    
    private lateinit var spinnerInputType: Spinner
    private lateinit var spinnerActivityType: Spinner
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_start, container, false)
        
        // Initialize spinners
        spinnerInputType = view.findViewById(R.id.spinner_input_type)
        spinnerActivityType = view.findViewById(R.id.spinner_activity_type)
        
        // Setup Input Type spinner
        setupInputTypeSpinner()
        
        // Setup Activity Type spinner
        setupActivityTypeSpinner()
        
        return view
    }
    
    private fun setupInputTypeSpinner() {
        val inputTypes = arrayOf("Manual Entry", "GPS", "Automatic")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, inputTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerInputType.adapter = adapter
        
        spinnerInputType.setSelection(0)
    }
    
    private fun setupActivityTypeSpinner() {
        val activityTypes = arrayOf("Running", "Walking", "Standing", "Cycling", "Hiking", "Downhill Skiing", "Cross-Country Skiing", "Snowboarding", "Skating", "Swimming", "Mountain Biking", "Wheelchair", "Elliptical", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, activityTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerActivityType.adapter = adapter
        
        spinnerActivityType.setSelection(0)
    }
    
    fun getSelectedInputType(): String {
        return spinnerInputType.selectedItem.toString()
    }
    
    fun getSelectedActivityType(): String {
        return spinnerActivityType.selectedItem.toString()
    }
}