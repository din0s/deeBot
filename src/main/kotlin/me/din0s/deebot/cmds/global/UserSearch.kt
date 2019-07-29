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

import me.din0s.deebot.cmds.Command
import me.din0s.util.escaped
import me.din0s.util.getAllArgs
import me.din0s.util.matchUsers
import me.din0s.util.reply
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Searches for a user throughout all running instances.
 *
 * @author Dinos Papakostas
 */
object UserSearch : Command(
    name = "find",
    description = "Find all users with a given name",
    alias = setOf("usersearch", "search"),
    minArgs = 1,
    requiredParams = arrayOf("name"),
    examples = arrayOf("dinos#0649")
) {
    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        val name = event.getAllArgs()
        val list = event.jda.shardManager!!.matchUsers(name).map { it.discriminator }.sorted().toList()
        when {
            list.isNullOrEmpty() -> event.reply("*No users matched that criteria!*")
            list.size == 1 -> event.reply("__Found 1 user__: ${name.escaped()}#${list[0]}")
            else -> event.reply("__Found ${list.size} users.__\nTags: ```py\n${list.joinToString(", ")}\n```")
        }
    }
}
