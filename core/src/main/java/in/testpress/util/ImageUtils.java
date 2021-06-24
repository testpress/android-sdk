package in.testpress.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import in.testpress.R;

public class ImageUtils {
    private static String[] imageExtensions = {"jpg", "jpeg", "png", "gif", "JPG", "JPEG", "PNG", "GIF"};

    /**
     * Init the ImageLoader with custom configuration.
     *
     * @param context Context
     * @return Initialized ImageLoader.
     */
    public static ImageLoader initImageLoader(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null.");
        }
        ImageLoader imageLoader = ImageLoader.getInstance();
        if (imageLoader.isInited()) {
            return imageLoader;
        } else {
            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .cacheOnDisk(true).cacheInMemory(true)
                    .imageScaleType(ImageScaleType.EXACTLY)
                    .displayer(new FadeInBitmapDisplayer(300)).build();

            ImageLoaderConfiguration config =
                    new ImageLoaderConfiguration.Builder(context.getApplicationContext())
                            .defaultDisplayImageOptions(defaultOptions)
                            .memoryCache(new WeakMemoryCache())
                            .diskCacheSize(500 * 1024 * 1024).build();

            imageLoader.init(config);
            return imageLoader;
        }
    }

    public static DisplayImageOptions getPlaceholdersOption() {
        return new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageForEmptyUri(R.drawable.testpress_placeholder_icon)
                .showImageOnFail(R.drawable.testpress_placeholder_icon)
                .showImageOnLoading(R.drawable.testpress_placeholder_icon).build();
    }

    private static boolean isImagePresentInCache(String imageName, Context context) {
        File file = new File(context.getCacheDir().getAbsolutePath() + "/"  + imageName);
        return file.exists();
    }

    private static void storeImageToCache(String url, Context context) {
        try {
            String imageName = url.substring(url.lastIndexOf('/') + 1);
            InputStream in = new java.net.URL(url).openStream();
            Bitmap image = BitmapFactory.decodeStream(in);
            FileOutputStream fos = new FileOutputStream(new File(context.getCacheDir().getAbsolutePath() + "/" + imageName));
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (MalformedURLException e) {
            Log.d("WebviewUtils", "MalformedURLException " + e.getLocalizedMessage());
        } catch (IOException e) {
            Log.d("WebviewUtils", "Unknown IO-error occurred! Try again...");
        }
    }

    static boolean isImage(String url) {
        for (String extension : imageExtensions) {
            if (url.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    static FileInputStream readImageFromCache(String url, Context context) throws FileNotFoundException {
        String imageName = url.substring(url.lastIndexOf('/') + 1);
        if (!isImagePresentInCache(imageName, context)) {
            storeImageToCache(url, context);
        }

        return new FileInputStream(new File(context.getCacheDir().getAbsolutePath() + "/" + imageName));
    }
}
