package io.mailit.apikey.spi.security

interface SecureRandom {

    fun generateInts(sequenceSize: Int, lowerBound: Int = 0, upperBound: Int): Sequence<Int>
}
