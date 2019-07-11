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
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

abstract class Command(
    val name: String,
    val description: String,
    val alias: Set<String> = emptySet(),

    val guildOnly: Boolean = false,
    val devOnly: Boolean = false,
    val minArgs: Int = 0,
    val maxArgs: Int = Integer.MAX_VALUE,
    val botPermissions: Array<Permission> = Permission.EMPTY_PERMISSIONS,
    val userPermissions: Array<Permission> = Permission.EMPTY_PERMISSIONS,

    val requiredParams: Array<String> = arrayOf(),
    val optionalParams: Array<String> = arrayOf(),
    val flags: Map<String, String> = emptyMap(),
    val variables: Map<String, String> = emptyMap(),
    val examples: Array<String> = arrayOf()
) {
    val usage by lazy {
        val sb = StringBuilder(name)
        if (requiredParams.isNotEmpty()) {
            sb.append(" [").append(requiredParams.joinToString(" ")).append("]")
        }
        if (optionalParams.isNotEmpty()) {
            sb.append(" (").append(optionalParams.joinToString(" ")).append(")")
        }
        sb.toString()
    }

    abstract fun execute(event: MessageReceivedEvent, args: List<String>)

    protected fun MessageReceivedEvent.parseFlags(boolean: Boolean = false) : Map<String, String?> {
        val args = message.contentRaw.split(Regex.WHITESPACE).drop(1)
        if (args.isEmpty()) {
            return emptyMap()
        }

        val map = mutableMapOf<String, String?>()
        args.forEachIndexed { index, arg ->
            val key = when {
                arg.startsWith("-") && arg.length == 2 -> "${arg[1]}"
                arg.startsWith("--") && arg.length > 2 -> arg.substring(2)
                else -> return@forEachIndexed
            }
            map[key] = when {
                boolean || index == args.size - 1 -> null
                else -> args[index + 1]
            }
        }
        return map
    }

    override fun toString() : String {
        return javaClass.simpleName
    }
}
