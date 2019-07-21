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
import me.din0s.deebot.paginate
import me.din0s.deebot.reply
import me.din0s.deebot.strip
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.function.UnaryOperator

object BanList: Command(
    name = "banlist",
    description = "Get a list of all banned users",
    alias = setOf("bans"),
    maxArgs = 1,
    botPermissions = arrayOf(Permission.BAN_MEMBERS),
    userPermissions = arrayOf(Permission.BAN_MEMBERS),
    optionalParams = arrayOf("page")
) {
    private val USERS_PER_PAGE = 5
    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        event.guild.retrieveBanList().queue { bans ->
            if (bans.isEmpty()) {
                event.reply("*There are no banned users on this server!*")
                return@queue
            }
            val pages = bans.paginate(UnaryOperator { it as Guild.Ban
                "- ${it.user.asTag.strip()}\n"
            }, USERS_PER_PAGE)
            val page = when {
                args.isEmpty() -> 0
                else -> {
                    if (args[0].matches(Regex.INTEGER)) {
                        val num = args[0].toInt()
                        if (num in 1..pages.size) {
                            num - 1
                        } else {
                            -1
                        }
                    } else {
                        -1
                    }
                }
            }
            if (page == -1) {
                event.reply("**That's not a valid page index!** (range: 1 - ${pages.size})")
            } else {
                event.reply("__Banned Users__ (${page + 1}/${pages.size})\n${pages[page]}")
            }
        }
    }
}
