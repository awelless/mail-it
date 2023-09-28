package io.mailit.admin.console.http

const val PAGE_PARAM = "page"
const val SIZE_PARAM = "size"

private const val DEFAULT_PAGE = 0
private const val MIN_PAGE = 0
private const val MAX_PAGE = 1_000_000

private const val DEFAULT_SIZE = 10
private const val MIN_SIZE = 1
private const val MAX_SIZE = 1_000

fun normalizePage(page: Int?) = when {
    page == null -> DEFAULT_PAGE
    page < MIN_PAGE -> MIN_PAGE
    page > MAX_PAGE -> MAX_PAGE
    else -> page
}

fun normalizeSize(size: Int?) = when {
    size == null -> DEFAULT_SIZE
    size < MIN_SIZE -> MIN_SIZE
    size > MAX_SIZE -> MAX_SIZE
    else -> size
}
