package `in`.testpress.util

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import `in`.testpress.R

/**
 * Drawable that draws custom colors for the system bars (status bar and navigation bar).
 */
private class SystemBarBackgroundDrawable(
    private val statusBarColor: Int,
    private val navBarColor: Int
) : Drawable() {

    private var statusBarHeight = 0
    private var navBarHeight = 0
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * Updates the inset heights and redraws.
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

        // Draw status bar area
        paint.color = statusBarColor
        canvas.drawRect(0f, 0f, width.toFloat(), statusBarHeight.toFloat(), paint)

        // Draw content area (between status and nav bar) in white
        paint.color = Color.WHITE
        canvas.drawRect(
            0f,
            statusBarHeight.toFloat(),
            width.toFloat(),
            (height - navBarHeight).toFloat(),
            paint
        )

        // Draw navigation bar area
        paint.color = navBarColor
        canvas.drawRect(
            0f,
            (height - navBarHeight).toFloat(),
            width.toFloat(),
            height.toFloat(),
            paint
        )
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}

/**
 * Applies custom colors to the system status bar and navigation bar
 * by adding a background drawable that accounts for insets.
 */
fun applySystemBarColors(rootView: View, statusBarColor: Int, navBarColor: Int) {
    val systemBarDrawable = SystemBarBackgroundDrawable(statusBarColor, navBarColor)
    rootView.background = systemBarDrawable

    ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
        val statusInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
        val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

        systemBarDrawable.updateInsets(statusInsets.top, navInsets.bottom)
        view.setPadding(0, statusInsets.top, 0, navInsets.bottom)

        insets
    }
}

/**
 * Applies primary color to the status bar and white color to the navigation bar,
 * using system bar insets for padding and background drawing.
 *
 * Example usage in Activity (Kotlin):
 *
 *     class MainActivity : AppCompatActivity() {
 *         override fun onCreate(savedInstanceState: Bundle?) {
 *             super.onCreate(savedInstanceState)
 *             setContentView(R.layout.activity_main)
 *             applySystemBarColors(window.decorView.rootView)
 *         }
 *     }
 *
 * Example usage in Fragment (Kotlin):
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