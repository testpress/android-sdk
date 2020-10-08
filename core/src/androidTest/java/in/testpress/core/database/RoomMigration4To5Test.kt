package `in`.testpress.core.database

import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.roommigration.RoomMigration4To5
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


@RunWith(AndroidJUnit4::class)
class RoomMigration4To5Test {

    private val testPressDatabase = "testpress-database"

    private val ALL_MIGRATIONS = arrayOf(RoomMigration4To5.MIGRATION_4_5)

    @Rule @JvmField
    val helper: MigrationTestHelper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
            TestpressDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory())


    @Test
    @Throws(IOException::class)
    fun migrateAll() {
        val db: SupportSQLiteDatabase = helper.createDatabase(testPressDatabase, 4)
        db.close()

        val appDb: TestpressDatabase = Room.databaseBuilder(
                InstrumentationRegistry.getInstrumentation().targetContext,
                TestpressDatabase::class.java,
                testPressDatabase)
                .addMigrations(*ALL_MIGRATIONS).build()
        appDb.openHelper.writableDatabase
        appDb.close()
    }
}
