package `in`.testpress.store.viewModel

import `in`.testpress.network.Resource
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ProductDetailViewModelTest: ProductDetailViewModelTestMixin() {

    @Test
    fun fetchShouldReturnSuccessResponseWhenItIsSuccess() {
        val expectedResponse = Resource.success(null)
        repository.setResponse(Resource.success(null))

        Assert.assertEquals(expectedResponse,repository.fetch().value)
    }

    @Test
    fun fetchShouldReturnLoadingWhenItsBeenLoading() {
        val expectedResponse = Resource.loading(null)
        repository.setResponse(Resource.loading(null))

        Assert.assertEquals(expectedResponse,repository.fetch().value)
    }

    @Test
    fun isAccessCodeEnabledReturnsFalseWhenItIsDisabled() {
        Assert.assertFalse(isAccessCodeEnabled)
    }

    @Test
    fun isAccessCodeEnabledReturnsTrueWhenItIsEnabled() {
        enableHaveAccessCode()

        Assert.assertTrue(isAccessCodeEnabled)
    }
}
