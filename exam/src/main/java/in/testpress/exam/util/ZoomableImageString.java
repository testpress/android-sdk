package in.testpress.exam.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;

import in.testpress.exam.R;
import in.testpress.exam.ui.view.TouchImageView;

public class ZoomableImageString {

    Activity activity;

    public ZoomableImageString(Activity activity) {
        this.activity = activity;
    }

    public SpannableString convertString(Spanned spanned) {
        SpannableString span = new SpannableString(trim(spanned, 0, spanned.length()));
        ImageSpan[] spans = span.getSpans(0, span.length(), ImageSpan.class);
        for(ImageSpan imageSpan : spans) {
            ClickableImageSpan clickableSpan = new ClickableImageSpan(activity, imageSpan.getDrawable());
            span.setSpan(clickableSpan, span.getSpanStart(imageSpan), span.getSpanStart(imageSpan)+1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;    }

    // http://stackoverflow.com/a/16745540/400236
    CharSequence trim(CharSequence s, int start, int end) {
        while (start < end && Character.isWhitespace(s.charAt(start))) {
            start++;
        }

        while (end > start && Character.isWhitespace(s.charAt(end - 1))) {
            end--;
        }

        return s.subSequence(start, end);
    }

    class ClickableImageSpan extends ClickableSpan {
        Drawable drawable;
        Activity activity;

        ClickableImageSpan(Activity activity, Drawable drawable){
            this.activity = activity;
            this.drawable= drawable;
        }

        @Override
        public void onClick(View textView) {
            final ImageDialog dialog = new ImageDialog(activity, drawable);
            dialog.show();
        }
    }

    private class ImageDialog extends Dialog {
        UrlImageDownloader drawable;
        // Clone the drawable as the original image gets hidden
        Drawable clone;

        public ImageDialog(Context context,Drawable drawable) {
            super(context, android.R.style.Theme_Dialog);
            this.drawable = (UrlImageDownloader)drawable;
            this.clone = ((UrlImageDownloader) drawable).drawable.getConstantState().newDrawable().mutate();
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.testpress_image_view_layout);
            Log.d("ZoomableImageString", "Using TouchImageView");
            TouchImageView image=(TouchImageView)findViewById(R.id.image);
            Log.d("ZoomableImageString", "Member Clone Mutate Drawable " + drawable);
            Log.d("ZoomableImageString", "Member Clone Mutate Drawable " + clone);
            image.setImageDrawable(clone);
        }
    }
}
