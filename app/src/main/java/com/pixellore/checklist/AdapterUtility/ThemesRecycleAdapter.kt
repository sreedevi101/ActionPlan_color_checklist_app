package com.pixellore.checklist.AdapterUtility

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.pixellore.checklist.DataClass.Theme
import com.pixellore.checklist.R

/*
* This adapter class is part of the feature "Theme Selection by User"
*
* Recycler View Adapter to display the list of Themes (from data class Themes) to the DialogFragment
* */
class ThemesRecycleAdapter(
    private val themesList: ArrayList<Theme>,
    private val listenerTheme: ThemeItemSelectListener
) :
    RecyclerView.Adapter<ThemesRecycleAdapter.ThemesViewHolder>() {


    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemesViewHolder {
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.theme_item, parent, false)
        return ThemesViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ThemesViewHolder, position: Int) {
        holder.itemPosition = position
        holder.bind()
    }

    override fun getItemCount(): Int {
        return themesList.size
    }


    //ViewHolder class for handling interactions with corresponding item
    inner class ThemesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val themeLayout = itemView.findViewById<ConstraintLayout>(R.id.theme_layout)
        val themeName = itemView.findViewById<TextView>(R.id.theme_name)
        val currentTheme = itemView.findViewById<TextView>(R.id.current_theme_indication)
        val selectionCheckMark = itemView.findViewById<ImageView>(R.id.selection_check_mark)
        var itemPosition: Int = 0



        // bind data to item views
        fun bind() {

            val themeAtPos = themesList[itemPosition]
            if (itemPosition == selectedPosition) {
                selectionCheckMark.visibility = View.VISIBLE
                //themeLayout.setSelected(true)
            } else {
                selectionCheckMark.visibility = View.GONE
                //themeLayout.setSelected(false)
            }


            val font = themeAtPos.font
            if (font != null){
                if (font.headingTextColorResId != null){
                    themeName.setTextColor(font.headingTextColorResId!!)
                    currentTheme.setTextColor(font.headingTextColorResId!!)
                }
            }


            if (themeAtPos.is_current_theme){
                currentTheme.visibility = View.VISIBLE
            } else{
                currentTheme.visibility = View.GONE
            }



            // when the item is clicked, pass the details to activity through the interface
            itemView.setOnClickListener {
                // manage to select only one item
                listenerTheme.itemClicked(themeAtPos, itemPosition)
                val prevSelectedPosition = selectedPosition
                selectedPosition = itemPosition
                notifyItemChanged(prevSelectedPosition)
                notifyItemChanged(selectedPosition)

                // All the themes in input data are updated with the primary color of the selected theme as text color
                // notifyDataSetChanged() to reload the recycler view with this new input data
                notifyDataSetChanged()
            }

            themeName.text = themeAtPos.theme_name

            // todo mark current theme in the list
        }



    }

    interface ThemeItemSelectListener {
        fun itemClicked(theme: Theme, position: Int)
    }

}