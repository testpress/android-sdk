package `in`.testpress.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.slidingpanelayout.widget.SlidingPaneLayout

class SlidingPaneLayout : SlidingPaneLayout {
    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context!!, attrs, defStyle) {}

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return false
    }
}
