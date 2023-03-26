package io.mailit.core.service.id

fun interface InstanceIdProvider {

    fun getInstanceId(): Int
}
