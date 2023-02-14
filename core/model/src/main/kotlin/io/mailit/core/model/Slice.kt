package io.mailit.core.model

data class Slice<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val last: Boolean,
) {

    fun <S> map(mapper: (T) -> S) = Slice(
        content = content.map(mapper),
        page = page,
        size = size,
        last = last,
    )
}
