package `in`.testpress.store.models

import `in`.testpress.database.CoursesItem
import `in`.testpress.database.ProductsItem
import `in`.testpress.database.ProductsListEntity

data class ProductsListResponse(
    var next: String? = null,
    var perPage: Int? = null,
    var previous: String? = null,
    var count: Int? = null,
    var results: Result? = null
)

data class Result(
    var courses: List<CoursesItem?>? = null,
    var prices: List<PricesItem?>? = null,
    var products: List<ProductsItem?>? = null
)

fun Result.asDatabaseModel(): ProductsListEntity {
    return ProductsListEntity(
        courses = this.courses as List<CoursesItem>,
        products = this.products as List<ProductsItem>
    )
}


