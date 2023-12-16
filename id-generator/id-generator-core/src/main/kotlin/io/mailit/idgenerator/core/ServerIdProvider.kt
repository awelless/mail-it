package io.mailit.idgenerator.core

internal fun interface ServerIdProvider {

    fun getServerId(): Int
}
