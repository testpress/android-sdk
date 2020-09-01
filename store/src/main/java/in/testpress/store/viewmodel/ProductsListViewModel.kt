package `in`.testpress.store.viewmodel

import `in`.testpress.database.ProductsListEntity
import `in`.testpress.network.Resource
import `in`.testpress.store.repository.ProductsListRepository
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class ProductsListViewModel(val repository: ProductsListRepository): ViewModel() {

    fun loadProductsList(): LiveData<Resource<ProductsListEntity>> {
        Log.e("TAGG net", internetIsConnected().toString())
        return repository.fetch(internetIsConnected())
    }

    private fun internetIsConnected(): Boolean {
        return try {
            val command = "ping -c 1 google.com"
            Runtime.getRuntime().exec(command).waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }
}