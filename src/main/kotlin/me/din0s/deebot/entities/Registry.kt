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

package me.din0s.deebot.entities

import me.din0s.const.Regex
import me.din0s.deebot.Bot
import me.din0s.deebot.Config
import me.din0s.deebot.cmds.global.Help
import me.din0s.deebot.managers.ServerManager
import me.din0s.deebot.reply
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.apache.logging.log4j.LogManager
import org.reflections.Reflections
import java.lang.reflect.Modifier

object Registry : ListenerAdapter() {
    private val commandList: Map<String, Command>
    private val log = LogManager.getLogger(Registry::class.java)
    private val prefix = Config.defaultPrefix

    init {
        val cmds = mutableMapOf<String, Command>()
        Reflections("me.din0s.deebot.cmds")
            .getSubTypesOf(Command::class.java)
            .filter { !Modifier.isAbstract(it.modifiers) }
            .forEach {
                val cmd = it.getDeclaredConstructor().newInstance()
                cmds[cmd.name] = cmd
                log.trace("{} -> {}", cmd, cmd.name)

                cmd.alias.forEach { alias ->
                    cmds[alias] = cmd
                    log.trace("{} -> {}", cmd, alias)
                }
            }
        commandList = cmds
        log.debug("Registered {} commands", commandList.size)

        val help = cmds["help"] as Help
        help.generate()
    }

    fun getCommands() : Set<Command> {
        return commandList.values.toSet()
    }

    fun getCommand(label: String) : Command? {
        return commandList[label.toLowerCase()]
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot) return

        val rawMessage = event.message.contentRaw
        val serverPrefix = when {
            event.isFromGuild -> ServerManager.get(event.guild.id)?.prefix ?: prefix
            else -> prefix
        }

        if (rawMessage.startsWith(serverPrefix) && rawMessage.length > serverPrefix.length) {
            val allArgs = rawMessage.substring(serverPrefix.length).split(Regex.WHITESPACE)
            val label = allArgs[0].toLowerCase()
            val command = commandList[label] ?: return

            if (log.isTraceEnabled) {
                val (origin, location) = when {
                    event.isFromGuild -> Pair("G", "%${event.guild.name}#${event.textChannel.name}")
                    else -> Pair("DM", event.author.asTag)
                }
                log.trace("{}[{}] Received command {}", origin, location, rawMessage)
            }

            if (command.guildOnly && !event.isFromGuild) {
                event.reply("**This command can only be used in a server!**")
                return
            }

            val isDev = event.author.idLong == Bot.DEV_ID
            if (command.devOnly && !isDev) {
                return
            }
            // TODO: if blacklisted
            if (event.isFromGuild && !event.textChannel.canTalk()) {
                return
            }

            if (
                !isDev && event.isFromGuild
                && command.userPermissions.any { !event.member!!.permissions.contains(it) }
            ) {
                event.reply("**You do not have the required permissions to execute this command!**\n" +
                        "Required Permissions: `${command.userPermissions.joinToString(", ")}`")
                return
            }
            if (
                event.isFromGuild
                && command.botPermissions.any { !event.guild.selfMember.permissions.contains(it) }
            ) {
                event.reply("**The bot does not have the required permissions to execute this command!**\n" +
                        "*Please contact the server's admins if you believe this is an error!*\n" +
                        "Required Permissions: `${command.botPermissions.joinToString(", ")}`")
                return
            }

            val commandArgs = allArgs.drop(1)
            if (commandArgs.size < command.minArgs) {
                event.reply("**Incorrect Usage!** You used less command arguments than expected.")
                // TODO: Add usage message as well
                return
            }
            if (commandArgs.size > command.maxArgs) {
                event.reply("**Incorrect Usage!** You used more command arguments than expected.")
                // TODO: Add usage
                return
            }

            command.execute(event, commandArgs)
        }
    }
}
