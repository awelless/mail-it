package io.mailit.idgenerator.test

import io.mailit.idgenerator.api.IdGenerator

class ConstantIdGenerator(private val id: Long) : IdGenerator {

    override fun generateId() = id
}
