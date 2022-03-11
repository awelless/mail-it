package it.mail.service.model

data class Slice<T>(
        val content: List<T>,
        val page: Int,
        val size: Int,
) {

    fun <S> map(mapper: (T) -> S) = Slice(content.map(mapper), page, size)
}