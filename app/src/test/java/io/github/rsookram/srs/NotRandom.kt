package io.github.rsookram.srs

import kotlin.random.Random

/**
 * A random number generator which always return 0 to remove randomness from tests.
 */
class NotRandom : Random() {

    override fun nextBits(bitCount: Int) = 0

    override fun nextLong(from: Long, until: Long): Long = 0
}
