package `in`.testpress.course.util

import android.content.res.AssetManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Typeface

object TestpressFont {

    fun getRubikMediumFont(assets: AssetManager): FontFamily {
        return FontFamily(
            Typeface(
                android.graphics.Typeface.createFromAsset(
                    assets,
                    "Rubik-Medium.ttf"
                )
            )
        )
    }
}