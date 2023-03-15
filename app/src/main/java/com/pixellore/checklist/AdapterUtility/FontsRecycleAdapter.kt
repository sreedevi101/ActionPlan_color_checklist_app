package com.pixellore.checklist.AdapterUtility

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pixellore.checklist.DataClass.TextFont
import com.pixellore.checklist.R

class FontsRecycleAdapter(
    private val fontsList: ArrayList<TextFont>,
    private val onFontSelectedListener: OnFontSelectedListener
)  : RecyclerView.Adapter<FontsRecycleAdapter.FontsViewHolder>() {


    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FontsViewHolder {
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.font_item, parent, false)
        return FontsViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return fontsList.size
    }

    override fun onBindViewHolder(holder: FontsViewHolder, position: Int) {
        holder.itemPosition = position
        holder.bind(holder.itemView.context)
    }

    inner class FontsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val fontName = itemView.findViewById<TextView>(R.id.font_name)
        val selectionCheckMark = itemView.findViewById<ImageView>(R.id.selection_check_mark)
        val currentFont = itemView.findViewById<TextView>(R.id.current_font_indication)
        var itemPosition: Int = 0

        fun bind(context: Context) {

            val fontAtPos = fontsList[itemPosition]
            if (itemPosition == selectedPosition) {
                selectionCheckMark.visibility = View.VISIBLE
            } else {
                selectionCheckMark.visibility = View.GONE
            }

            // Load the custom font file from the assets folder
            val typeface = Typeface.createFromAsset(context.assets, fontAtPos.file_name)

            fontName.text = fontAtPos.font_name
            fontName.typeface = typeface


            if (fontAtPos.is_current_font){
                currentFont.visibility = View.VISIBLE
            } else{
                currentFont.visibility = View.GONE
            }

            itemView.setOnClickListener{
                onFontSelectedListener.onFontSelected(fontAtPos, itemPosition)
                val prevSelectedPosition = selectedPosition
                selectedPosition = itemPosition
                notifyItemChanged(prevSelectedPosition)
                notifyItemChanged(selectedPosition)
            }
        }
    }

    interface OnFontSelectedListener {
        fun onFontSelected(textFont: TextFont, position: Int)
    }
}