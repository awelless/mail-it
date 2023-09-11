package io.mailit.persistence.mysql

import io.vertx.core.buffer.Buffer

internal fun ByteArray.toBuffer() = Buffer.buffer(this)
