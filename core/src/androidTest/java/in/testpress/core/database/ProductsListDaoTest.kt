package `in`.testpress.core.database

import `in`.testpress.util.getOrAwaitValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductsListDaoTest: ProductListDaoTestMixin() {

    @Test
    fun readShouldReturnInsertedData() {
        val productsList = productListFixture()
        db.productsListDao().insert(productsList)

        val fetchedProducts = db.productsListDao().getAll().getOrAwaitValue()
        Assert.assertEquals(productsList,fetchedProducts)
    }
}
