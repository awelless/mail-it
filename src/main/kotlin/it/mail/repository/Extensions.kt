package it.mail.repository

import io.quarkus.hibernate.orm.panache.kotlin.PanacheQuery
import io.quarkus.hibernate.orm.panache.kotlin.runtime.KotlinJpaOperations

internal fun <E : Any> PanacheQuery<E>.withFetchGraph(graphName: String): PanacheQuery<E> {
    val entityGraph = KotlinJpaOperations.INSTANCE.entityManager.getEntityGraph(graphName)
    return withHint("javax.persistence.fetchgraph", entityGraph)
}
