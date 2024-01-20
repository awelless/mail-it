package io.mailit.apikey.lang

@Suppress("UNCHECKED_CAST") // Only Result.failure is being casted.
inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>) =
    when (val value = getOrNull()) {
        null -> this as Result<R>
        else -> transform(value)
    }

inline fun <reified E : Throwable, T> Result<T>.mapError(transform: (E) -> Throwable) =
    when (val exception = exceptionOrNull()) {
        is E -> Result.failure(transform(exception))
        else -> this
    }

inline fun <T> Result<T>.ensure(condition: (T) -> Boolean, error: (T) -> Throwable) =
    when (val value = getOrNull()) {
        null -> this
        else -> if (condition(value)) this else Result.failure(error(value))
    }
