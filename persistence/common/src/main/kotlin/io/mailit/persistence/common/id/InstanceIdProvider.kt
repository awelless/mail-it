package io.mailit.persistence.common.id

fun interface InstanceIdProvider {

    fun getInstanceId(): Long
}
