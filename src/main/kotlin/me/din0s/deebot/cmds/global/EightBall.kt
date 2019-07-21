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

package me.din0s.deebot.cmds.global

import me.din0s.const.Unicode
import me.din0s.deebot.entities.Command
import me.din0s.deebot.reply
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import kotlin.random.Random

class EightBall : Command(
    name = "8ball",
    description = "Ask a question to the almighty 8ball",
    minArgs = 1,
    requiredParams = arrayOf("your question"),
    examples = arrayOf("Will I win the lottery?", "Am I the greatest?")
) {
    private val replies = listOf(
        "Yes, definitely!",
        "No way..",
        "Absolutely!",
        "Absolutely.. not.",
        "Most likely.",
        "Very doubtful :<",
        "Don't count on it.",
        "My sources say no.",
        "I have my doubts..",
        "That's a terrible idea..",
        "As I see it, yes ^.^",
        "I wouldn't say so.",
        "It's scientifically proven!"
    )

    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        val questionOriginal = args.joinToString(" ").replace("`", "")
        val questionFinal = when {
            questionOriginal.endsWith('?') -> questionOriginal
            else -> "$questionOriginal?"
        }
        val reply = replies.random()
        val emoji1 = when (Random.nextBoolean()) {
            true -> Unicode.START_CENTER
            else -> Unicode.START_CIRLE
        }
        val emoji2 = when (Random.nextBoolean()) {
            true -> Unicode.EIGHT_BALL
            else -> Unicode.GLASS_BALL
        }
        val msg = "$emoji1 `${questionFinal.capitalize()}` $reply $emoji2"
        if (msg.length <= 2000) {
            event.reply(msg)
        } else {
            event.reply("*Your question is too long to be answered by the 8ball!*")
        }
    }
}
