package jpwh.kotlin.env

import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeClass
import org.testng.annotations.BeforeMethod

import java.io.*

/**
 * Starts and stops the JPA environment before/after a test class.
 *
 *
 * Create a subclass to write unit tests. Access the `EntityManagerFactory`
 * with [JPATest.JPA] and create `EntityManager` instances.
 *
 *
 *
 * Drops and creates the SQL database schema of the persistence unit before and after
 * every test method. This means your database will be cleaned for every test method.
 *
 *
 *
 * Override the [.configurePersistenceUnit] method to provide a custom
 * persistence unit name or additional `hbm.xml` file names to load for
 * your test class.
 *
 *
 *
 * Override the [.afterJPABootstrap] method to execute operations before the
 * test method but after the `EntityManagerFactory` is ready. At this point
 * you can create an `EntityManager` or `Session#doWork(JDBC)`. If
 * cleanup is needed, override the [.beforeJPAClose] method.
 *
 */
class JPATest : TransactionManagerTest() {

    private var persistenceUnitName: String? = null
    private var hbmResources: Array<String>? = null
    private var JPA: JPASetup? = null

    @BeforeClass
    @Throws(Exception::class)
    fun beforeClass() {
        configurePersistenceUnit()
    }

    @Throws(Exception::class)
    fun configurePersistenceUnit() {
        configurePersistenceUnit(null)
    }

    @Throws(Exception::class)
    fun configurePersistenceUnit(
        persistenceUnitName: String?,
        vararg hbmResources: String
    ) {
        this.persistenceUnitName = persistenceUnitName
        this.hbmResources = hbmResources as Array<String>
    }

    @BeforeMethod
    @Throws(Exception::class)
    fun beforeMethod() {
        JPA = JPASetup(TM!!.databaseProduct, persistenceUnitName, hbmResources)
        // Always drop the schema, cleaning up at least some of the artifacts
        // that might be left over from the last run, if it didn't cleanup
        // properly
        JPA!!.dropSchema()

        JPA!!.createSchema()
        afterJPABootstrap()
    }

    @Throws(Exception::class)
    fun afterJPABootstrap() {
    }

    @AfterMethod(alwaysRun = true)
    @Throws(Exception::class)
    fun afterMethod() {
        if (JPA != null) {
            beforeJPAClose()
            if ("true" != System.getProperty("keepSchema")) {
                JPA!!.dropSchema()
            }
            JPA!!.entityManagerFactory.close()
        }
    }

    @Throws(Exception::class)
    fun beforeJPAClose() {

    }

    @Throws(IOException::class)
    private fun copy(input: Reader, output: Writer): Long {
        val buffer = CharArray(4096)
        var count: Long = 0
        var n = 0
        while (-1 != n) {
            n = input.read(buffer)
            output.write(buffer, 0, n)
            count += n.toLong()
        }
        return count
    }

    @Throws(IOException::class)
    private fun getTextResourceAsString(resource: String): String {
        val inputStream = this.javaClass.classLoader.getResourceAsStream(resource)
            ?: throw IllegalArgumentException("Resource not found: $resource")
        val stringWriter = StringWriter()
        copy(InputStreamReader(inputStream), stringWriter)
        return stringWriter.toString()
    }

    private fun unwrapRootCause(throwable: Throwable): Throwable? {
        return unwrapCauseOfType(throwable, null)
    }

    private fun unwrapCauseOfType(throwable: Throwable, type: Class<out Throwable>?): Throwable? {
        var throwable = throwable
        var current: Throwable? = throwable
        while (current != null) {
            if (type != null && type.isAssignableFrom(current.javaClass))
                return current
            throwable = current
            current = current.cause
        }
        return throwable
    }
}
