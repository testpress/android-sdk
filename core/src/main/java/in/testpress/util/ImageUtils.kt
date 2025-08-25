package `in`.testpress.util

import android.content.Context
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.assist.ImageScaleType
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer

import `in`.testpress.R

object ImageUtils {

    /**
     * Init the ImageLoader with custom configuration.
     *
     * @param context Context
     * @return Initialized ImageLoader.
     */
    @JvmStatic
    fun initImageLoader(context: Context?): ImageLoader {
        requireNotNull(context) { "Context must not be null." }

        val imageLoader = ImageLoader.getInstance()
        if (imageLoader.isInited) {
            return imageLoader
        }

        val defaultOptions = DisplayImageOptions.Builder()
            .cacheOnDisk(true).cacheInMemory(true)
            .imageScaleType(ImageScaleType.EXACTLY)
            .displayer(FadeInBitmapDisplayer(300)).build()

        val config = ImageLoaderConfiguration.Builder(context.applicationContext)
            .defaultDisplayImageOptions(defaultOptions)
            .memoryCache(WeakMemoryCache())
            .diskCacheSize(500 * 1024 * 1024).build()

        return imageLoader.also { it.init(config) }
    }

    private fun buildPlaceholderOptions(placeholderResId: Int): DisplayImageOptions.Builder {
        return DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .resetViewBeforeLoading(true)
            .showImageForEmptyUri(placeholderResId)
            .showImageOnFail(placeholderResId)
            .showImageOnLoading(placeholderResId)
    }

    @JvmStatic
    fun getPlaceholdersOption(): DisplayImageOptions {
        return buildPlaceholderOptions(R.drawable.testpress_placeholder_icon)
            .imageScaleType(ImageScaleType.EXACTLY)
            .build()
    }

    @JvmStatic
    fun getAvatarPlaceholdersOption(): DisplayImageOptions {
        return buildPlaceholderOptions(R.drawable.profile_image_place_holder).build()
    }
}
