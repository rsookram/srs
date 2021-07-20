package io.github.rsookram.srs

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Implementation of a clock that always returns the user-defined instant. Similar to
 * [Clock.fixed], but allows the instant to be changed after creation.
 */
class AdjustableClock(var instant: Instant) : Clock() {

    override fun getZone(): ZoneId = ZoneOffset.UTC

    override fun withZone(zone: ZoneId?): Clock {
        require(zone == getZone()) { "Only UTC is supported. Given $zone" }
        return this
    }

    override fun instant(): Instant = instant
}
