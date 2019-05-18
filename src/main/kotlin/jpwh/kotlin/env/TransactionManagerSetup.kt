package jpwh.kotlin.env

import bitronix.tm.TransactionManagerServices
import bitronix.tm.resource.jdbc.PoolingDataSource

import javax.naming.Context
import javax.naming.InitialContext
import javax.sql.DataSource
import javax.transaction.Status
import javax.transaction.UserTransaction
import java.util.logging.Logger

/**
 * Provides a database connection pool with the Bitronix JTA transaction
 * manager (http://docs.codehaus.org/display/BTM/Home).
 *
 *
 * Hibernate will look up the datasource and `UserTransaction` through
 * JNDI, that's why you also need a `jndi.properties` file. A minimal
 * JNDI context is bundled with and started by Bitronix.
 *
 */
open class TransactionManagerSetup @Throws(Exception::class)
@JvmOverloads constructor(
    val databaseProduct: DatabaseProduct,
    connectionURL: String? = null
) {

    private val namingContext: Context = InitialContext()
    private val datasource: PoolingDataSource

    val userTransaction: UserTransaction
        get() = try {
            namingContext.lookup("java:comp/UserTransaction") as UserTransaction
        } catch (ex: Exception) {
            throw RuntimeException(ex)
        }

    val dataSource: DataSource
        get() = try {
            namingContext.lookup(DATASOURCE_NAME) as DataSource
        } catch (ex: Exception) {
            throw RuntimeException(ex)
        }

    init {

        logger.fine("Starting database connection pool")

        logger.fine("Setting stable unique identifier for transaction recovery")
        TransactionManagerServices.getConfiguration().serverId = "myServer1234"

        logger.fine("Disabling JMX binding of manager in unit tests")
        TransactionManagerServices.getConfiguration().isDisableJmx = true

        logger.fine("Disabling transaction logging for unit tests")
        TransactionManagerServices.getConfiguration().journal = "null"

        logger.fine("Disabling warnings when the database isn't accessed in a transaction")
        TransactionManagerServices.getConfiguration().isWarnAboutZeroResourceTransaction = false

        logger.fine("Creating connection pool")
        datasource = PoolingDataSource()
        datasource.uniqueName = DATASOURCE_NAME
        datasource.minPoolSize = 1
        datasource.maxPoolSize = 5
        datasource.preparedStatementCacheSize = 10

        // Our locking/versioning tests assume READ COMMITTED transaction
        // isolation. This is not the default on MySQL InnoDB, so we set
        // it here explicitly.
        datasource.isolationLevel = "READ_COMMITTED"

        // Hibernate's SQL schema generator calls connection.setAutoCommit(true)
        // and we use auto-commit mode when the EntityManager is in suspended
        // mode and not joined with a transaction.
        datasource.allowLocalTransactions = true

        logger.info("Setting up database connection: $databaseProduct")
        databaseProduct.configuration.configure(datasource, connectionURL)

        logger.fine("Initializing transaction and resource management")
        datasource.init()
    }

    fun rollback() {
        val tx = userTransaction
        try {
            if (tx.status == Status.STATUS_ACTIVE || tx.status == Status.STATUS_MARKED_ROLLBACK) tx.rollback()
        } catch (ex: Exception) {
            System.err.println("Rollback of transaction failed, trace follows!")
            ex.printStackTrace(System.err)
        }

    }

    @Throws(Exception::class)
    fun stop() {
        logger.fine("Stopping database connection pool")
        datasource.close()
        TransactionManagerServices.getTransactionManager().shutdown()
    }



    companion object {
        const val DATASOURCE_NAME = "myDS"
        private val logger = logger<TransactionManagerSetup>()

        private inline fun <reified T> logger(): Logger {
            return Logger.getLogger(T::class.java.name)
        }
    }

}
