package io.mailit.persistence.postgresql

import io.mailit.persistence.test.DatabaseSchemaTest
import io.quarkus.test.junit.QuarkusTest

@QuarkusTest
class PostgresqlDatabaseSchemaTest : DatabaseSchemaTest(
    referenceSchemaName = "comparison_reference",
    referenceSchemaScriptLocation = "db/expected_schema.sql",
)
