package io.mailit.core.service.test

import io.mailit.core.service.id.IdGenerator

object ConstantIdGenerator : IdGenerator {

    const val ID = 1234567890L

    override fun generateId() = ID
}
