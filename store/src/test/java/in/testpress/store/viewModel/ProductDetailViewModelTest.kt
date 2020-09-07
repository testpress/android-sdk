package `in`.testpress.store.viewModel

import `in`.testpress.network.Resource
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ProductDetailViewModelTest: ProductDetailViewModelTestMixin() {

    @Test
    fun fetchShouldReturnResponseOfProductDetail() {
        var expectedResponse = Resource.success(null)
        repository.setResponse(Resource.success(null))

        Assert.assertEquals(expectedResponse,repository.fetch().value)

        expectedResponse = Resource.loading(null)
        repository.setResponse(Resource.loading(null))

        Assert.assertEquals(expectedResponse,repository.fetch().value)
    }
}
