package io.mailit.admin.console.security

import jakarta.inject.Singleton
import org.eclipse.microprofile.config.inject.ConfigProperty

@Singleton
class UserCredentials(
    @ConfigProperty(name = "admin.console.username") val username: String,
    @ConfigProperty(name = "admin.console.password") val password: CharArray,
) {
    fun areValid(username: String, password: CharArray) =
        this.username == username && this.password.contentEquals(password)
}
