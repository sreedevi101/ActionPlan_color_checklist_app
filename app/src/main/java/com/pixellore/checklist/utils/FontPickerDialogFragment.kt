package com.pixellore.checklist.utils

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pixellore.checklist.AdapterUtility.FontsRecycleAdapter
import com.pixellore.checklist.DataClass.TextFont
import com.pixellore.checklist.R


class FontPickerDialogFragment(
    private val fontsList: ArrayList<TextFont>,
    private val onFontSavedListener: (textFont: TextFont, textFontPosition: Int) -> Unit
    ) : DialogFragment() {

    private lateinit var fontsRecyclerViewList: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_font_picker_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // initialize this for current theme
        var textFontID: Int = -1
        var textFontName: String = ""
        var textFontSelected: TextFont = fontsList[0]
        var textFontSelectedPosition : Int = 0

        val cancelBtn = view.findViewById<Button>(R.id.cancel_button)
        val okBtn = view.findViewById<Button>(R.id.ok_button)

        // initialize and setup RecyclerView
        fontsRecyclerViewList = view.findViewById(R.id.fonts_recycler_view)
        fontsRecyclerViewList.setHasFixedSize(true)
        fontsRecyclerViewList.setItemViewCacheSize(20)
        fontsRecyclerViewList.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)

        // create OnFontSelectedListener to listen to click event on items from the RecyclerView Adapter
        val fontSelectedListener: FontsRecycleAdapter.OnFontSelectedListener =
            object : FontsRecycleAdapter.OnFontSelectedListener {
                override fun onFontSelected(textFont: TextFont, position: Int) {

                    textFontSelected = textFont
                    textFontSelectedPosition = position
                    textFontID = textFont.font_resource_id
                    textFontName = textFont.font_name

                    Log.v(Constants.TAG, "Font selected: $textFontName ($textFontID)")
                }

            }

        val adapter: FontsRecycleAdapter = FontsRecycleAdapter(
            fontsList,
            fontSelectedListener
        )
        fontsRecyclerViewList.adapter = adapter

        // Setup Buttons
        cancelBtn.setOnClickListener {
            dismiss()
        }

        okBtn.setOnClickListener {
            Log.v(Constants.TAG, "User selected font $textFontName")
            onFontSavedListener(textFontSelected, textFontSelectedPosition)
            dismiss()
        }

    }

}