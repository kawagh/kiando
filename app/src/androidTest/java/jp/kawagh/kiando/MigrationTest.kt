package jp.kawagh.kiando

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import jp.kawagh.kiando.data.AppDatabase
import jp.kawagh.kiando.data.MIGRATION6to7
import jp.kawagh.kiando.data.MIGRATION8to9
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    @Throws(IOException::class)
    fun migrate6To7() {
        var db = helper.createDatabase(TEST_DB, 6).apply {
            // db has schema version 6. insert some data using SQL queries.
            // You cannot use DAO classes because they expect the latest schema.


            // Prepare for the next version.
            close()
        }

        // Re-open the database with version 7 and provide
        // migration6to7 as the migration process.
        db = helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION6to7)

        // MigrationTestHelper automatically verifies the schema changes,
        // but you need to validate that the data was migrated properly.
    }


    @Test
    @Throws(IOException::class)
    fun migrate8To9() {
        var db = helper.createDatabase(TEST_DB, 8).apply {
            close()
        }
        db = helper.runMigrationsAndValidate(TEST_DB, 9, true, MIGRATION8to9)
    }
}