package `in`.testpress.util

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import `in`.testpress.R

/**
 * Drawable that overlays custom colors for the system bars (status bar and navigation bar)
 * without affecting the existing background of the root view.
 */
private class SystemBarOverlayDrawable(
    private val statusBarColor: Int,
    private val navBarColor: Int
) : Drawable() {

    private var statusBarHeight = 0
    private var navBarHeight = 0
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * Updates the status bar and navigation bar inset heights and triggers a redraw
     * if they have changed.
     */
    fun updateInsets(statusBarHeight: Int, navBarHeight: Int) {
        if (this.statusBarHeight != statusBarHeight || this.navBarHeight != navBarHeight) {
            this.statusBarHeight = statusBarHeight
            this.navBarHeight = navBarHeight
            invalidateSelf()
        }
    }

    override fun draw(canvas: Canvas) {
        val width = bounds.width()
        val height = bounds.height()

        // Draw status bar area overlay
        if (statusBarHeight > 0) {
            paint.color = statusBarColor
            canvas.drawRect(0f, 0f, width.toFloat(), statusBarHeight.toFloat(), paint)
        }

        // Draw navigation bar area overlay
        if (navBarHeight > 0) {
            paint.color = navBarColor
            canvas.drawRect(
                0f,
                (height - navBarHeight).toFloat(),
                width.toFloat(),
                height.toFloat(),
                paint
            )
        }
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}

/**
 * Applies overlay colors to the system status bar and navigation bar
 * without replacing the existing background of the root view.
 *
 * This draws the overlay in the system bar inset areas only.
 */
fun applySystemBarColors(rootView: View, statusBarColor: Int, navBarColor: Int) {
    val overlayDrawable = SystemBarOverlayDrawable(statusBarColor, navBarColor)

    val existingBackground = rootView.background
    val layeredDrawable = if (existingBackground != null) {
        LayerDrawable(arrayOf(existingBackground, overlayDrawable))
    } else {
        overlayDrawable
    }

    rootView.background = layeredDrawable

    ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
        val statusInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
        val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

        overlayDrawable.updateInsets(statusInsets.top, navInsets.bottom)
        view.setPadding(0, statusInsets.top, 0, navInsets.bottom)

        insets
    }
}

/**
 * Applies app theme's primary color to the status bar and white color to the navigation bar
 * on Android 15 (API 35) and above, using system bar insets for layout and background drawing.
 *
 * This function should be called from your Activity or Fragment to ensure proper appearance.
 *
 * Example usage in Activity:
 *
 *     class MainActivity : AppCompatActivity() {
 *         override fun onCreate(savedInstanceState: Bundle?) {
 *             super.onCreate(savedInstanceState)
 *             setContentView(R.layout.activity_main)
 *             applySystemBarColors(window.decorView.rootView)
 *         }
 *     }
 *
 * Example usage in Fragment:
 *
 *     class HomeFragment : Fragment(R.layout.fragment_home) {
 *         override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *             super.onViewCreated(view, savedInstanceState)
 *             requireContext().applySystemBarColors(view)
 *         }
 *     }
 */
fun Context.applySystemBarColors(rootView: View) {
    val isRuntimeApiAbove34 = Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    val isTargetApiAbove34 = applicationInfo.targetSdkVersion > Build.VERSION_CODES.UPSIDE_DOWN_CAKE

    if (isRuntimeApiAbove34 && isTargetApiAbove34) {
        val statusBarColor = ContextCompat.getColor(this, R.color.testpress_color_primary)
        val navBarColor = ContextCompat.getColor(this, R.color.testpress_white)
        applySystemBarColors(rootView, statusBarColor, navBarColor)
    }
}
