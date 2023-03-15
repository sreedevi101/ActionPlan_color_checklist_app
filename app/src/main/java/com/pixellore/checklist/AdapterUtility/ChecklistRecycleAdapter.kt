package com.pixellore.checklist.AdapterUtility

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.pixellore.checklist.DataClass.CustomStyle
import com.pixellore.checklist.DatabaseUtility.Checklist
import com.pixellore.checklist.R
import com.pixellore.checklist.utils.Constants

/**
 * Adapter for the recycler view in Main Activity
 * to display the checklists in database
 * */
class ChecklistRecycleAdapter(private val clickListenerChecklist: (position: Int, checklist:Checklist, actionRequested: Int) -> Unit) :
    ListAdapter<Checklist, ChecklistRecycleAdapter.ChecklistRecycleViewHolder>(ChecklistItemComparator()) {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChecklistRecycleViewHolder {
        return ChecklistRecycleViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ChecklistRecycleViewHolder, position: Int) {
        val currentChecklist = getItem(position)
        holder.bind(currentChecklist, holder.itemView.context, clickListenerChecklist)
    }

    class ChecklistRecycleViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){


        fun bind(currentChecklist: Checklist, context: Context,
                 clickListenerChecklist: (position: Int, checklist: Checklist, actionRequested: Int) -> Unit,)
        {

            with(itemView){
                val checklistLayout: ConstraintLayout = findViewById(R.id.checklistCardLayout)
                val checklistTitleView: TextView = findViewById(R.id.checklist_title)
                val checklistPinnedButton: ImageButton = findViewById(R.id.pin_button)
                val checklistMoreButton: ImageButton = findViewById(R.id.more_button)

                val moreEditMenu = PopupMenu(context, checklistMoreButton)
                val menu = moreEditMenu.menu
                moreEditMenu.menuInflater.inflate(R.menu.checklist_item_popup_menu, menu)


                moreEditMenu.setOnMenuItemClickListener {
                    when(it.itemId){
                        R.id.popup_change_background ->
                        {
                            ColorPickerDialog
                                .Builder(context)        				// Pass Activity Instance
                                .setTitle("Choose Color")           	// Default "Choose Color"
                                .setColorShape(ColorShape.SQAURE)   // Default ColorShape.CIRCLE
                                .setDefaultColor(android.R.color.white)     // Pass Default Color
                                .setColorListener { color, colorHex ->
                                    // Handle Color Selection
                                    //Log.v(Constants.TAG, "Color Selected: $color")

                                    // change background color of checklist item
                                    checklistLayout.setBackgroundColor(color)

                                    // modify Checklist item to save in the database
                                    if (currentChecklist.font != null){
                                        currentChecklist.font?.backgroundColorResId = color
                                    } else{
                                        val font = CustomStyle(backgroundColorResId = color)
                                        currentChecklist.font = font
                                    }

                                    // update in database
                                    clickListenerChecklist(adapterPosition, currentChecklist, Constants.UPDATE_DB)

                                }.show()

                            return@setOnMenuItemClickListener true
                        }
                        R.id.popup_change_title_color ->
                        {
                            ColorPickerDialog
                                .Builder(context)        				// Pass Activity Instance
                                .setTitle("Choose Color")           	// Default "Choose Color"
                                .setColorShape(ColorShape.SQAURE)   // Default ColorShape.CIRCLE
                                .setDefaultColor(android.R.color.white)     // Pass Default Color
                                .setColorListener { color, colorHex ->
                                    // Handle Color Selection
                                    //Log.v(Constants.TAG, "Color Selected: $color")

                                    // change text color
                                    checklistTitleView.setTextColor(color)

                                    // modify Checklist item to save in the database
                                    if (currentChecklist.font != null){
                                        currentChecklist.font?.headingTextColorResId = color
                                    } else{
                                        val font = CustomStyle(headingTextColorResId = color)
                                        currentChecklist.font = font
                                    }

                                    // update in database
                                    clickListenerChecklist(adapterPosition, currentChecklist, Constants.UPDATE_DB)

                                }.show()

                            return@setOnMenuItemClickListener true
                        }
                        R.id.popup_delete_checklist_item->{

                            // todo show alert dialog and get confirmation from user

                            // update in database
                            clickListenerChecklist(adapterPosition, currentChecklist, Constants.DELETE)

                            return@setOnMenuItemClickListener true
                        }
                        else -> {
                            return@setOnMenuItemClickListener true
                        }
                    }
                }

                checklistMoreButton.setOnClickListener {
                    moreEditMenu.show()
                }

                checklistTitleView.text = currentChecklist.checklist_title

                // Pinned or not pinned - Based on the status of the boolean, apply the icon
                if (currentChecklist.isPinned){
                    checklistPinnedButton.setImageResource(R.drawable.ic_baseline_push_pin_24)
                } else {
                    checklistPinnedButton.setImageResource(R.drawable.ic_outline_push_pin_not_pinned_24)
                }

                // set background color
                currentChecklist.font?.backgroundColorResId?.let {
                    checklistLayout.setBackgroundColor(it)
                }

                // set title text color
                currentChecklist.font?.headingTextColorResId?.let {
                    checklistTitleView.setTextColor(it)
                }


                checklistLayout.setOnClickListener {
                    clickListenerChecklist(adapterPosition, currentChecklist, Constants.OPEN_EDITOR)
                }

                // todo move pinned to another recycler view
                checklistPinnedButton.setOnClickListener {
                    if (currentChecklist.isPinned){
                        // if pinned, unpin
                        currentChecklist.isPinned = false
                        checklistPinnedButton.setImageResource(R.drawable.ic_outline_push_pin_not_pinned_24)
                    } else {
                        // if not pinned, pin
                        currentChecklist.isPinned = true
                        checklistPinnedButton.setImageResource(R.drawable.ic_baseline_push_pin_24)
                    }

                    // update database
                    clickListenerChecklist(adapterPosition, currentChecklist, Constants.UPDATE_DB)
                }
            }

        }



        companion object {
            fun create(parent: ViewGroup): ChecklistRecycleViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.checklist_item, parent, false)
                return ChecklistRecycleViewHolder((view))
            }
        }
    }

    class ChecklistItemComparator : DiffUtil.ItemCallback<Checklist>(){
        override fun areItemsTheSame(oldItem: Checklist, newItem: Checklist): Boolean {
            return oldItem.checklist_id == newItem.checklist_id
        }

        override fun areContentsTheSame(oldItem: Checklist, newItem: Checklist): Boolean {
            return oldItem == newItem
        }

    }
}