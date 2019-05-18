package jpwh.kotlin.env

import bitronix.tm.resource.jdbc.PoolingDataSource

import java.util.Properties

enum class DatabaseProduct (
    var configuration: DataSourceConfiguration,
    var hibernateDialect: String
) {

    H2(
        object : DataSourceConfiguration {
            override fun configure(ds: PoolingDataSource, connectionURL: String?) {
                ds.className = "org.h2.jdbcx.JdbcDataSource"
                ds.driverProperties["URL"] = connectionURL ?: "jdbc:h2:mem:test"
                ds.driverProperties["user"] = "sa"
            }
        },
        jpwh.kotlin.shared.ImprovedH2Dialect::class.java.name
    ),

    ORACLE(
        object : DataSourceConfiguration {
            override fun configure(ds: PoolingDataSource, connectionURL: String?) {
                ds.className = "oracle.jdbc.xa.client.OracleXADataSource"
                ds.driverProperties["URL"] = connectionURL ?: "jdbc:oracle:thin:test/test@192.168.56.101:1521:xe"

                // Required for reading VARBINARY/LONG RAW columns easily, see
                // http://stackoverflow.com/questions/10174951
                val connectionProperties = Properties()
                connectionProperties["useFetchSizeWithLongColumn"] = "true"
                ds.driverProperties["connectionProperties"] = connectionProperties
            }
        },
        org.hibernate.dialect.Oracle10gDialect::class.java.name
    ),

    POSTGRESQL(
        object : DataSourceConfiguration {
            override fun configure(ds: PoolingDataSource, connectionURL: String?) {
                ds.className = "org.postgresql.xa.PGXADataSource"
                if (connectionURL != null) {
                    throw IllegalArgumentException(
                        "PostgreSQL XADataSource doesn't support connection URLs"
                    )
                }
                ds.driverProperties["serverName"] = "10.0.0.2"
                ds.driverProperties["databaseName"] = "test"
                ds.driverProperties["user"] = "test"
                ds.driverProperties["password"] = "test"
            }
        },
        org.hibernate.dialect.PostgreSQL82Dialect::class.java.name
    ),

    MYSQL(
        object : DataSourceConfiguration {
            override fun configure(ds: PoolingDataSource, connectionURL: String?) {
                // TODO: MySQL XA support is completely broken, we use the BTM XA wrapper
                //ds.setClassName("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
                ds.className = "bitronix.tm.resource.jdbc.lrc.LrcXADataSource"
                ds.driverProperties["url"] =
                    connectionURL ?: "jdbc:mysql://localhost/test?sessionVariables=sql_mode='PIPES_AS_CONCAT'"

                ds.driverProperties["driverClassName"] = "com.mysql.jdbc.Driver"
            }
        },
        // Yes, this should work with 5.6, no idea why Gail named it 5.7
        org.hibernate.dialect.MySQL57Dialect::class.java.name
    );

    interface DataSourceConfiguration {

        fun configure(ds: PoolingDataSource, connectionURL: String?)
    }

}
