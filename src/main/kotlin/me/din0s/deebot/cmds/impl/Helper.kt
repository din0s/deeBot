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

package me.din0s.deebot.cmds.impl

import me.din0s.const.Regex
import me.din0s.deebot.cmds.Command
import me.din0s.util.*
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Collects all commands of a given type and creates
 * the help pages required to be retrieved instantly.
 *
 * @author Dinos Papakostas
 */
abstract class Helper(
    val cmdName: String,
    cmdDescription: String,
    cmdAlias: Set<String> = setOf(),
    cmdExamples: Array<String> = arrayOf(),
    path: String,
    clazz: Class<out Command>,
    filter: (T: Command) -> Boolean,
    val getCmd: (String) -> Command?
) : Command(
    name = cmdName,
    description = cmdDescription,
    alias = cmdAlias,
    maxArgs = 1,
    optionalParams = arrayOf("command / page number"),
    examples = cmdExamples
) {
    private val helpPages: List<String>
    private val pageSize = 5

    /**
     * Initialize the helper command by dynamically loading all of
     * the present commands and adding them to the paginated list.
     */
    init {
        helpPages = loadClasses(path, clazz)
            .filter { filter.invoke(it) }
            .paginate({
                "+ %PREFIX%${it.usage}\n- ${it.description}\n"
            }, pageSize)
    }

    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        val prefix = event.getPrefix()
        val sb = StringBuilder("```diff\n")
        val index = when {
            args.isEmpty() -> {
                sb.append(helpPages[0].withPrefix(prefix))
                1
            }
            args[0].matches(Regex.INTEGER) -> {
                val index = args[0].toInt()
                if (index > helpPages.size || index <= 0) {
                    event.reply("That help page doesn't exist! Valid range: [1-${helpPages.size}]")
                    return
                }
                sb.append(helpPages[index - 1].withPrefix(prefix))
                index
            }
            else -> {
                val cmd = getCmd(args[0])
                if (cmd == null) {
                    event.reply("That command doesn't exist!")
                } else {
                    event.showCommandHelp(cmd, prefix.escaped())
                }
                return
            }
        }
        if (index != helpPages.size) {
            sb.append("\nUse ")
                .append(prefix.noBackTicks())
                .append(cmdName)
                .append(" ")
                .append(index + 1)
                .append(" to see the next page (total: ")
                .append(helpPages.size)
                .append(")")
        }
        sb.append("\n```")
        event.reply(sb.toString())
    }

    /**
     * Replaces the prefix placeholder with the proper prefix for the server
     * that the command was called in.
     *
     * @param prefix The proper prefix to be used.
     * @return The String without the prefix placeholder and no back ticks,
     * in order to be used nicely in code blocks.
     */
    private fun String.withPrefix(prefix: String): String {
        return replace("%PREFIX%", prefix.noBackTicks())
    }

    /**
     * Replies to the user with the help message for the specified command.
     *
     * @param cmd The command to display help info for.
     * @param prefix The prefix to use for the server that the command was called in.
     */
    private fun MessageReceivedEvent.showCommandHelp(cmd: Command, prefix: String) {
        reply("**$prefix${cmd.usage}**\n${cmd.description}.\n${cmd.extras.replace("%PREFIX%", prefix)}")
    }
}
