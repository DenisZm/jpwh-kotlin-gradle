package jpwh.kotlin.env

import org.testng.annotations.AfterSuite
import org.testng.annotations.BeforeSuite
import org.testng.annotations.Optional
import org.testng.annotations.Parameters

import java.util.Locale

/**
 * Starts and stops the transaction manager/database pool before/after a test suite.
 *
 *
 * All tests in a suite execute with a single [TransactionManagerSetup], call
 * the static [TransactionManagerTest.TM] in your test to access the JTA
 * transaction manager and database connections.
 *
 *
 *
 * The test parameters `database` (specifying a supported
 * [DatabaseProduct]) and a `connectionURL` are optional.
 * The default is an in-memory H2 database instance, created and destroyed
 * automatically for each test suite.
 *
 */
open class TransactionManagerTest {

    @Parameters("database", "connectionURL")
    @BeforeSuite
    @Throws(Exception::class)
    fun beforeSuite(
        @Optional database: String?,
        @Optional connectionURL: String
    ) {
        TM = TransactionManagerSetup(
            if (database != null)
                DatabaseProduct.valueOf(database.toUpperCase(Locale.US))
            else
                DatabaseProduct.H2,
            connectionURL
        )
    }

    @AfterSuite(alwaysRun = true)
    @Throws(Exception::class)
    fun afterSuite() {
        TM?.stop()
    }

    companion object {

        // Static single database connection manager per test suite
        var TM: TransactionManagerSetup? = null
    }
}
