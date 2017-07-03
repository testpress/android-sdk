package in.testpress.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;

import in.testpress.R;
import in.testpress.ui.view.TouchImageView;

public class ZoomableImageString {

    public static SpannableString convertString(Spanned spanned, Activity activity,
                                                boolean whiteBackground) {

        SpannableString span = new SpannableString(trim(spanned, 0, spanned.length()));
        ImageSpan[] spans = span.getSpans(0, span.length(), ImageSpan.class);
        for(ImageSpan imageSpan : spans) {
            ClickableImageSpan clickableSpan =
                    new ClickableImageSpan(activity, imageSpan.getDrawable(), whiteBackground);

            span.setSpan(
                    clickableSpan,
                    span.getSpanStart(imageSpan),
                    span.getSpanStart(imageSpan) + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        return span;
    }

    // http://stackoverflow.com/a/16745540/400236
    private static CharSequence trim(CharSequence s, int start, int end) {
        while (start < end && Character.isWhitespace(s.charAt(start))) {
            start++;
        }

        while (end > start && Character.isWhitespace(s.charAt(end - 1))) {
            end--;
        }

        return s.subSequence(start, end);
    }

    private static class ClickableImageSpan extends ClickableSpan {
        Drawable drawable;
        Activity activity;
        boolean whiteBackground;

        ClickableImageSpan(Activity activity, Drawable drawable, boolean whiteBackground){
            this.activity = activity;
            this.drawable = drawable;
            this.whiteBackground = whiteBackground;
        }

        @Override
        public void onClick(View textView) {
            final ImageDialog dialog = new ImageDialog(activity, drawable, whiteBackground);
            dialog.show();
        }
    }

    private static class ImageDialog extends Dialog {
        UrlImageDownloader drawable;
        // Clone the drawable as the original image gets hidden
        Drawable clone;
        boolean whiteBackground;

        ImageDialog(Context context, Drawable drawable, boolean whiteBackground) {
            super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            this.whiteBackground = whiteBackground;
            this.drawable = (UrlImageDownloader)drawable;
            this.clone = ((UrlImageDownloader) drawable)
                    .drawable.getConstantState().newDrawable().mutate();
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.testpress_image_view_layout);
            Log.d("ZoomableImageString", "Using TouchImageView");
            TouchImageView image=(TouchImageView)findViewById(R.id.image);
            if (whiteBackground) {
                image.setBackgroundColor(Color.WHITE);
            } else {
                image.setBackgroundColor(Color.BLACK);
            }
            Log.d("ZoomableImageString", "Member Clone Mutate Drawable " + drawable);
            Log.d("ZoomableImageString", "Member Clone Mutate Drawable " + clone);
            image.setImageDrawable(clone);
        }
    }
}
