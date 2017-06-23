package in.testpress.ui.view;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.KeyEvent;

import in.testpress.util.UIUtils;

public class BackEventListeningEditText extends AppCompatEditText {

    private EditTextImeBackListener mOnImeBack;

    public BackEventListeningEditText(Context context) {
        super(context);
    }

    public BackEventListeningEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BackEventListeningEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK &&
                event.getAction() == KeyEvent.ACTION_UP) {
            if (mOnImeBack != null) {
                mOnImeBack.onImeBack(this, getText().toString());
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public void setImeBackListener(EditTextImeBackListener listener) {
        mOnImeBack = listener;
    }

    public interface EditTextImeBackListener {
        void onImeBack(BackEventListeningEditText editText, String text);
    }

    public void clearFocus(Activity activity) {
        clearFocus();
        UIUtils.hideSoftKeyboard(activity);
    }
}
