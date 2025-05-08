package com.example.myworldapp2.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.myworldapp2.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AddTagDialog : DialogFragment() {
    
    interface TagDialogListener {
        fun onTagAdded(name: String, color: String)
    }
    
    private var listener: TagDialogListener? = null
    
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            val fragment = parentFragment
            if (fragment is TagDialogListener) {
                listener = fragment
            }
        } catch (e: ClassCastException) {
            throw ClassCastException("Parent fragment must implement TagDialogListener")
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_add_tag, null)
        
        val tagNameEditText = view.findViewById<TextInputEditText>(R.id.tagNameEditText)
        val tagNameLayout = view.findViewById<TextInputLayout>(R.id.tagNameLayout)
        val colorRadioGroup = view.findViewById<RadioGroup>(R.id.colorRadioGroup)
        val addButton = view.findViewById<Button>(R.id.addButton)
        val cancelButton = view.findViewById<Button>(R.id.cancelButton)
        
        val dialog = builder.setView(view).create()
        
        addButton.setOnClickListener {
            val tagName = tagNameEditText.text.toString().trim()
            
            if (tagName.isEmpty()) {
                tagNameLayout.error = "Введите название тега"
                return@setOnClickListener
            }
            
            // Получаем выбранный цвет
            val selectedRadioButtonId = colorRadioGroup.checkedRadioButtonId
            val color = when (selectedRadioButtonId) {
                R.id.colorGreen -> "#4CAF50"
                R.id.colorBlue -> "#2196F3"
                R.id.colorRed -> "#F44336"
                R.id.colorOrange -> "#FF9800"
                R.id.colorPurple -> "#9C27B0"
                else -> "#4CAF50" // По умолчанию зеленый
            }
            
            listener?.onTagAdded(tagName, color)
            dialog.dismiss()
        }
        
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        
        return dialog
    }
    
    companion object {
        fun newInstance(): AddTagDialog {
            return AddTagDialog()
        }
    }
} 