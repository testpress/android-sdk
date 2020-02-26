package `in`.testpress.core.database

import `in`.testpress.database.TestpressDatabase
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Before
import org.junit.Rule


abstract class DbTestMixin {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private lateinit var _db: TestpressDatabase
    val db: TestpressDatabase
        get() = _db

    @Before
    fun initDb() {
        _db = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                TestpressDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() {
        _db.close()
    }
}