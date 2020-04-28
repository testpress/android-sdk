package `in`.testpress.course.util

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.models.greendao.ProductDao
import android.content.Context

object ProductUtils {
    @JvmStatic
    fun getPriceForProduct(slug: String, context: Context): Float {
        val productDao = TestpressSDKDatabase.getProductDao(context)
        val products = productDao.queryBuilder()
            .where(ProductDao.Properties.Slug.eq(slug)).list()

        if (products.isNotEmpty()) {
            return products[0].currentPrice.toFloat()
        }

        return 0.0F
    }
}