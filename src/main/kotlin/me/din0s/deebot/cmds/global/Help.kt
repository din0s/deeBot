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

import me.din0s.const.Regex
import me.din0s.deebot.Config
import me.din0s.deebot.entities.Command
import me.din0s.deebot.entities.Registry
import me.din0s.deebot.managers.ServerManager
import me.din0s.deebot.paginate
import me.din0s.deebot.reply
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.function.UnaryOperator

class Help : Command(
    name = "help",
    description = "Display information about any command",
    alias = setOf("h", "commands", "cmds"),
    maxArgs =  1,
    optionalParams = arrayOf("command / page number"),
    examples = arrayOf("2", "choice")
) {
    private lateinit var helpPages : List<String>
    private val PAGE_SIZE = 5

    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        val prefix = when {
            event.isFromGuild -> ServerManager.get(event.guild.id)?.prefix ?: Config.defaultPrefix
            else -> Config.defaultPrefix
        }

        val sb = StringBuilder("```diff\n")
        val index = when {
            args.isEmpty() -> {
                sb.append(helpPages[0].withPrefix(prefix))
                1
            }
            args[0].matches(Regex.INTEGER) -> {
                val index = args[0].toInt()
                if (index > helpPages.size || index <= 0) {
                    event.reply("That help page doesn't exist!")
                    return
                }
                sb.append(helpPages[index - 1].withPrefix(prefix))
                index
            }
            else -> {
                val cmd = Registry.getCommand(args[0])
                if (cmd == null) {
                    event.reply("That command doesn't exist!")
                } else {
                    val info = StringBuilder()
                        .append("**")
                        .append(prefix)
                        .append(cmd.usage)
                        .append("**\n")
                        .append(cmd.description)
                        .append(".")
                    // TODO: add examples, flags, vars
                    event.reply(info.toString())
                }
                return
            }
        }
        if (index != helpPages.size) {
            sb.append("\nUse ${prefix}help ").append(index + 1).append(" to see the next page")
        }
        sb.append("\n```")
        event.reply(sb.toString())
    }

    fun generate() {
        helpPages = Registry.getCommands()
            .filter { !it.devOnly }
            .sortedBy { it.name }
            .paginate(UnaryOperator { it as Command
                "+ %PREFIX%${it.usage}\n- ${it.description}\n"
            }, PAGE_SIZE)
    }

    private fun String.withPrefix(prefix: String) : String {
        return replace("%PREFIX%", prefix)
    }

}
