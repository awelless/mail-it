package io.mailit.admin.console.http

import kotlin.math.max
import kotlin.math.min

const val PAGE_PARAM = "page"
const val SIZE_PARAM = "size"

private const val DEFAULT_PAGE = 0
private const val MIN_PAGE = 0
private const val MAX_PAGE = 1_000_000

private const val DEFAULT_SIZE = 10
private const val MIN_SIZE = 1
private const val MAX_SIZE = 1_000

fun normalizePage(page: Int?) = page?.let { putInRange(it, MIN_PAGE, MAX_PAGE) } ?: DEFAULT_PAGE
fun normalizeSize(size: Int?) = size?.let { putInRange(it, MIN_SIZE, MAX_SIZE) } ?: DEFAULT_SIZE

private fun putInRange(number: Int, minBorder: Int, maxBorder: Int) = max(min(number, maxBorder), minBorder)
