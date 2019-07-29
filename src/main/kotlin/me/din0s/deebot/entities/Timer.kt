/*
 * MIT License
 *
 * Copyright (c) 2019 Dinos Papakostas
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.din0s.deebot.entities

import me.din0s.deebot.handlers.TimerHandler
import me.din0s.sql.managers.TimerManager
import me.din0s.sql.tables.Timers
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Duration
import java.time.OffsetDateTime

/**
 * This class contains all info for a countdown timer,
 * including its ID, the message to send, the user's ID,
 * the channel's ID that the message will be sent in,
 * whether it's in private, the next datetime it will be executed,
 * and a period of when it should be repeated.
 *
 * @author Dinos Papakostas
 */
data class Timer(
    val uuid: Long,
    val message: String,
    val userId: Long,
    val channelId: Long,
    var private: Boolean,
    var next: OffsetDateTime,
    val period: Duration
) {
    private val log = LogManager.getLogger()

    /**
     * Upon creating a Timer object, schedule it according to
     * the next datetime that it should be executed at.
     */
    init {
        TimerHandler.schedule(this)
    }

    /**
     * Delete the timer object from the database.
     */
    fun deleteFromDb() {
        log.debug("Deleting T#{}", uuid)
        transaction {
            Timers.deleteWhere { Timers.uuid eq uuid }
        }
    }

    /**
     * Cancel a timer and remove it from the database.
     */
    fun cancel() {
        val success = TimerHandler.cancel(this)
        if (success) {
            log.debug("Cancelling T#{}", uuid)
            TimerManager.delete(userId, uuid)
        } else {
            log.warn("Tried to cancel already executed T#{}", uuid)
        }
    }

    /**
     * Reschedule the timer based on the period specified.
     */
    fun reschedule() {
        val days = period.toDays()
        log.debug("Rescheduling T#{} for {} day(s)", uuid, days)
        next = next.plusDays(days)
        transaction {
            Timers.update({ Timers.uuid eq uuid }) { entry ->
                entry[time] = next.toString()
            }
        }
    }
}
