package com.pixellore.checklist.AdapterUtility

import android.content.res.ColorStateList
import android.graphics.Paint
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pixellore.checklist.DatabaseUtility.Subtask
import com.pixellore.checklist.R

class SubTaskRecycleAdapter(private val clickListenerSubtask: (position: Int, subtask: Subtask) -> Unit,
                            private val listener:  (position: Int, subtask: Subtask) -> Unit) :
    ListAdapter<Subtask, SubTaskRecycleAdapter.SubtaskViewHolder>(SubItemComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtaskViewHolder {
        return SubtaskViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: SubtaskViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current, clickListenerSubtask, listener)
    }

    class SubtaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val subtaskItem: ConstraintLayout = itemView.findViewById(R.id.subtask_item_layout)
        private val subtaskTitleView: TextView = itemView.findViewById(R.id.subtaskTitle)
        private val completedCheckBox: CheckBox = itemView.findViewById(R.id.subtaskCompletedCheck)

        fun bind(
            currentSubtask: Subtask,
            clickListenerSubtask: (position: Int, subtask: Subtask) -> Unit,
            listener: (position: Int, subtask: Subtask) -> Unit
        ) {
            subtaskTitleView.text = currentSubtask.subtask_title


            toggleStrikeThrough(
                textViewToStrike = subtaskTitleView,
                currentSubtask.subtask_isCompleted
            )
            completedCheckBox.isChecked = currentSubtask.subtask_isCompleted

            // set color if bodyTextColorResId is not null
            currentSubtask.subtask_font?.bodyTextColorResId?.let {
                subtaskTitleView.setTextColor(it)

                val colorStateList = ColorStateList.valueOf(it)
                completedCheckBox.buttonTintList = colorStateList
            }

            completedCheckBox.setOnClickListener {
                val isChecked = completedCheckBox.isChecked
                currentSubtask.subtask_isCompleted = isChecked
                toggleStrikeThrough(textViewToStrike = subtaskTitleView, isChecked)
                clickListenerSubtask(adapterPosition, currentSubtask)

            }

            subtaskItem.setOnClickListener {
                // Call the interface method to notify the listener
                listener(adapterPosition, currentSubtask)
            }
        }

        private fun toggleStrikeThrough(textViewToStrike: TextView, isChecked: Boolean) {
            if (isChecked) {
                textViewToStrike.paintFlags =
                    textViewToStrike.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                textViewToStrike.paintFlags =
                    textViewToStrike.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

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
    class SubItemComparator : DiffUtil.ItemCallback<Subtask>() {
        override fun areItemsTheSame(oldItem: Subtask, newItem: Subtask): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Subtask, newItem: Subtask): Boolean {
            return oldItem.subtask_title == newItem.subtask_title
        }
    }

}