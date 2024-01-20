package io.mailit.apikey.fake

import io.mailit.apikey.spi.security.SecureRandom

object ConstantSecuredRandom : SecureRandom {

    override fun generateInts(sequenceSize: Int, lowerBound: Int, upperBound: Int): Sequence<Int> {
        var counter = 0
        return generateSequence {
            if (counter == sequenceSize) {
                null
            } else {
                counter++
                lowerBound
            }
        }
    }
}
