package `in`.testpress.store.di

import `in`.testpress.database.ProductDao
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.store.network.StoreApiClient
import `in`.testpress.store.repository.ProductsRepository
import `in`.testpress.store.ui.ProductsListActivity
import `in`.testpress.store.viewmodel.ProductListViewModel
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object StoreModule {

    @Singleton
    @Provides
    fun provideProductDao(@ApplicationContext context: Context): ProductDao {
        return TestpressDatabase(context).productDao()
    }

    @Singleton
    @Provides
    fun provideStoreApiClient(@ApplicationContext context: Context): StoreApiClient {
        return StoreApiClient(context)
    }

    @Provides
    fun provideProductListRepository(productDao: ProductDao, storeApiClient: StoreApiClient): ProductsRepository {
        return ProductsRepository(productDao, storeApiClient)
    }

    @Provides
    fun provideViewModel(repository: ProductsRepository): ProductListViewModel {
        return ProductListViewModel(repository, SavedStateHandle())
    }
}
