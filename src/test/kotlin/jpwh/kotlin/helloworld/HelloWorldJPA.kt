package jpwh.kotlin.helloworld

import jpwh.kotlin.env.TransactionManagerTest
import jpwh.kotlin.model.helloworld.Message
import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import javax.persistence.Persistence

class HelloWorldJPA : TransactionManagerTest() {

    @Test
    fun storeLoadMessage() {
        val entityManagerFactory = Persistence.createEntityManagerFactory("HelloWorldPU")

        try {
            run {
                val transaction = TM.userTransaction
                transaction.begin()

                val entityManager = entityManagerFactory.createEntityManager()

                val message = Message(text = TEST_STRING)

                entityManager.persist(message)
                transaction.commit()
                entityManager.close()
            }

            run {
                val transaction = TM.userTransaction
                transaction.begin()

                val entityManager = entityManagerFactory.createEntityManager()

                val messages = entityManager
                    .createQuery("select m from Message m")
                    .resultList as List<Message>
                // SELECT * from MESSAGE


                assertEquals(messages.size, 1)
                assertEquals(messages[0].text, TEST_STRING)

                messages[0].text = "Take me to your leader!"

                transaction.commit()

                entityManager.close()
            }

        } finally {
            TM.rollback()
            entityManagerFactory.close()
        }
    }

    companion object {
        private const val TEST_STRING = "Hello World!"
    }
}