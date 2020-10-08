package `in`.testpress.core.database

import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.roommigration.RoomMigration4To5.MIGRATION_4_5
import `in`.testpress.database.roommigration.RoomMigration5To6.MIGRATION_5_6
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomMigrationsTest {

    @Rule @JvmField
    val helper: MigrationTestHelper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
            TestpressDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory())

    private val MIGRATIONS = arrayOf(MIGRATION_4_5, MIGRATION_5_6)

    private val testPressDatabase = "testpress-database"

    private val appDb: TestpressDatabase = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            TestpressDatabase::class.java,
            testPressDatabase)
            .addMigrations(*MIGRATIONS).build()

    private val databaseCurrentVersion = appDb.openHelper.readableDatabase.version

    @Test
    fun migrationShouldRunSuccessfully() {
        assertDoesNotThrow {
            createDatabaseWithPreviousVersion()
            createDatabaseWithLatestVersion()
        }
    }

    private fun createDatabaseWithPreviousVersion() {
        helper.createDatabase(testPressDatabase, databaseCurrentVersion - 1).apply {
            close()
        }
    }

    private fun createDatabaseWithLatestVersion() {
        helper.createDatabase(testPressDatabase, databaseCurrentVersion).apply {
            close()
        }
    }
}
