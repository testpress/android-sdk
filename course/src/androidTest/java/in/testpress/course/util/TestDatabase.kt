package `in`.testpress.course.util

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import `in`.testpress.database.TestpressDatabase
import org.junit.After
import org.junit.Before
import org.junit.Rule
import java.util.concurrent.Executors

abstract class TestDatabase {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private lateinit var _database: TestpressDatabase
    val database: TestpressDatabase
        get() = _database

    @Before
    fun initDb() {
        _database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TestpressDatabase::class.java
        )
            .setTransactionExecutor(Executors.newSingleThreadExecutor())
            .build()
    }

    @After
    fun closeDb() {
        _database.close()
    }
}