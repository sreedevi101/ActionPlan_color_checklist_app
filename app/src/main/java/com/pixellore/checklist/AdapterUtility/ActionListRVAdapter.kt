package com.pixellore.checklist.AdapterUtility

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pixellore.checklist.DatabaseUtility.Task
import com.pixellore.checklist.R

/**
* Adapter for the RecyclerView to display action items (in database) as a list
 *
* */
class ActionListRVAdapter:
    ListAdapter<Task, ActionListRVAdapter.ActionListRVViewHolder>(ActionItemComparator()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionListRVViewHolder {
        return ActionListRVViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ActionListRVViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current.title)
    }


    class ActionListRVViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val taskTitleView: TextView = itemView.findViewById(R.id.actionItemTitle)

        fun bind(text: String?) {
            taskTitleView.text = text
        }

        companion object {
            fun create(parent: ViewGroup): ActionListRVViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.task_item, parent, false)
                return ActionListRVViewHolder((view))
            }
        }


    }
    /*
    * Callback for calculating the diff between two non-null items in a list.
    * The WordsComparator defines how to compute if two words are the same or if the contents are the same.
    * */
    class ActionItemComparator: DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.title == newItem.title
        }
    }

}