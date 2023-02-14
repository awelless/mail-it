package io.mailit.persistence.common

import io.mailit.core.model.Slice

/**
 * Creates [Slice] from selected [content].
 *
 * Note: to properly calculate [Slice.last] property,
 * [size] + 1 [content] element should be initially requested from a database
 */
fun <T> createSlice(content: List<T>, page: Int, size: Int) = Slice(
    content = content.take(size),
    page = page,
    size = size,
    last = content.size <= size,
)
