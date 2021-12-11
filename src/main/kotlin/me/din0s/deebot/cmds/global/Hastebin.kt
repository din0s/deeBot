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
import me.din0s.deebot.entities.BaseCallback
import me.din0s.util.HttpUtil
import me.din0s.util.reply
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import okhttp3.Call
import okhttp3.Response
import org.apache.logging.log4j.LogManager
import org.json.JSONObject
import java.io.IOException

/**
 * Creates a new paste using the HasteBin API.
 *
 * @author Dinos Papakostas
 */
object Hastebin : Command(
    name = "hastebin",
    description = "Upload a text snippet to Hastebin",
    alias = setOf("hb", "haste", "paste", "pastebin"),
    minArgs = 1,
    requiredParams = arrayOf("your text/code snippet"),
    optionalParams = arrayOf("language flag"),
    flags = mapOf(Pair("language", "the programming language to display the color formatting for")),
    examples = arrayOf("not my password", "print('hi') --py")
) {
    private val log = LogManager.getLogger()
    private const val BASE_URL = "https://hastebin.com/documents"
    private val VALID_FLAGS = setOf("-l", "--lang", "--language")

    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        val (paste, flag) = parseArgs(args)

        event.channel.sendTyping().queue {
            HttpUtil.post(BASE_URL, paste, cb = object : BaseCallback() {
                override fun onFailure(call: Call, e: IOException) {
                    super.onFailure(call, e)
                    failed()
                }

                override fun handleResponse(call: Call, response: Response) {
                    val json = response.asJson()
                    if (!json.has("key")) {
                        log.warn("Response from API didn't contain a key!\n{}", json)
                        failed()
                        return
                    }
                    if (event.isFromGuild && event.guild.selfMember.hasPermission(event.textChannel, Permission.MESSAGE_MANAGE)) {
                        event.message.delete().queue {
                            success(json, flag)
                        }
                    } else {
                        success(json, flag)
                    }
                }

                fun failed() {
                    event.reply("There was an error uploading your snippet, please try again later.")
                }

                fun success(json: JSONObject, flag: String?) {
                    val ext = when (flag) {
                        null -> "txt"
                        else -> flag
                    }
                    event.reply("${event.author.asMention}: https://hastebin.com/${json.getString("key")}.$ext")
                }
            })
        }
    }

    /**
     * Parse the input in order to find a language flag, either as the
     * first or the last element of the command's arguments.
     *
     * @param args The command's argument list.
     * @return A pair of the normal paste content and an optional language flag.
     */
    private fun parseArgs(args: List<String>) : Pair<String, String?> {
        if (args.size > 1) {
            if (VALID_FLAGS.contains(args[0].lowercase())) {
                return Pair(args.drop(2).joinToString(" "), args[1])
            }
            if (VALID_FLAGS.contains(args[args.size - 2].lowercase())) {
                return Pair(args.dropLast(2).joinToString(" "), args.last())
            }
        }
        return Pair(args.joinToString(" "), null)
    }
}
