package `in`.testpress.course.util

import `in`.testpress.course.ui.OfflineExamListActivity
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class SwipeToDeleteCallback(
    private val offlineExamAdapter: OfflineExamListActivity.OfflineExamAdapter,
    private val deleteIcon: Drawable
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val maxScaleFactor = 0.2f
    private var currentScale = 0.0f

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.bindingAdapterPosition
        offlineExamAdapter.removeItem(position)
    }

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val deleteBackground = createDeleteBackground()
        val iconMargin = calculateIconMargin(itemView.height)

        if (dX < 0) { // Swiping to the left
            setDeleteBackgroundBounds(deleteBackground, itemView, dX)
            setDeleteIconBounds(itemView, iconMargin, dX, viewHolder)
        } else {
            resetBounds(deleteBackground, deleteIcon)
        }

        drawBackgroundAndIcon(canvas, deleteBackground, deleteIcon)

        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun createDeleteBackground(): ColorDrawable {
        return ColorDrawable(Color.RED)
    }

    private fun calculateIconMargin(itemHeight: Int): Int {
        return (itemHeight - deleteIcon.intrinsicHeight) / 2
    }

    private fun setDeleteBackgroundBounds(deleteBackground: ColorDrawable, itemView: View, dX: Float) {
        deleteBackground.setBounds(
            itemView.right + dX.toInt(),
            itemView.top,
            itemView.right,
            itemView.bottom
        )
    }

    private fun setDeleteIconBounds(itemView: View, iconMargin: Int, dX: Float, viewHolder: RecyclerView.ViewHolder) {
        val swipeDistancePercentage = kotlin.math.abs(dX) / viewHolder.itemView.width
        currentScale = calculateCurrentScale(swipeDistancePercentage)

        deleteIcon.setBounds(
            itemView.right - iconMargin - (deleteIcon.intrinsicWidth * currentScale).toInt(),
            itemView.top + iconMargin + (deleteIcon.intrinsicHeight * (1 - currentScale) / 2).toInt(),
            itemView.right - iconMargin,
            itemView.bottom - iconMargin - (deleteIcon.intrinsicHeight * (1 - currentScale) / 2).toInt()
        )
    }

    private fun calculateCurrentScale(swipeDistancePercentage: Float): Float {
        return when {
            swipeDistancePercentage <= maxScaleFactor -> swipeDistancePercentage / maxScaleFactor
            else -> 1.0f
        }
    }

    private fun resetBounds(deleteBackground: ColorDrawable, deleteIcon: Drawable) {
        deleteBackground.setBounds(0, 0, 0, 0)
        deleteIcon.setBounds(0, 0, 0, 0)
    }

    private fun drawBackgroundAndIcon(canvas: Canvas, deleteBackground: ColorDrawable, deleteIcon: Drawable) {
        deleteBackground.draw(canvas)
        deleteIcon.draw(canvas)
    }
}
