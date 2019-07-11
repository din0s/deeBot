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

package me.din0s.deebot

import me.din0s.deebot.entities.Registry
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.lang.StringBuilder
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.function.UnaryOperator

fun MessageChannel.send(msg: String) {
    sendMessage(msg).queue()
}

fun MessageReceivedEvent.reply(msg: String) {
    if (isFromGuild && !textChannel.canTalk()) {
        author.openPrivateChannel().queue {
            it.send(msg)
        }
    } else {
        channel.send(msg)
    }
}

fun User.whisper(msg: String) {
    openPrivateChannel().queue {
        it.send(msg)
    }
}

fun Member.canDelete(textChannel: TextChannel) : Boolean {
    return hasPermission(textChannel, Permission.MESSAGE_MANAGE)
}

fun Collection<Any>.paginate(str: UnaryOperator<Any>, size: Int) : List<String> {
    var i = 0
    val pages = mutableListOf<String>()
    val sb = StringBuilder()

    forEach {
        if (i != 0) {
            if (i % size == 0) {
                pages.add(sb.toString())
                sb.clear()
            } else {
                sb.append("\n")
            }
        }

        sb.append(str.apply(it))
        i++
    }

    if (sb.isNotEmpty()) {
        pages.add(sb.toString())
    }
    return pages
}

fun Long.asTime() : String {
    var time = this
    val days = TimeUnit.MILLISECONDS.toDays(time)
    time -= TimeUnit.DAYS.toMillis(days)

    val hours = TimeUnit.MILLISECONDS.toHours(time)
    time -= TimeUnit.HOURS.toMillis(hours)

    val mins = TimeUnit.MILLISECONDS.toMinutes(time)
    time -= TimeUnit.MINUTES.toMillis(mins)

    val secs = TimeUnit.MILLISECONDS.toSeconds(time)
    val sb = StringBuilder()
    for (unit in setOf(Pair(days, "day"), Pair(hours, "hour"), Pair(mins, "minute"), Pair(secs, "second"))) {
        if (unit.first > 0) {
            sb.append(unit.first).append(" ").append(unit.second)
            if (unit.first > 1) {
                sb.append("s")
            }
            sb.append(" ")
        }
    }
    return sb.toString()
}
