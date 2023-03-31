package com.pixellore.checklist.AdapterUtility

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pixellore.checklist.DataClass.TutorialPage
import com.pixellore.checklist.R

class TutorialRecyclerAdapter (
    private val tutorialPagesList: List<TutorialPage>
):
    RecyclerView.Adapter<TutorialRecyclerAdapter.TutorialRecyclerViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutorialRecyclerViewHolder {
        return TutorialRecyclerViewHolder.create(parent)
    }

    override fun getItemCount(): Int {
        return tutorialPagesList.size
    }

    override fun onBindViewHolder(holder: TutorialRecyclerViewHolder, position: Int) {
        val currentTutorialPage = tutorialPagesList[position]
        holder.bind(currentTutorialPage)
    }

    class TutorialRecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(currentTutorialPage: TutorialPage) {

            val headingTextView = itemView.findViewById<TextView>(R.id.tutorial_heading)
            val descriptionTextView = itemView.findViewById<TextView>(R.id.tutorial_explanation)
            val iconImage = itemView.findViewById<ImageView>(R.id.tutorial_icon)


            headingTextView.text = currentTutorialPage.heading
            descriptionTextView.text = currentTutorialPage.explanation
            iconImage.setImageResource(currentTutorialPage.iconResId)

        }

        companion object {
            fun create(parent: ViewGroup): TutorialRecyclerViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.tutorial_item, parent, false)
                return TutorialRecyclerViewHolder(view)
            }
        }
    }
}