package com.pixellore.checklist

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pixellore.checklist.AdapterUtility.ThemesRecycleAdapter
import com.pixellore.checklist.DataClass.Theme
import com.pixellore.checklist.utils.Constants
import java.util.*

class ThemePickerDialogFragment : DialogFragment() {

    // RecyclerView for listing age groups
    private lateinit var  themesRecyclerViewList: RecyclerView

    // dialog view is created
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Objects.requireNonNull(dialog)?.window!!.requestFeature(Window.FEATURE_NO_TITLE)

        // Show status bar
        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        return inflater.inflate(R.layout.fragment_theme_picker_dialog,null,false)
    }

    //dialog view is ready
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // todo initialize this for current theme
        var themeSelectedIndex: Int = -1
        var themeSelectedName:String = ""

        // initialize and setup RecyclerView
        themesRecyclerViewList = view.findViewById(R.id.themes_recycler_view)
        themesRecyclerViewList.setHasFixedSize(true)
        themesRecyclerViewList.setItemViewCacheSize(20)
        themesRecyclerViewList.layoutManager  = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)

        // create Arraylist of Themes data class to be displayed in RecyclerView
        val themesList :ArrayList<Theme> = ArrayList()

        // Theme 1
        themesList.add(
            Theme("Professional",resources.getInteger(R.integer.app_theme_set1),
                is_current_theme = true
            )
        )
        // Theme 2
        themesList.add(
            Theme("Shrine pink",resources.getInteger(R.integer.app_theme_set2),
                is_current_theme = false
            )
        )
        // Theme 3
        themesList.add(
            Theme("Blue Orange",resources.getInteger(R.integer.app_theme_set3),
                is_current_theme = false
            )
        )
        // Theme 4
        themesList.add(
            Theme("Classy",resources.getInteger(R.integer.app_theme_set4),
                is_current_theme = false
            )
        )
        // Theme 5
        themesList.add(
            Theme("Blue Teal",resources.getInteger(R.integer.app_theme_set5),
                is_current_theme = false
            )
        )
        // Theme 6
        themesList.add(
            Theme("Green Orange",resources.getInteger(R.integer.app_theme_set6),
                is_current_theme = false
            )
        )

        // create ThemeItemSelectListener to listen to click event on items from the RecyclerView Adapter
        val listenerTheme: ThemesRecycleAdapter.ThemeItemSelectListener = object: ThemesRecycleAdapter.ThemeItemSelectListener{
            override fun itemClicked(theme: Theme, position: Int) {
                // Todo set theme
                Log.v(Constants.TAG, "Theme: " + theme.theme_name  + theme.theme_index_num)
                // Save the selected theme index number
                themeSelectedIndex = theme.theme_index_num
                themeSelectedName = theme.theme_name
            }
        }



        // create AgeGroupAdapter and pass as parameters the agelist and the AgeItemSelectListener
        val  adapter: ThemesRecycleAdapter = ThemesRecycleAdapter(themesList, listenerTheme)
        themesRecyclerViewList.adapter = adapter



        // Setup Buttons
        val cancelBtn = view.findViewById<Button>(R.id.cancel_button)
        val okBtn = view.findViewById<Button>(R.id.ok_button)

        cancelBtn.setOnClickListener {
            dismiss()
        }

        okBtn.setOnClickListener {
            // set theme
            Log.v(Constants.TAG, "User selected theme " + themeSelectedName + " Apply changes")

            dismiss()
        }

    }
}