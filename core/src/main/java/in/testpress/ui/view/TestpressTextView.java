package in.testpress.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import in.testpress.core.TestpressFont;
import in.testpress.core.TestpressSdk;

public class TestpressTextView extends TextView {

    public TestpressTextView(Context context) {
        super(context);
        init();
    }

    public TestpressTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TestpressTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("NewApi")
    public TestpressTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        try {
            if (!isInEditMode()) {
                TestpressFont font = TestpressSdk.getTestpressFont(getContext());
                setTextSize(font.getSize().floatValue());
                setLineSpacing(0, font.getLineMultiplier().floatValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
