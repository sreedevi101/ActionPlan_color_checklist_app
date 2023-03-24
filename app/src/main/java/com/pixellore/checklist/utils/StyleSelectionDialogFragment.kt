package com.pixellore.checklist.utils

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.pixellore.checklist.DataClass.CustomStyle
import com.pixellore.checklist.DataClass.TextFont
import com.pixellore.checklist.DataClass.Theme
import com.pixellore.checklist.R

class StyleSelectionDialogFragment(
    private val context: Context,
    private val onStyleSelectedListener: (colorOrFont: Any, styleSelection: Int) -> Unit
) : DialogFragment() {

    private lateinit var baseActivityListener: BaseActivityListener

    fun setBaseActivityListener(listener: BaseActivityListener) {
        this.baseActivityListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_style_selection_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val styleSelector = view.findViewById<RadioGroup>(R.id.style_selector)
        styleSelector.setOnCheckedChangeListener { radioGroup, optionId ->
            selectStyleForAllTasks(optionId)
            dismiss()
        }
    }


    private fun selectStyleForAllTasks(optionId: Int) {

        when(optionId){
            R.id.style_selector_background,
            R.id.style_selector_title_color,
            R.id.style_selector_details_color -> {

                // open color picker
                ColorPickerDialog
                    .Builder(context)                        // Pass Activity Instance
                    .setTitle("Choose Color")            // Default "Choose Color"
                    .setColorShape(ColorShape.SQAURE)   // Default ColorShape.CIRCLE
                    .setDefaultColor(android.R.color.white)     // Pass Default Color
                    .setColorListener { color, colorHex ->
                        // Handle Color Selection
                        //Log.v(Constants.TAG, "Color Selected: $color")
                        //Log.v(Constants.TAG, "Color Selected - Hex: $colorHex")

                        // return color
                        onStyleSelectedListener(color, optionId)

                    }.show()

            }
            R.id.style_selector_font -> {

                val fontsData = baseActivityListener.getFontsData()
                val fontPicker = FontPickerDialogFragment(fontsData
                ) { textFont, _ ->
                    // return color
                    onStyleSelectedListener(textFont, optionId)
                }
                fontPicker.show(parentFragmentManager, "font_picker")


            }
        }

    }

}