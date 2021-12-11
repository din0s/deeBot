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

package me.din0s.sql.managers

import me.din0s.const.Regex
import me.din0s.deebot.entities.Timer
import me.din0s.exceptions.InvalidDurationException
import me.din0s.exceptions.RepeatFrequencyException
import me.din0s.exceptions.TooManyEntriesException
import me.din0s.sql.tables.Timers
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.User
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.DayOfWeek
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.abs

/**
 * Manages all timers (announcements/reminders).
 *
 * @author Dinos Papakostas
 */
object TimerManager : ISqlManager {
    private val log = LogManager.getLogger()

    private val MINUTES = setOf("m", "min", "mins", "minute", "minutes")
    private val HOURS = setOf("h", "hour", "hours")
    private val DAYS = setOf("d", "day", "days")
    private val WEEKS = setOf("w", "week", "weeks")
    private const val MAX_ENTRIES_PER_USER = 5

    private val userMap = mutableMapOf<Long, MutableSet<Timer>>()

    override fun postReady() {
        log.info("Loading Timers")
        transaction {
            Timers.selectAll().forEach {
                val id = it[Timers.uuid]
                val message = it[Timers.message]
                val userId = it[Timers.userId]
                val channelId = it[Timers.channelId]
                val private = it[Timers.private]
                var time = OffsetDateTime.parse(it[Timers.time])
                val period = Duration.ofDays(it[Timers.period] ?: 0)
                if (time.isBefore(OffsetDateTime.now())) {
                    if (period.isZero) {
                        Timers.deleteWhere { Timers.uuid eq id }
                        return@forEach
                    } else {
                        while (time.isBefore(OffsetDateTime.now())) {
                            time = time.plusDays(period.toDays())
                        }
                        Timers.update({ Timers.uuid eq id }) { entry ->
                            entry[Timers.time] = time.toString()
                        }
                    }
                }
                add(userId, Timer(id, message, userId, channelId, private, time, period))
            }
        }
    }

    /**
     * Creates a new timer based on the user's input.
     *
     * @param msg The message to display after the countdown ends.
     * @param user The user that created this timer.
     * @param channel The channel to send the message in.
     * @param durationStr A string that describes the duration of the timer.
     * @param repeat Whether the timer should repeat.
     * @param inPrivate Whether the reply should be send in private.
     * @return The timer, if one was successfully created.
     * @throws TooManyEntriesException If the user's timers count exceeds the allowance.
     * @throws InvalidDurationException If the duration string could not be parsed.
     */
    fun create(
        msg: String,
        user: User,
        channel: MessageChannel,
        durationStr: String,
        repeat: Boolean,
        inPrivate: Boolean
    ) : Timer {
        if ((userMap[user.idLong]?.size ?: 0) >= MAX_ENTRIES_PER_USER) {
            throw TooManyEntriesException
        }

        val (date, duration) = durationStr.parse(repeat)
        log.debug("Parsed date {}", date)
        return transaction {
            val entry = Timers.insert {
                it[message] = msg
                it[userId] = user.idLong
                it[channelId] = channel.idLong
                it[private] = inPrivate
                it[time] = date.toString()
                it[period] = duration.toDays()
            }
            val timer = Timer(entry[Timers.uuid], msg, user.idLong, channel.idLong, inPrivate, date, duration)
            add(user.idLong, timer)
            return@transaction timer
        }
    }

    /**
     * Returns the specified user's set of timers.
     *
     * @param user The user to get the timers of.
     * @return A set of all timers that the user currently has.
     * If none are set, this will be an empty set.
     */
    fun get(user: User) : Set<Timer> {
        return userMap[user.idLong] ?: emptySet()
    }

    /**
     * Deletes a timer from the user's set, matching the provided IDs.
     *
     * @param userId The user's ID to delete the timer for.
     * @param uuid The timer's ID which should be deleted.
     * @return True if the timer was found & deleted, false otherwise.
     */
    fun delete(userId: Long, uuid: Long) : Boolean  {
        val success = userMap[userId]?.removeIf { it.uuid == uuid } ?: false
        if (success) {
            transaction {
                Timers.deleteWhere { Timers.uuid eq uuid }
            }
        }
        return success
    }

