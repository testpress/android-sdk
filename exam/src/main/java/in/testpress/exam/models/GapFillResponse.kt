package `in`.testpress.exam.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GapFillResponse(
    val order: Int,
    val answer: String
): Parcelable