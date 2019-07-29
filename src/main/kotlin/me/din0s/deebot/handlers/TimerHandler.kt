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

package me.din0s.deebot.handlers

import me.din0s.deebot.Bot
import me.din0s.deebot.entities.Timer
import me.din0s.util.asTime
import net.dv8tion.jda.api.entities.MessageChannel
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * Handles the exection and scheduling of timers.
 *
 * @see [Timer]
 * @author Dinos Papakostas
 */
object TimerHandler {
    private val log = LogManager.getLogger()
    private val scheduler = Executors.newScheduledThreadPool(1)
    private val tasks = mutableMapOf<Long, ScheduledFuture<*>>()

    /**
     * Schedule a timer to be executed either once or repeatedly,
     * depending on its period field.
     *
     * @param timer The timer to schedule.
     */
    fun schedule(timer: Timer) {
        getChannel(timer) ?: return
        val delay = ChronoUnit.SECONDS.between(OffsetDateTime.now(), timer.next)
        transaction {
            tasks[timer.uuid] = when {
                timer.period.isZero -> {
                    log.info(
                        "Scheduling #{} in {}",
                        timer.uuid, (delay * 1000).asTime()
                    )

                    scheduler.schedule({
                        log.info("Executed #{}", timer.uuid)
                        sendWithSuccess(timer)
                        timer.deleteFromDb()
                    }, delay, TimeUnit.SECONDS)
                }
                else -> {
                    log.info(
                        "Scheduling #{} in {} to run every {} day(s)",
                        timer.uuid, (delay * 1000).asTime(), timer.period.toDays()
                    )

                    scheduler.scheduleWithFixedDelay({
                        if (sendWithSuccess(timer)) {
                            log.info("Executed #{}", timer.uuid)
                            timer.reschedule()
                        } else {
                            timer.deleteFromDb()
                        }
                    }, delay, timer.period.seconds, TimeUnit.SECONDS)
                }
            }
        }
    }

    /**
     * Cancel an already scheduled timer.
     *
     * @param timer The timer to cancel.
     * @return True if the timer was cancelled.
     * A timer cannot be cancelled if it has been executed already,
     * or if it hasn't been scheduled at all.
     */
    fun cancel(timer: Timer) : Boolean {
        val task = tasks.remove(timer.uuid) ?: return false
        return task.cancel(false)
    }

    /**
     * Get the [MessageChannel] that the timer's message should be sent in.
     *
     * @param timer The timer to get the channel for.
     * @return The channel for this timer, or null if it couldn't be found.
     */
    private fun getChannel(timer: Timer) : MessageChannel? {
        val channel = when (timer.private) {
            true -> Bot.SHARD_MANAGER.getPrivateChannelById(timer.channelId)
            false -> Bot.SHARD_MANAGER.getTextChannelById(timer.channelId)
        }
        if (channel == null) {
            timer.deleteFromDb()
        }
        return channel
    }

    /**
     * Attempts to send the message and returns whether it was successful or not.
     *
     * @param timer The timer to send the message of.
     * @return True if the channel was found and the message was sent, false otherwise.
     */
    private fun sendWithSuccess(timer: Timer) : Boolean {
        val channel = getChannel(timer) ?: return false
        channel.sendMessage(timer.message).queue()
        return true
    }
}
