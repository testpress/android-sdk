package in.testpress.util;

import java.lang.reflect.Field;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;

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
            throw new IllegalArgumentException("Font asset path must not be null or Empty.");
        }
        if (newTypeface == null) {
            throw new IllegalArgumentException("Font asset path must not be null or Empty.");
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

}