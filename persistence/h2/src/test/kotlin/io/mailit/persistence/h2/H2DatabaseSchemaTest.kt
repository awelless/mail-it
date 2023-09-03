package io.mailit.persistence.h2

import io.mailit.persistence.test.DatabaseSchemaTest
import io.quarkus.test.junit.QuarkusTest

@QuarkusTest
class H2DatabaseSchemaTest : DatabaseSchemaTest(
    referenceSchemaName = "COMPARISON_REFERENCE",
    referenceSchemaScriptLocation = "db/expected_schema.sql",
)
