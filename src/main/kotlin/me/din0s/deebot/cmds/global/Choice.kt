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

class Choice : Command(
    name = "choice",
    description = "Select a random option out of the given choices",
    alias = setOf("choose", "select"),
    minArgs = 1,
    requiredParams = arrayOf("choices separated by ;"),
    examples = arrayOf("pizza; burger; cookies", "roblox; minecraft; fortnite")
) {
    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        val params = args.joinToString(" ")
        if (!params.contains(';') || params == ";") {
            // TODO: Usage msg
            return
        }

        val options = params.split(';').map { it.trim() }.filter { it.isNotEmpty() }
        when (options.size) {
            0 -> {} // TODO: Usage
            1 -> event.reply("Give me more options to choose from!")
            else -> {
                event.reply("\uD83E\uDD14 *I'd say..:* `${options.shuffled()[0]}`") // TODO: Strip formatting
            }
        }
    }
}
