package `in`.testpress.course.util

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.models.greendao.Product
import `in`.testpress.models.greendao.ProductDao
import android.content.Context
import org.greenrobot.greendao.query.QueryBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito
import org.powermock.api.mockito.PowerMockito.`when`
import org.powermock.api.mockito.PowerMockito.mockStatic
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
@PrepareForTest(TestpressSDKDatabase::class)
class ProductUtilsTest {
    @Mock
    lateinit var context: Context
    private val productDao = Mockito.mock(ProductDao::class.java)
    @Mock
    lateinit var queryBuilder: QueryBuilder<Product>

    @Before
    fun setUp() {
        mockStatic(TestpressSDKDatabase::class.java)
        `when`(TestpressSDKDatabase.getProductDao(any())).thenReturn(productDao)
        `when`(productDao.queryBuilder()).thenReturn(queryBuilder)
        `when`(
            productDao.queryBuilder().where(ArgumentMatchers.any())
        ).thenReturn(queryBuilder)
    }

    @Test
    fun getPriceForProductShouldReturnPriceInFloatForMatchingProduct() {
        val product = Product(1)
        product.currentPrice = "4.1"
        `when`(productDao.queryBuilder().list()).thenReturn(listOf(product))

        assert(ProductUtils.getPriceForProduct("a", context) == product.currentPrice.toFloat())
    }

    @Test
    fun getPriceForProductShouldReturnZeroIfProductIsNotFound() {
        `when`(productDao.queryBuilder().list()).thenReturn(listOf())
        assert(ProductUtils.getPriceForProduct("a", context) == 0.0f)
    }
}