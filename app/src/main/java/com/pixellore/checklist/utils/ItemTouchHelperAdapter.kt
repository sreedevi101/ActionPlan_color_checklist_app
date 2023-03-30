package com.pixellore.checklist.utils

/**
 * This interface defines the methods that a recycler adapter must implement to be notified of drag-and-drop
 * events in a RecyclerView.
*/

interface ItemTouchHelperAdapter {
    /**
     * This method is called when an item is moved. The adapter should update the position of the
     * dragged item in the list.
     *
     * @param fromPosition The original position of the item.
     * @param toPosition The new position of the item.
     * */

    fun onItemMove(fromPosition: Int, toPosition: Int)
    //fun onItemDismiss(position: Int)
}
