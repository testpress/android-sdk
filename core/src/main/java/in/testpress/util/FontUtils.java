package in.testpress.util;

import java.lang.reflect.Field;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import in.testpress.core.TestpressSdk;

@SuppressWarnings("ConstantConditions")
public final class FontUtils {

    /**
     * Load the font from the given path.
     *
     * @param context Context
     * @param fontAssetPath Custom font path which exists in the assets folder.
     *                      <br> eg. "font/vernada.ttf"
     * @return Typeface created from the given path.
     * @throws IllegalArgumentException if typeface couldn't be load from the given path.
     */
    public static Typeface getTypeface(@NonNull Context context, @NonNull String fontAssetPath) {
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null.");
        }
        if (fontAssetPath == null || fontAssetPath.isEmpty()) {
            throw new IllegalArgumentException("Font asset path must not be null or Empty.");
        }
        try {
            return Typeface.createFromAsset(context.getAssets(), fontAssetPath);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not get typeface '" + fontAssetPath
                    + "' because " + e.getMessage());
        }
    }

    /**
     * Replace the give inbuilt static typeface with the custom font in given path.
     *
     * @param context Context
     * @param staticTypefaceFieldName Static Typeface fields in {@link Typeface}.<br> eg. "MONOSPACE"
     * @param fontAssetPath Custom font path, which exist in assets folder.<br>eg. "font/vernada.ttf"
     */
    public static void replaceFont(@NonNull Context context, @NonNull String staticTypefaceFieldName,
                                   @NonNull String fontAssetPath) {
        replaceFont(staticTypefaceFieldName, getTypeface(context, fontAssetPath));
    }

    /**
     * Replace the give inbuilt static typeface with the given typeface.
     *
     * @param staticTypefaceFieldName Static Typeface fields in {@link Typeface}.<br> eg. "MONOSPACE"
     * @param newTypeface Typeface to override the given static Typeface.
     */
    @SuppressWarnings("TryWithIdenticalCatches")
    public static void replaceFont(@NonNull String staticTypefaceFieldName,
                                   @NonNull final Typeface newTypeface) {
        if (staticTypefaceFieldName == null || staticTypefaceFieldName.isEmpty()) {
            throw new IllegalArgumentException("Static Typeface FieldName must not be null or Empty.");
        }
        if (newTypeface == null) {
            throw new IllegalArgumentException("Typeface must not be null.");
        }
        try {
            final Field staticField = Typeface.class.getDeclaredField(staticTypefaceFieldName);
            staticField.setAccessible(true);
            staticField.set(null, newTypeface);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the custom font if exist, Default otherwise.
     *
     * @param context Context
     * @param viewGroup ViewGroup which child views font needs to be changed.
     */
    public static void applyTestpressFont(@NonNull Context context, @NonNull ViewGroup viewGroup) {
        applyTestpressFont(context, viewGroup, false);
    }

    /**
     *
     * @param context Context
     * @param viewGroup ViewGroup which child views font needs to be changed.
     * @param isBold True if child views font need to be bold, False otherwise.
     */
    public static void applyTestpressFont(@NonNull Context context, @NonNull ViewGroup viewGroup,
                                          boolean isBold) {
        if (viewGroup == null) {
            throw new IllegalArgumentException("ViewGroup must not be null.");
        }
        Typeface typeface = TestpressSdk.getTestpressFont(context).getTypeface(context);
        setFont(viewGroup, typeface, isBold);
    }

    private static void setFont(ViewGroup viewGroup, Typeface font, boolean isBold) {
        int count = viewGroup.getChildCount();
        View v;
        for(int i = 0; i < count; i++) {
            v = viewGroup.getChildAt(i);
            if(v instanceof TextView || v instanceof Button) {
                TextView textView = (TextView) v;
                if (textView.getTypeface().isBold() || isBold) {
                    textView.setTypeface(font, Typeface.BOLD);
                } else {
                    textView.setTypeface(font);
                }
            } else if(v instanceof ViewGroup)
                setFont((ViewGroup)v, font, isBold);
        }
    }

}