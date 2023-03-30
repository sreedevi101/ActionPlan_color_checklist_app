package com.pixellore.checklist.utils

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * This is the callback for the ItemTouchHelper that is used to enable drag-and-drop functionality
 * for a RecyclerView. The ItemTouchHelperCallback class extends the ItemTouchHelper.Callback class
 * which defines the basic touch handling behavior.
 * It is used in combination with an adapter that implements the ItemTouchHelperAdapter interface.
 *
 * @param adapter An instance of RecyclerView.Adapter that implements the ItemTouchHelperAdapter
 * interface. This adapter will be notified of drag-and-drop events.
 *
 * */
class ItemTouchHelperCallback(private val adapter: ItemTouchHelperAdapter) :
    ItemTouchHelper.Callback() {

    /**
     * This method is called to determine the drag directions of a ViewHolder. We return
     * ItemTouchHelper.UP and ItemTouchHelper.DOWN to enable vertical dragging.
     *
     * @param recyclerView The RecyclerView to which the ItemTouchHelper is attached.
     * @param viewHolder The ViewHolder for which the drag directions are calculated.
     * @return The drag directions for the ViewHolder.
     */
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0)
    }

    /**
     * This method is called when an item is moved. It notifies the adapter of the move and
     * updates the position of the dragged item in the list.
     *
     * @param recyclerView The RecyclerView to which the ItemTouchHelper is attached.
     * @param viewHolder The ViewHolder that is being moved.
     * @param target The ViewHolder that is the target of the move.
     * @return true if the move is completed, false otherwise.
     */

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val fromPosition = viewHolder.adapterPosition
        val toPosition = target.adapterPosition
        adapter.onItemMove(fromPosition, toPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)

        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            // Notify the ViewHolder that it has been selected
            (viewHolder as? ItemTouchHelperViewHolder)?.onItemSelected()
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)

        // Notify the ViewHolder that it has been cleared
        (viewHolder as? ItemTouchHelperViewHolder)?.onItemClear()
    }
}
