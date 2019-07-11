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

import me.din0s.deebot.entities.Command
import me.din0s.deebot.reply
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class Lenny : Command(
    name = "lenny",
    description = "Get a random lennyface",
    alias = setOf("lennyface")
) {
    private val faces = setOf(
        "( \u0361\u00b0 \u035c\u0296 \u0361\u00b0)",
        "( \u0361\u2609 \u035c\u0296 \u0361\u2609)",
        "[ \u0361\u00b0 \u035c\u0296 \u0361\u00b0 ]",
        "(\u0e07 \u0361\u00b0 \u035c\u0296 \u0361\u00b0)\u0e07",
        "\u2514[ \u0361\u00b0 \u035c\u0296 \u0361\u00b0]\u2518",
        "\u1559( \u0361\u00b0 \u035c\u0296 \u0361\u00b0)\u1557",
        "\u4e41( \u0361\u00b0 \u035c\u0296 \u0361\u00b0)\u310f",
        "\u30fd( \u0361\u00b0 \u035c\u0296 \u0361\u00b0) \uff89",
        "( \u0361\u00b0 \u035c\u0296 \u0361\u00b0)>\u2310\u25a0-\u25a0",
        "[\u0332\u0305$\u0332\u0305(\u0332\u0305 \u0361\u00b0 \u035c\u0296 \u0361\u00b0\u0332\u0305)\u0332\u0305$\u0332\u0305]"
    )
    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        event.reply(faces.random())
    }
}