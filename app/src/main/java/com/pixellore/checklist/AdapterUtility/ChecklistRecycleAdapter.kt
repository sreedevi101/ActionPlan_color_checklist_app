package com.pixellore.checklist.AdapterUtility

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pixellore.checklist.DatabaseUtility.Checklist
import com.pixellore.checklist.DatabaseUtility.TaskWithSubtasks
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
        holder.bind(currentChecklist, clickListenerChecklist)
    }

    class ChecklistRecycleViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){


        fun bind(currentChecklist: Checklist,
                 clickListenerChecklist: (position: Int, checklist: Checklist, actionRequested: Int) -> Unit,)
        {

            with(itemView){
                val checklistLayout: ConstraintLayout = findViewById(R.id.checklistCardLayout)
                val checklistTitleView: TextView = findViewById(R.id.checklist_title)
                val checklistPinnedButton: ImageButton = findViewById(R.id.pin_button)

                checklistTitleView.text = currentChecklist.checklist_title

                // Pinned or not pinned - Based on the status of the boolean, apply the icon
                if (currentChecklist.isPinned){
                    checklistPinnedButton.setImageResource(R.drawable.ic_baseline_push_pin_24)
                } else {
                    checklistPinnedButton.setImageResource(R.drawable.ic_outline_push_pin_not_pinned_24)
                }

                checklistLayout.setOnClickListener {
                    clickListenerChecklist(adapterPosition, currentChecklist, Constants.OPEN_EDITOR)
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