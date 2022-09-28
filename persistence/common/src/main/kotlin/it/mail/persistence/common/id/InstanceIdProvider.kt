package it.mail.persistence.common.id

fun interface InstanceIdProvider {

    fun getInstanceId(): Long
}
