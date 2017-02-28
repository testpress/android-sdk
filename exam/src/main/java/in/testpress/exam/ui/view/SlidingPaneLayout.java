package in.testpress.exam.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class SlidingPaneLayout extends android.support.v4.widget.SlidingPaneLayout {

    public SlidingPaneLayout(Context context) {
        super(context);
    }

    public SlidingPaneLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlidingPaneLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
