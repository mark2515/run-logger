package moe.kunlonghe.myruns

import androidx.appcompat.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.DialogFragment

class MyDialog : DialogFragment(), View.OnClickListener {
    companion object {
        const val DIALOG_KEY = "dialog"
        const val TEST_DIALOG = 1
        const val PROFILE_PHOTO_DIALOG = 2
    }

    interface ProfilePhotoDialogListener {
        fun onOpenCamera()
        fun onSelectFromGallery()
    }

    private var profilePhotoListener: ProfilePhotoDialogListener? = null

    fun setProfilePhotoDialogListener(listener: ProfilePhotoDialogListener) {
        profilePhotoListener = listener
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