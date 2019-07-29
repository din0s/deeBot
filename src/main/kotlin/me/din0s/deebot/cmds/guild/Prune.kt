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
import me.din0s.deebot.cmds.Command
import me.din0s.util.matchUserIds
import me.din0s.util.reply
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.apache.logging.log4j.LogManager
import java.time.OffsetDateTime

/**
 * Deletes an amount of recent messages,
 * optionally from a specific user.
 *
 * @author Dinos Papakostas
 */
object Prune : Command(
    name = "prune",
    description = "Delete an amount of recent messages",
    alias = setOf("purge", "deletemessages", "clearmessages"),
    guildOnly = true,
    minArgs = 1,
    botPermissions = arrayOf(Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY),
    userPermissions = arrayOf(Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY),
    requiredParams = arrayOf("amount"),
    optionalParams = arrayOf("user"),
    flags = mapOf(Pair("silent", "Skip the success message after pruning")),
    examples = arrayOf("25", "50 dinos#0649")
) {
    private val log = LogManager.getLogger()

    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        if (!args[0].matches(Regex.INTEGER)) {
            event.reply("**That's not a valid amount of messages!**")
            return
        }
        val msgCount = args[0].toInt()
        if (msgCount !in 1..100) {
            event.reply("**Please choose an amount between 1 and 100 messages!**")
            return
        }
        event.channel.getHistoryBefore(event.messageId, msgCount).queue {
            val userArgs = event.message.contentRaw
                .substringAfter(args[0])
                .substringBeforeLast("--silent")
                .trim()
            val hasUsers = userArgs.isNotBlank()
            val history = it.retrievedHistory
            val messages = mutableSetOf<Message>()
            val hasOld = if (hasUsers) {
                val users = event.jda.shardManager!!.matchUserIds(userArgs)
                val userHistory = history.filter { m -> m.author.id in users }.toList()
                !messages.addIfAllowed(userHistory)
            } else {
                !messages.addIfAllowed(history)
            }
            val size = messages.size
            if (size == 100) {
                event.message.delete().queue()
            } else {
                messages.add(event.message)
            }
            val silent = args.last().equals("--silent", true)
            if (messages.size == 1) {
                messages.first().delete().queue {
                    if (!silent) {
                        if (hasOld) {
                            event.reply("""
                                ${event.author.asMention}: *No messages were deleted.
                                Discord doesn't allow bulk deletion of messages older than 2 weeks!*
                            """.trimIndent())
                        } else {
                            event.reply("${event.author.asMention}: *No messages were deleted.*")
                        }
                    }
                }
            } else {
                log.info("Deleting {} messages in G#{}", size, event.guild.id)
                event.textChannel.deleteMessages(messages).queue {
                    if (!silent) {
                        when (size) {
                            1 -> event.reply("${event.author.asMention}: *Successfully deleted 1 message.*")
                            else -> event.reply("${event.author.asMention}: *Successfully deleted $size messages.*")
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds the messages of the target collection to this one,
     * if allowed by the date filter.
     *
     * @param target The collection that contains all messages found.
     * @return True if all messages were added, false if at least one was skipped.
     */
    private fun MutableSet<Message>.addIfAllowed(target: Collection<Message>) : Boolean {
        val allowedDate = OffsetDateTime.now().minusWeeks(2)
        target.filter { m -> m.timeCreated.isAfter(allowedDate) }.forEach { m-> add(m) }
        return size == target.size
    }
}
