package `in`.testpress.store.viewmodel

import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressSession
import `in`.testpress.database.ProductDetailEntity
import `in`.testpress.models.InstituteSettings
import `in`.testpress.network.Resource
import `in`.testpress.store.repository.ProductDetailRepository
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel


class ProductDetailViewModel(val repository: ProductDetailRepository): ViewModel() {

    fun loadProductsList(forceFetch: Boolean, productSlug: String): LiveData<Resource<ProductDetailEntity?>> {
        return repository.fetch(forceFetch, productSlug)
    }
<<<<<<< Updated upstream

    fun isAccessCodeEnabled(context: Context): Boolean {
        val session: TestpressSession? = TestpressSdk.getTestpressSession(context)
        val settings: InstituteSettings? = session?.instituteSettings
        return settings?.isAccessCodeEnabled == true
    }
=======
>>>>>>> Stashed changes
}
