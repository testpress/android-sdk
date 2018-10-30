package in.testpress.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;

import in.testpress.R;
import in.testpress.util.ClickableMovementMethod;

// Modified version of https://github.com/bravoborja/ReadMoreTextView/
public class ReadMoreTextView extends AppCompatTextView {

    private static final int TRIM_MODE_LINES = 0;
    private static final int TRIM_MODE_LENGTH = 1;
    private static final int DEFAULT_TRIM_LENGTH = 240;
    private static final int DEFAULT_TRIM_LINES = 2;
    private static final boolean DEFAULT_SHOW_TRIM_EXPANDED_TEXT = true;
    private static final String ELLIPSIZE = "... ";

    private CharSequence text;
    private boolean readMore = true;
    private int trimLength;
    private CharSequence trimCollapsedText;
    private CharSequence trimExpandedText;
    private ReadMoreClickableSpan viewMoreSpan;
    private int colorClickableText;
    private boolean showTrimExpandedText;

    private int trimMode;
    private int trimLines;
    private StateChangeListener stateChangeListener;

    public ReadMoreTextView(Context context) {
        this(context, null);
    }

    public ReadMoreTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ReadMoreTextView);
        trimLength = typedArray.getInt(R.styleable.ReadMoreTextView_trimLength, DEFAULT_TRIM_LENGTH);
        int resourceIdTrimCollapsedText = typedArray.getResourceId(
                R.styleable.ReadMoreTextView_trimCollapsedText, R.string.testpress_read_more);
        int resourceIdTrimExpandedText = typedArray.getResourceId(
                R.styleable.ReadMoreTextView_trimExpandedText, R.string.testpress_read_less);
        this.trimCollapsedText = getResources().getString(resourceIdTrimCollapsedText);
        this.trimExpandedText = getResources().getString(resourceIdTrimExpandedText);
        this.trimLines = typedArray.getInt(R.styleable.ReadMoreTextView_trimLines, DEFAULT_TRIM_LINES);
        this.colorClickableText = typedArray.getColor(R.styleable.ReadMoreTextView_colorClickableText,
                ContextCompat.getColor(context, R.color.testpress_blue_text));
        this.showTrimExpandedText = typedArray.getBoolean(
                R.styleable.ReadMoreTextView_showTrimExpandedText, DEFAULT_SHOW_TRIM_EXPANDED_TEXT);
        this.trimMode = typedArray.getInt(R.styleable.ReadMoreTextView_trimMode, TRIM_MODE_LINES);
        typedArray.recycle();
        viewMoreSpan = new ReadMoreClickableSpan();
        setText();
    }

    private void setText() {
        setText(getDisplayableText());
        setMovementMethod(ClickableMovementMethod.getInstance());
        setClickable(false);
        setLongClickable(false);
        setHighlightColor(Color.TRANSPARENT);
    }

    private CharSequence getDisplayableText() {
        return getTrimmedText(text);
    }

    public void setText(CharSequence text, boolean readMore) {
        this.text = text;
        this.readMore = readMore;
        if (trimMode == TRIM_MODE_LINES) {
            setText(text);
            if (getLineCount() == 0) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        setText();
                    }
                });
                return;
            }
        }
        setText();
    }

    public void setText(CharSequence text, boolean readMore, StateChangeListener stateChangeListener) {
        this.stateChangeListener = stateChangeListener;
        setText(text, readMore);
    }

    private CharSequence getTrimmedText(CharSequence text) {
        if (trimMode == TRIM_MODE_LENGTH) {
            if (text != null && text.length() > trimLength) {
                if (readMore) {
                    return updateCollapsedText();
                } else {
                    return updateExpandedText();
                }
            }
        }
        if (trimMode == TRIM_MODE_LINES) {
            if (text != null && text.length() > trimLength) {
                if (readMore) {
                    if (getLineCount() > trimLines) {
                        return updateCollapsedText();
                    }
                } else {
                    return updateExpandedText();
                }
            }
        }
        return text;
    }

    private CharSequence updateCollapsedText() {
        int trimEndIndex = trimLength - ELLIPSIZE.length() - trimCollapsedText.length() + 1;
        SpannableStringBuilder s = new SpannableStringBuilder(text, 0, trimEndIndex)
                .append(ELLIPSIZE)
                .append(trimCollapsedText);
        return addClickableSpan(s, trimCollapsedText);
    }

    private CharSequence updateExpandedText() {
        if (showTrimExpandedText) {
            SpannableStringBuilder s =
                    new SpannableStringBuilder(text, 0, text.length()).append(trimExpandedText);

            return addClickableSpan(s, trimExpandedText);
        }
        return text;
    }

    private CharSequence addClickableSpan(SpannableStringBuilder s, CharSequence trimText) {
        int startIndex = s.length() - trimText.length();
        s.setSpan(viewMoreSpan, startIndex, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return s;
    }

    public void setTrimLength(int trimLength) {
        this.trimLength = trimLength;
        setText();
    }

    public void setColorClickableText(int colorClickableText) {
        this.colorClickableText = colorClickableText;
    }

    public void setTrimCollapsedText(CharSequence trimCollapsedText) {
        this.trimCollapsedText = trimCollapsedText;
    }

    public void setTrimExpandedText(CharSequence trimExpandedText) {
        this.trimExpandedText = trimExpandedText;
    }

    public void setTrimMode(int trimMode) {
        this.trimMode = trimMode;
    }

    public void setTrimLines(int trimLines) {
        this.trimLines = trimLines;
    }

    private class ReadMoreClickableSpan extends ClickableSpan {
        @Override
        public void onClick(@NonNull View widget) {
            readMore = !readMore;
            if (stateChangeListener != null) {
                stateChangeListener.onStateChanged(!readMore);
            }
            setText();
        }

        @Override
        public void updateDrawState(@NonNull TextPaint ds) {
            ds.setColor(colorClickableText);
        }
    }

    public interface StateChangeListener {

        void onStateChanged(boolean expanded);

    }

}