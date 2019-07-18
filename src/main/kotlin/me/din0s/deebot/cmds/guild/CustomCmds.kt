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

package me.din0s.deebot.cmds.guild

import me.din0s.const.Regex
import me.din0s.deebot.entities.Command
import me.din0s.deebot.entities.CustomCommand
import me.din0s.deebot.entities.Registry
import me.din0s.deebot.managers.CustomCmdManager
import me.din0s.deebot.reply
import me.din0s.deebot.strip
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.apache.logging.log4j.LogManager

class CustomCmds : Command(
    name = "customcommands",
    description = "Create or manage a Custom Command for your server",
    alias = setOf("custom", "customcmds", "customcmd", "customcommand"),
    guildOnly = true,
    minArgs = 1,
    userPermissions = arrayOf(Permission.MANAGE_SERVER),
    requiredParams = arrayOf("create / list / modify / delete / resetALL"),
    flags = mapOf(
        Pair("private", "Reply in a private message"),
        Pair("delete", "Delete the original message")
    ),
    variables = mapOf(
        Pair("user", "The user's name"),
        Pair("userID", "The user's ID"),
        Pair("nickname", "The user's nickname"),
        Pair("input", "The user's command input"),
        Pair("mention", "The user as @mention")
    ),
    examples = arrayOf("create testCmd | Hi %user%!")
) {
    private val log = LogManager.getLogger(CustomCmds::class.java)
    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        when (args[0].toLowerCase()) {
            "create" -> {
                event.createOrEdit(args, true)
            }
            "list" -> {
                if (!CustomCmdManager.hasEntries(event.guild)) {
                    event.reply("There are no Custom Commands for this server!")
                    return
                }

                when (args.size) {
                    2 -> {
                        val list = CustomCmdManager.getByLabel(args[1], event.guild)
                        if (list != null) {
                            event.reply(
                                "**Command:** ${args[1].strip()}\n" +
                                        "Responses:\n" +
                                        "- ${list.joinToString("\n- ") { it.response } }"
                            )
                        } else {
                            event.reply("That Custom Command doesn't exist!")
                        }
                    }
                    1 -> {
                        val cmds = CustomCmdManager.getAll(event.guild)!!
                        event.reply("**Custom Commands:**\n${cmds.keys.joinToString(", ")}")
                    }
                    else -> {
                        // TODO: usage
                    }
                }
            }
            "modify", "update", "edit" -> {
                event.createOrEdit(args, false)
            }
            "delete" -> {
                if (args.size != 2) {
                    // TODO: usage
                    return
                }
                if (CustomCmdManager.delete(args[1], event.guild)) {
                    event.reply("*Successfully deleted the Custom Command!*")
                } else {
                    event.reply("That Custom Command doesn't exist!")
                }
            }
            "resetall", "deleteall" -> {
                if (CustomCmdManager.deleteAll(event.guild)) {
                    event.reply("**Deleted all Custom Commands!** \uD83D\uDE35")
                } else {
                    event.reply("There are no Custom Commands for this server!")
                }
            }
            "reload" -> {
                // TODO
            }
        }
    }

    private fun MessageReceivedEvent.createOrEdit(args: List<String>, create: Boolean) {
        val split = message.contentRaw.substringAfter(args[0]).trim().split(Regex.PIPE)
        if (split.size < 2) {
            // TODO: usage
            return
        }
        val label = split[0]
        var responses = split.drop(1)
        when {
            label.isEmpty() -> reply("**The command name cannot be empty!**")
            label.contains(' ') -> reply("**The command name cannot have spaces in it!**")
            Registry.getCommand(label) != null -> reply("**You cannot replace an existing global command!**")
            responses.isEmpty() -> reply("**You must include at least one response!**")
            responses.size == 1 && responses[0].equals("--private", true) -> reply("**You must specify the message to send!**")
            else -> {
                val existing = CustomCmdManager.getByLabel(label, guild) != null
                when {
                    existing && create -> {
                        reply("**That command already exists!**")
                        return
                    }
                    !existing && !create -> {
                        reply("**That command doesn't exist!**")
                        return
                    }
                    create -> {
                        // deleting old in order to save the updated one
                        CustomCmdManager.delete(label, guild)
                    }
                }

                var flags = 0
                var private = false
                var delete = false
                split.reversed().forEach {
                    if (!it.startsWith("--")) {
                        return@forEach
                    }
                    flags++
                    val flag = it.substring(2)
                    when (flag.toLowerCase()) {
                        "private" -> private = true
                        "delete" -> delete = true
                    }
                }

                val list = mutableListOf<CustomCommand>()
                responses = responses.dropLast(flags)
                responses.forEach {
                    list.add(CustomCommand(label, it, guild.idLong, private, delete))
                }
                CustomCmdManager.add(label, list, guild)
                log.debug("Cmd: {}, Responses: {}", label, responses)

                if (create) {
                    reply("Created command ${label.strip()}!")
                } else {
                    reply("Updated command ${label.strip()}!")
                }
            }
        }
    }
}
