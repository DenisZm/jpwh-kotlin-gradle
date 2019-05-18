package jpwh.kotlin.env

import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.Persistence
import java.util.HashMap

/**
 * Creates an EntityManagerFactory.
 *
 *
 * Configuration of the persistence units is taken from `META-INF/persistence.xml`
 * and other sources. Additional `hbm.xml` file names can be given to the
 * constructor.
 *
 */
class JPASetup @Throws(Exception::class)
constructor(
    databaseProduct: DatabaseProduct,
    private val persistenceUnitName: String?,
    vararg hbmResources: Array<String>?
) {
    private val properties: MutableMap<String, String> = HashMap()
    val entityManagerFactory: EntityManagerFactory

    init {

        // No automatic scanning by Hibernate, all persistence units list explicit classes/packages
        properties["hibernate.archive.autodetection"] = "none"

        // Really the only way how we can get hbm.xml files into an explicit persistence
        // unit (where Hibernate scanning is disabled)
        properties["hibernate.hbmxml.files"] = hbmResources.joinToString()

        // We don't want to repeat these settings for all units in persistence.xml, so
        // they are set here programmatically
        properties["hibernate.format_sql"] = "true"
        properties["hibernate.use_sql_comments"] = "true"

        // Select database SQL dialect
        properties["hibernate.dialect"] = databaseProduct.hibernateDialect

        entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName, properties)
    }

    fun createEntityManager(): EntityManager {
        return entityManagerFactory.createEntityManager()
    }

    fun createSchema() {
        generateSchema("create")
    }

    fun dropSchema() {
        generateSchema("drop")
    }

    private fun generateSchema(action: String) {
        // Take exiting EMF properties, override the schema generation setting on a copy
        val createSchemaProperties = HashMap(properties)
        createSchemaProperties["javax.persistence.schema-generation.database.action"] = action
        Persistence.generateSchema(persistenceUnitName, createSchemaProperties)
    }
}
