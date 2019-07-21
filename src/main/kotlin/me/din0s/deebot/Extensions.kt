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

import me.din0s.const.Regex
import me.din0s.const.Unicode
import me.din0s.deebot.entities.Command
import me.din0s.deebot.managers.ServerManager
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.sharding.ShardManager
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.util.function.UnaryOperator

fun MessageChannel.send(msg: String) {
    val safeMsg = msg.replace("@everyone", "@${Unicode.ZWSP}everyone")
        .replace("@here", "@${Unicode.ZWSP}here")
    sendMessage(safeMsg).queue()
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

fun MessageReceivedEvent.showUsage(cmd: Command) {
    val prefix = when {
        isFromGuild -> ServerManager.get(guild.id)?.prefix ?: Config.defaultPrefix
        else -> Config.defaultPrefix
    }
    reply("**Usage:** ${prefix.strip()}${cmd.usage}")
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

fun Guild.getUser(name: String) : List<User> {
    val user = when {
        name.matches(Regex.USER_TAG) -> getMemberByTag(name)?.user
        name.matches(Regex.DISCORD_ID) -> getMemberById(name)?.user
        else -> null
    }
    return if (user == null) {
        val byName = members.filter { it.user.name.equals(name, true) }.map { it.user }.toList()
        return if (byName.isEmpty()) {
            members.filter { it.nickname?.equals(name, true) ?: false }.map { it.user }.toList()
        } else {
            byName
        }
    } else {
        listOf(user)
    }
}

fun ShardManager.getUser(name: String) : List<User> {
    val user = when {
        name.matches(Regex.USER_TAG) -> getUserByTag(name)
        name.matches(Regex.DISCORD_ID) -> getUserById(name)
        else -> null
    }
    return if (user == null) {
        users.filter { it.name.equals(name, true) }.toList()
    } else {
        listOf(user)
    }
}

fun OffsetDateTime.format() : String {
    return format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + " UTC"
}

fun String.strip() : String {
    return replace("*", "\\*")
        .replace("`", "\\`")
        .replace("_", "\\_")
        .replace("~~", "\\~\\~")
        .replace(">", "\u180E>")
}
