package `in`.testpress.exam.models

import android.os.Parcel
import android.os.Parcelable

data class UserUploadedFile(
    val id: Long? = null,
    val url: String? = null,
    val path: String? = null
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(url)
        parcel.writeString(path)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserUploadedFile> {
        override fun createFromParcel(parcel: Parcel): UserUploadedFile {
            return UserUploadedFile(parcel)
        }

        override fun newArray(size: Int): Array<UserUploadedFile?> {
            return arrayOfNulls(size)
        }
    }
}
