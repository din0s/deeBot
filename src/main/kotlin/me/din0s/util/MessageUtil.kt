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

package me.din0s.util

import me.din0s.const.Unicode
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Responds to the user with the supplied message.
 * Optionally handles the exploit of @everyone or @here.
 *
 * @param msg The message to send.
 * @param safe Whether all instances of mass mentions should be stripped.
 */
fun MessageReceivedEvent.reply(msg: String, safe: Boolean = true) {
    if (isFromGuild && !textChannel.canTalk()) {
        author.whisper(msg)
    } else {
        channel.send(msg, safe)
    }
}

/**
 * Sends a message in the channel.
 * Optionally handles the exploit of @everyone or @here.
 *
 * @param msg The message to send.
 * @param safe Whether all instances of mass mentions should be stripped.
 */
fun MessageChannel.send(msg: String, safe: Boolean) {
    val message = when {
        safe -> msg
            .replace("@everyone", "@${Unicode.ZERO_WIDTH}everyone")
            .replace("@here", "@${Unicode.ZERO_WIDTH}here")
        else -> msg
    }
    sendMessage(message).queue()
}

/**
 * Opens a private channel with the user and sends them a message.
 *
 * @param msg The message to send.
 */
fun User.whisper(msg: String) {
    openPrivateChannel().queue { it.send(msg, false) }
}
