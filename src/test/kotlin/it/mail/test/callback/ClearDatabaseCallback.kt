package it.mail.test.callback

import io.quarkus.arc.Arc
import io.quarkus.test.junit.callback.QuarkusTestAfterEachCallback
import io.quarkus.test.junit.callback.QuarkusTestMethodContext
import javax.sql.DataSource

class ClearDatabaseCallback : QuarkusTestAfterEachCallback {

    private val truncateAllScript: String = javaClass.getResource("/db/clear_all_tables.sql").readText()

    override fun afterEach(context: QuarkusTestMethodContext) {
        val datasource = Arc.container()
            .instance(DataSource::class.java)
            .get()

        datasource?.clear()
    }

    private fun DataSource.clear() {
        connection.use { conn ->
            conn.createStatement().use { statement ->
                statement.execute(truncateAllScript)
            }
        }
    }
}
