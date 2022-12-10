package com.pixellore.checklist.AdapterUtility

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pixellore.checklist.DatabaseUtility.Subtask
import com.pixellore.checklist.DatabaseUtility.Task
import com.pixellore.checklist.R

class SubTaskRecycleAdapter(private val clickListenerSubtask: (position: Int, subtask: Subtask) -> Unit):
    ListAdapter<Subtask, SubTaskRecycleAdapter.SubtaskViewHolder>(SubItemComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtaskViewHolder {
        return SubtaskViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: SubtaskViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current, clickListenerSubtask)
    }

    class SubtaskViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        private val subtaskTitleView: TextView = itemView.findViewById(R.id.subtaskTitle)

        fun bind(currentSubtask: Subtask,
                 clickListenerSubtask: (position: Int, subtask: Subtask) -> Unit)
        {
            subtaskTitleView.text = currentSubtask.subtask_title
<<<<<<< Updated upstream
=======

            toggleStrikeThrough(textViewToStrike = subtaskTitleView, currentSubtask.subtask_isCompleted)
            completedCheckBox.isChecked = currentSubtask.subtask_isCompleted

            completedCheckBox.setOnClickListener {
                val isChecked  = completedCheckBox.isChecked
                currentSubtask.subtask_isCompleted = isChecked
                toggleStrikeThrough(textViewToStrike = subtaskTitleView, isChecked)
                clickListenerSubtask(adapterPosition, currentSubtask)
            }
        }

        private fun toggleStrikeThrough(textViewToStrike:TextView, isChecked:Boolean){
            if (isChecked){
                textViewToStrike.paintFlags = textViewToStrike.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else{
                textViewToStrike.paintFlags = textViewToStrike.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
>>>>>>> Stashed changes
        }

        companion object {
            fun create(parent: ViewGroup): SubtaskViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.subtask_item, parent, false)
                return SubtaskViewHolder((view))
            }
        }

    }

    /*
    * Callback for calculating the diff between two non-null items in a list.
    * The WordsComparator defines how to compute if two words are the same or if the contents are the same.
    * */
    class SubItemComparator: DiffUtil.ItemCallback<Subtask>() {
        override fun areItemsTheSame(oldItem: Subtask, newItem: Subtask): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Subtask, newItem: Subtask): Boolean {
            return oldItem.subtask_title == newItem.subtask_title
        }
    }

}