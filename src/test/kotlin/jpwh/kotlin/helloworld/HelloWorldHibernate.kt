package jpwh.kotlin.helloworld

import jpwh.kotlin.env.TransactionManagerTest
import jpwh.kotlin.model.helloworld.Message
import org.hibernate.SessionFactory
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.boot.MetadataSources
import org.hibernate.cfg.Environment
import org.hibernate.resource.transaction.backend.jta.internal.JtaTransactionCoordinatorBuilderImpl
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class HelloWorldHibernate : TransactionManagerTest() {

//    fun simpleBoot() {
//        val sessionFactory = MetadataSources(
//            StandardServiceRegistryBuilder()
//                .configure("hibernate.cfg.xml")
//                .build()
//        ).buildMetadata().buildSessionFactory()
//    }

    private fun createSessionFactory(): SessionFactory {
        val serviceRegistryBuilder = StandardServiceRegistryBuilder()

        serviceRegistryBuilder
            .applySetting("hibernate.connection.datasource", "myDS")
            .applySetting("hibernate.format_sql", "true")
            .applySetting("hibernate.use_sql_comments", "true")
            .applySetting("hibernate.hbm2ddl.auto", "create-drop")

        serviceRegistryBuilder.applySetting(
            Environment.TRANSACTION_COORDINATOR_STRATEGY,
            JtaTransactionCoordinatorBuilderImpl::class.java
        )

        val serviceRegistry  = serviceRegistryBuilder.build()
        val metadataSources = MetadataSources(serviceRegistry)

        metadataSources.addAnnotatedClass(
            jpwh.kotlin.model.helloworld.Message::class.java
        )

        val metadataBuilder = metadataSources.metadataBuilder

        val metadata = metadataBuilder.build()

        assertEquals(metadata.entityBindings.size, 1)

        return metadata.buildSessionFactory()
    }

    @Test
    @Throws(Exception::class)
    fun storeLoadMessage() {
        val sessionFactory = createSessionFactory()
        try {
            run {
                /*
                Get access to the standard transaction API <code>UserTransaction</code> and
                begin a transaction on this thread of execution.
             */
                val transaction = TM.userTransaction
                transaction.begin()

                /*
                Whenever you call <code>getCurrentSession()</code> in the same thread you get
                the same <code>org.hibernate.Session</code>. It's bound automatically to the
                ongoing transaction and is closed for you automatically when that transaction
                commits or rolls back.
             */
                val session = sessionFactory.currentSession

                val message = Message(text = "Hello World!")

                /*
                The native Hibernate API is very similar to the standard Java Persistence API and most methods
                have the same name.
             */
                session.persist(message)

                /*
                Hibernate synchronizes the session with the database and closes the "current"
                session on commit of the bound transaction automatically.
             */
                transaction.commit()
                // INSERT into MESSAGE (ID, TEXT) values (1, 'Hello World!')
            }

            run {
                val transaction = TM.userTransaction
                transaction.begin()

                /*
                    A Hibernate criteria query is a type-safe programmatic way to express queries,
                    automatically translated into SQL.
                 */

                val session = sessionFactory.currentSession

                val builder = session.criteriaBuilder

                val criteria = builder.createQuery(Message::class.java)
                criteria.from(Message::class.java)

                val messages = session.createQuery(criteria).resultList

                // SELECT * from MESSAGE

                assertEquals(messages.size, 1)
                assertEquals(messages[0].text, "Hello World!")

                transaction.commit()
            }

        } finally {
            TM.rollback()
        }
    }
}