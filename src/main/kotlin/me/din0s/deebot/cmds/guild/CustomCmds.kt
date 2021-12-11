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
import me.din0s.const.Unicode
import me.din0s.deebot.cmds.Command
import me.din0s.deebot.entities.CustomCmdResponse
import me.din0s.deebot.handlers.CommandHandler
import me.din0s.sql.managers.CustomCmdManager
import me.din0s.sql.tables.Commands
import me.din0s.util.escaped
import me.din0s.util.getPrefix
import me.din0s.util.reply
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.apache.logging.log4j.LogManager

/**
 * Creates a new custom command for this guild.
 *
 * @author Dinos Papakostas
 */
object CustomCmds : Command(
    name = "customcmds",
    description = "Create or manage a Custom Command for your server",
    alias = setOf("custom", "customcommands", "customcmd", "customcommand"),
    guildOnly = true,
    minArgs = 1,
    userPermissions = arrayOf(Permission.MANAGE_SERVER),
    requiredParams = arrayOf("create / list / modify / delete / deleteALL"),
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
    examples = arrayOf(
        "create testCmd | Hi %user%! Flipped a coin: \$random{Heads; Tails}",
        "create multiple | This is random response 1 | This is random response 2",
        "delete oldCommand",
        "list"
    )
) {
    private val log = LogManager.getLogger()

    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        val prefix = event.getPrefix().escaped()
        when (args[0].lowercase()) {
            "create" -> {
                event.createOrEdit(args, true)
            }
            "list" -> {
                if (!CustomCmdManager.hasEntries(event.guild)) {
                    event.reply("There are no custom commands for this server!")
                    return
                }
                when (args.size) {
                    2 -> {
                        val list = CustomCmdManager.getByLabel(args[1], event.guild)
                        if (list != null) {
                            event.reply("""
                                **Command:** $prefix${args[1].escaped()}
                                Responses:
                                - ${list.joinToString("\n- ") { it.response }}
                            """.trimIndent())
                        } else {
                            event.reply("That custom command doesn't exist!")
                        }
                    }
                    1 -> {
                        val cmds = CustomCmdManager.getAll(event.guild)
                        event.reply("""
                            **Custom Commands:**
                            ${cmds.keys.joinToString(", ") { "$prefix$it" }}
                            
                            Use $prefix$name list [name] to find out more!
                        """.trimIndent())
                    }
                    else -> {
                        event.reply("**Usage:** $prefix$name list (command name / page)")
                    }
                }
            }
            "modify", "update", "edit" -> {
                event.createOrEdit(args, false)
            }
            "delete" -> {
                if (args.size != 2) {
                    event.reply("**Usage:** $prefix$name delete [command name]")
                    return
                }
                val success = CustomCmdManager.delete(args[1], event.guild)
                if (success) {
                    event.reply("*Successfully deleted that custom command!*")
                } else {
                    event.reply("That custom command doesn't exist!")
                }
            }
            "deleteall" -> {
                if (CustomCmdManager.deleteAll(event.guild)) {
                    event.reply("**Deleted all custom commands!** ${Unicode.DEAD}")
                } else {
                    event.reply("There are no custom commands for this server!")
                }
            }
            "reload" -> {
                // TODO
            }
        }
    }

    /**
     * Create or update an existing command, based on the args specified by the user.
     *
     * @param args All command arguments as given by the user.
     * @param create Whether to create a new command or to edit an existing one.
     */
    private fun MessageReceivedEvent.createOrEdit(args: List<String>, create: Boolean) {
        val split = message.contentRaw
            .substringAfter(args[0])
            .replace("\\|", Unicode.ZERO_WIDTH)
            .trim()
            .split(Regex.PIPE)
        if (split.size < 2) {
            val sub = when {
                create -> "create"
                else -> "modify"
            }
            reply("**Usage:** ${getPrefix().escaped()}$name $sub [command name] | [random responses separated by |]")
            return
        }
        val label = split[0]
        var responses = split.drop(1).filterNot { it.replace("\n", " ").isBlank() }
        when {
            label.isEmpty() -> reply("**The command name cannot be empty!**")
            label.contains(' ') -> reply("**The command name cannot have spaces in it!**")
            label.length > Commands.MAX_LABEL_LENGTH -> "**The command name cannot be longer than ${Commands.MAX_LABEL_LENGTH} characters!**"
            CommandHandler.getCommand(label) != null -> reply("**You cannot replace an existing global command!**")
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
                    when (flag.lowercase()) {
                        "private" -> private = true
                        "delete" -> delete = true
                    }
                }

                val list = mutableListOf<CustomCmdResponse>()
                responses = responses.dropLast(flags)
                responses.forEach {
                    list.add(CustomCmdResponse(it.replace(Unicode.ZERO_WIDTH, "|"), private, delete))
                }
                CustomCmdManager.add(label, list, guild)
                log.debug("Cmd: {}, Responses: {}", label, responses)

                if (create) {
                    reply("Created command ${label.escaped()}!")
                } else {
                    reply("Updated command ${label.escaped()}!")
                }
            }
        }
    }
}
