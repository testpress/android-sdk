package `in`.testpress.store.di

import `in`.testpress.database.ProductDao
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.store.network.StoreApiClient
import android.content.Context
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
}
