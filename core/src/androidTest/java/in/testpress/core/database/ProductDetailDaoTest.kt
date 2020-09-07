package `in`.testpress.core.database

import `in`.testpress.util.getOrAwaitValue
import org.junit.Assert
import org.junit.Test

class ProductDetailDaoTest: ProductDetailDbTestMixin() {

    @Test
    fun readShouldReturnInsertedData() {
        val productsDetail = productDetailFixture()
        db.productDetailDao().insert(productsDetail)

        val fetchedProductDetail = db.productDetailDao().getAll().getOrAwaitValue()
        Assert.assertEquals(productsDetail,fetchedProductDetail)
    }
}
