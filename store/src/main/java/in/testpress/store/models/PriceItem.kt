package `in`.testpress.store.models

import android.os.Parcel
import android.os.Parcelable

data class PricesItem(
    var id: Int? = null,
    var name: String? = null,
    var price: String? = null,
    var validity: Int? = null,
    var endDate: String? = null,
    var startDate: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(name)
        parcel.writeString(price)
        parcel.writeValue(validity)
        parcel.writeString(endDate)
        parcel.writeString(startDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PricesItem> {
        override fun createFromParcel(parcel: Parcel): PricesItem {
            return PricesItem(parcel)
        }

        override fun newArray(size: Int): Array<PricesItem?> {
            return arrayOfNulls(size)
        }
    }
}

