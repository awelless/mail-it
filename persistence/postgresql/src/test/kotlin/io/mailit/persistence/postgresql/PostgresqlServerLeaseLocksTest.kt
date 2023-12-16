package io.mailit.persistence.postgresql

import io.mailit.persistence.test.ServerLeaseLocksTest
import io.quarkus.test.junit.QuarkusTest

@QuarkusTest
class PostgresqlServerLeaseLocksTest : ServerLeaseLocksTest()
