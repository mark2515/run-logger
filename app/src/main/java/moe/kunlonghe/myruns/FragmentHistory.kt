package moe.kunlonghe.myruns

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Button
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import moe.kunlonghe.myruns.database.*

class FragmentHistory : Fragment() {
    
    private lateinit var listView: ListView
    private lateinit var adapter: MyRunsEntryAdapter
    private lateinit var myRunsViewModel: MyRunsViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        
        // Initialize ListView
        listView = view.findViewById(R.id.listview_history)
        view.findViewById<Button>(R.id.button_delete_all).setOnClickListener {
            myRunsViewModel.deleteAll()
        }
        
        // Initialize adapter with empty list
        adapter = MyRunsEntryAdapter(requireContext(), emptyList())
        listView.adapter = adapter
        
        // Setup item click listener
        listView.setOnItemClickListener { _, _, position, _ ->
            val entry = adapter.getItem(position)
            entry?.let {
                if ((it.inputType == MyRunsEntry.INPUT_TYPE_GPS || 
                     it.inputType == MyRunsEntry.INPUT_TYPE_AUTOMATIC) && 
                    it.locationList != null) {
                    val intent = Intent(requireContext(), MapActivity::class.java)
                    intent.putExtra("ENTRY_ID", it.id)
                    startActivity(intent)
                } else {
                    val intent = Intent(requireContext(), DisplayEntryActivity::class.java)
                    intent.putExtra("ENTRY_ID", it.id)
                    startActivity(intent)
                }
            }
        }
        
        return view
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Setup database and ViewModel
        val database = MyRunsDatabase.getInstance(requireActivity().applicationContext)
        val databaseDao = database.myRunsEntryDao
        val repository = MyRunsRepository(databaseDao)
        val viewModelFactory = MyRunsViewModelFactory(repository)
        myRunsViewModel = ViewModelProvider(
            requireActivity(), 
            viewModelFactory
        ).get(MyRunsViewModel::class.java)
        
        // Observe database changes
        myRunsViewModel.allMyRunsEntriesLiveData.observe(viewLifecycleOwner) { entries ->
            if (entries != null) {
                adapter.replace(entries)
                adapter.notifyDataSetChanged()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Trigger a refresh when fragment becomes visible
        myRunsViewModel.allMyRunsEntriesLiveData.value?.let { entries ->
            adapter.replace(entries)
            adapter.notifyDataSetChanged()
        }
    }
}