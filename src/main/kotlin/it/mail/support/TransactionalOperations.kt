package it.mail.support

import javax.enterprise.context.ApplicationScoped
import javax.transaction.UserTransaction

@ApplicationScoped
class TransactionalOperations(
    private val transaction: UserTransaction,
) {

    fun doWithinTransaction(action: () -> Unit) {
        try {
            transaction.begin()
            action()
            transaction.commit()
        } catch (e: Exception) {
            transaction.rollback()
            throw e
        }
    }

    fun <T> getWithinTransaction(action: () -> T): T {
        try {
            transaction.begin()
            val returnValue = action()
            transaction.commit()

            return returnValue
        } catch (e: Exception) {
            transaction.rollback()
            throw e
        }
    }
}