    /**
     * Parses the string into a pair of datetime and a duration period.
     * These values are used to indicate when and how often a timer should be executed.
     *
     * @param repeat Whether this is a repeated timer or not.
     * @return A pair of [OffsetDateTime] and a [Duration],
     * for the timer's next occurrence and period respectively.
     * @throws InvalidDurationException If the string cannot be parsed to a pair.
     */
    private fun String.parse(repeat: Boolean) : Pair<OffsetDateTime, Duration> {
        if (matches(Regex.DURATION_RELATIVE)) {
            val groups = Regex.DURATION_RELATIVE.find(this)!!.groupValues
            log.trace("Found relative duration: {}", groups.drop(1))
            val amount = groups[1].toLong()
            val unit = groups[2].toUnit()

            if (repeat && unit == ChronoUnit.MINUTES) {
                throw RepeatFrequencyException
            }
            val period = when {
                repeat -> Duration.of(amount, unit)
                else -> Duration.ZERO
            }
            return Pair(OffsetDateTime.now().plus(amount, unit), period)
        } else if (matches(Regex.DURATION_ABSOLUTE)) {
            val groups = Regex.DURATION_ABSOLUTE.find(this)!!.groupValues
            log.trace("Found absolute duration: {}", groups.drop(1))
            val timezone = groups[5].toTimeZone()
            val now = OffsetDateTime.now(timezone)
            val dayOffset = groups[1].toDayOffset(now)
            val hour = groups[2].toInt()
            val mins = groups[3].toInt()
            val modifier = groups[4].lowercase()

            val isAm = modifier.startsWith('a')
            val isPm = modifier.startsWith('p')
            if ((isAm || isPm) && hour > 12) {
                throw InvalidDurationException
            }

            var time = now.withHour(hour).withMinute(mins).withSecond(0)
            if (isPm && hour != 12) {
                time = time.plusHours(12)
            } else if (isAm && hour == 12) {
                time = time.minusHours(12)
            }
            time = when {
                now.isAfter(time) && dayOffset == -1 -> time.plusDays(1)
                now.isAfter(time) && dayOffset == 0 -> time.plusDays(7)
                dayOffset == -1 -> time
                else -> time.plusDays(dayOffset.toLong())
            }

            val period = when {
                !repeat -> Duration.ZERO
                dayOffset == -1 -> Duration.ofDays(1)
                else -> Duration.ofDays(7)
            }
            return Pair(time, period)
        }
        throw InvalidDurationException
    }

    /**
     * Converts the string to its corresponding [ChronoUnit]
     *
     * @return The unit that matches the string.
     * @throws InvalidDurationException If the string doesn't contain a valid unit.
     */
    private fun String.toUnit() : ChronoUnit {
        val identifier = lowercase()
        return when {
            MINUTES.contains(identifier) -> ChronoUnit.MINUTES
            HOURS.contains(identifier) -> ChronoUnit.HOURS
            DAYS.contains(identifier) -> ChronoUnit.DAYS
            WEEKS.contains(identifier) -> ChronoUnit.WEEKS
            else -> throw InvalidDurationException
        }
    }

    /**
     * Converts the string to a timezone.
     *
     * @return The parsed timezone's ID,
     * or GMT if no timezone matches the input.
     */
    private fun String.toTimeZone() : ZoneId {
        return TimeZone.getTimeZone(uppercase()).toZoneId()
    }

    /**
     * Calculates the day offset between the given date and the day contained in the string.
     * e.g. If today is Monday and the String is 'Friday' then this returns 4.
     *
     * @param now The date to compare to, which should be [OffsetDateTime.now]
     * localized to the user's timezone.
     * @return The day offset, or -1 if no day is specified.
     * @throws InvalidDurationException If the string doesn't contain a valid day name.
     */
    private fun String.toDayOffset(now: OffsetDateTime) : Int {
        if (isEmpty()) {
            return -1
        }

        val currentDay = now.dayOfWeek
        return try {
            val targetDay = DayOfWeek.valueOf(trim().uppercase())
            abs(targetDay.value - currentDay.value)
        } catch (e: IllegalArgumentException) {
            throw InvalidDurationException
        }
    }

    /**
     * Adds a timer to the user's list in the internal mapping.
     *
     * @param userId The user's ID.
     * @param timer The timer to add.
     */
    private fun add(userId: Long, timer: Timer) {
        val set = when (val existing = userMap[userId]) {
            null -> {
                val createdSet = mutableSetOf<Timer>()
                userMap[userId] = createdSet
                createdSet
            }
            else -> existing
        }
        set.add(timer)
    }
}
