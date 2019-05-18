package jpwh.kotlin.model.helloworld

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class Message(
    @Id
    @GeneratedValue
    var id: Long? = null,
    var text: String
)

