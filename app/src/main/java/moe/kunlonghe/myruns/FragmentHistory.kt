package moe.kunlonghe.myruns

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import moe.kunlonghe.myruns.database.*

class FragmentHistory : Fragment() {
    
    private lateinit var listView: ListView
    private lateinit var adapter: ExerciseEntryAdapter
    private lateinit var exerciseViewModel: ExerciseViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        
        // Initialize ListView
        listView = view.findViewById(R.id.listview_history)
        
        // Initialize adapter with empty list
        adapter = ExerciseEntryAdapter(requireContext(), emptyList())
        listView.adapter = adapter
        
        // Setup item click listener
        listView.setOnItemClickListener { _, _, position, _ ->
            val entry = adapter.getItem(position)
            entry?.let {
                val intent = Intent(requireContext(), DisplayEntryActivity::class.java)
                intent.putExtra("ENTRY_ID", it.id)
                startActivity(intent)
            }
        }
        
        return view
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Setup database and ViewModel
        val database = ExerciseDatabase.getInstance(requireActivity().applicationContext)
        val databaseDao = database.exerciseEntryDao
        val repository = ExerciseRepository(databaseDao)
        val viewModelFactory = ExerciseViewModelFactory(repository)
        exerciseViewModel = ViewModelProvider(
            requireActivity(), 
            viewModelFactory
        ).get(ExerciseViewModel::class.java)
        
        // Observe database changes
        exerciseViewModel.allExerciseEntriesLiveData.observe(viewLifecycleOwner) { entries ->
            if (entries != null) {
                adapter.replace(entries)
                adapter.notifyDataSetChanged()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Trigger a refresh when fragment becomes visible
        exerciseViewModel.allExerciseEntriesLiveData.value?.let { entries ->
            adapter.replace(entries)
            adapter.notifyDataSetChanged()
        }
    }
}