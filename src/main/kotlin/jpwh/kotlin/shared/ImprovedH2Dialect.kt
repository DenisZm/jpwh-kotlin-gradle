package jpwh.kotlin.shared

import org.hibernate.dialect.H2Dialect
import org.hibernate.dialect.function.StandardSQLFunction
import org.hibernate.type.StandardBasicTypes

class ImprovedH2Dialect : H2Dialect() {
    init {
        // Demonstration of function customization in SQL dialect
        registerFunction("lpad", StandardSQLFunction("lpad", StandardBasicTypes.STRING))
    }
}
