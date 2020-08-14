package `in`.testpress.store.models

import com.google.gson.annotations.SerializedName

data class PricesItem(
        var id: Int? = null,
        var name: String? = null,
        var price: String? = null,
        var validity: Int? = null,

        @SerializedName("end_date")
        var endDate: String? = null,

        @SerializedName("start_date")
        var startDate: String? = null

)