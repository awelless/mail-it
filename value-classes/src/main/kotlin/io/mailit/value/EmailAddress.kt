package io.mailit.value

@JvmInline
value class EmailAddress private constructor(val email: String) {

    companion object {
        private val emailAddressRegex = Regex("[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+")

        fun String.toEmailAddress() =
            if (matches(emailAddressRegex)) {
                EmailAddress(this)
            } else {
                throw IllegalArgumentException("Invalid email format")
            }
    }
}
