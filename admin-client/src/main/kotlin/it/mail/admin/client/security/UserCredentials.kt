package it.mail.admin.client.security

import javax.inject.Singleton
import org.eclipse.microprofile.config.inject.ConfigProperty

@Singleton
class UserCredentials(
    @ConfigProperty(name = "admin.client.username") val username: String,
    @ConfigProperty(name = "admin.client.password") val password: CharArray,
) {
    fun areValid(username: String, password: CharArray) =
        this.username == username && this.password.contentEquals(password)
}
