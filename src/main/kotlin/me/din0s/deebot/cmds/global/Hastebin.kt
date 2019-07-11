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

import me.din0s.deebot.canDelete
import me.din0s.deebot.entities.BaseCallback
import me.din0s.deebot.entities.Command
import me.din0s.deebot.reply
import me.din0s.deebot.util.HttpUtil
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import okhttp3.Call
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class Hastebin : Command(
    name = "hastebin",
    description = "Upload a text snippet to Hastebin",
    alias = setOf("hb"),
    minArgs = 1,
    requiredParams = arrayOf("your text/code snippet"),
    optionalParams = arrayOf("language flag"),
    flags = mapOf(Pair("language", "the programming language to display the color formatting for")),
    examples = arrayOf("not my password", "print('hi') --py")
) {
    private val BASE_URL = "https://hastebin.com/documents"
    private val VALID_FLAGS = setOf("-l", "--lang", "--language")
    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        val flag = getLang(args) ?: "txt'"
        HttpUtil.get(BASE_URL, cb = object : BaseCallback() {
            override fun onFailure(call: Call, e: IOException) {
                super.onFailure(call, e)
                failed()
            }

            override fun onResponse(call: Call, response: Response) {
                super.onResponse(call, response)
                val json = response.asJson()
                if (!json.has("key")) {
                    failed()
                    return
                }
                if (event.isFromGuild && event.guild.selfMember.canDelete(event.textChannel)) {
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

            fun success(json: JSONObject, flag: String) {
                event.reply("${event.author.asMention}: https://hastebin.com/${json.getString("key")}.$flag")
            }
        })
    }

    private fun getLang(args: List<String>) : String? {
        if (args.size < 2) {
            return null
        }
        if (VALID_FLAGS.contains(args[0].toLowerCase())) {
            return args[1]
        }
        if (VALID_FLAGS.contains(args[args.size - 2].toLowerCase())) {
            return args.last()
        }
        return null
    }
}