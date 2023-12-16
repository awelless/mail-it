package io.mailit.idgenerator.api

interface IdGenerator {

    /**
     * Generates a new id. The id is guaranteed to be unique across all servers in the same cluster.
     */
    fun generateId(): Long
}
