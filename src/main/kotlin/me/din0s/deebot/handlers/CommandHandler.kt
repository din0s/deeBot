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

package me.din0s.deebot.handlers

import me.din0s.const.Regex
import me.din0s.deebot.Bot
import me.din0s.deebot.cmds.Command
import me.din0s.sql.managers.BlacklistManager
import me.din0s.util.*
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.apache.logging.log4j.LogManager

/**
 * This is the main [Command] handler.
 * It performs all checks for whether the command should be executed,
 * and responds accordingly if something has gone wrong.
 *
 * @author Dinos Papakostas
 */
object CommandHandler : ListenerAdapter() {
    private val log = LogManager.getLogger()
    private lateinit var commandList: Map<String, Command>

    /**
     * Initialize all commands and add them to the internal mapping.
     * If needed, specifically call a commands init() function too.
     */
    fun init() {
        val reflections = loadClasses("me.din0s.deebot.cmds", Command::class.java)
        commandList = reflections.register(log)
        log.debug("Registered {} commands", reflections.size)
        commandList.values.toSet().forEach { it.postRegister() }
        log.info("Commands READY!")
    }

    /**
     * Returns a command, if such exists, from the internal map.
     *
     * @param label The command label to look up for.
     * @return The command that fires for the specified label,
     * or null if one doesn't exist.
     */
    fun getCommand(label: String) : Command? {
        return commandList[label.lowercase()]
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot) {
            return
        }
        val prefix = event.getPrefix()
        val rawMessage = event.message.contentRaw
        if (rawMessage == event.jda.selfUser.asMention) {
            event.reply("The prefix is: ${prefix.escaped()}")
        } else if (rawMessage.startsWith(prefix) && rawMessage.length > prefix.length) {
            val allArgs = rawMessage.substring(prefix.length).split(Regex.WHITESPACE)
            val label = allArgs[0].lowercase()
            val command = commandList[label] ?: return

            if (log.isTraceEnabled) {
                val origin = when {
                    event.isFromGuild -> "TC#${event.channel.id}"
                    else -> "DM#${event.author.id}"
                }
                log.trace("{} - {}: {}", origin, event.author.asTag, rawMessage)
            }

            if (command.guildOnly && !event.isFromGuild) {
                event.reply("**This command can only be used in a server!**")
                return
            }
            val isDev = event.author.idLong == Bot.DEV_ID
            if (command.devOnly && !isDev) {
                log.trace("{} tried to run {}!", event.author.asTag, command.name)
                return
            }
            if (command.name != "blacklist" && event.isFromGuild && BlacklistManager.isBlacklisted(event.channel.idLong)) {
                log.debug("Received command in blacklisted channel, ignoring.")
                return
            }
            if (event.isFromGuild && !event.textChannel.canTalk()) {
                log.debug("Cannot talk in channel!")
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
            if (commandArgs.size < command.minArgs || commandArgs.size > command.maxArgs) {
                event.showUsage(command)
            } else {
                try {
                    command.execute(event, commandArgs)
                } catch (e: Exception) {
                    val cause = e.cause?.message ?: e.message
                    Bot.DEV.whisper("$cause\n${event.author.asTag}: $rawMessage")
                    event.reply("Oh no, something went wrong! The dev was notified.")
                    log.error(cause)
                    e.printStackTrace()
                }
            }
        }
    }
}
