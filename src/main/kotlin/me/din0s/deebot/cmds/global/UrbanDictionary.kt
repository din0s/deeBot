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

import me.din0s.deebot.entities.BaseCallback
import me.din0s.deebot.entities.Command
import me.din0s.deebot.reply
import me.din0s.deebot.util.HttpUtil
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import okhttp3.Call
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class UrbanDictionary : Command(
    name = "define",
    description = "Look up a word or a phrase on UrbanDictionary",
    alias = setOf("ud", "definition", "dictionary", "urban", "urbandictionary"),
    minArgs = 1,
    requiredParams = arrayOf("word or phrase")
) {
    private val BASE_URL = "https://api.urbandictionary.com/v0/define?term="

    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        val allArgs = args.joinToString(" ").replace("+", "%2B")
        val url = BASE_URL + URLEncoder.encode(allArgs, StandardCharsets.UTF_8.displayName())
        event.channel.sendTyping().queue {
            HttpUtil.get(url, cb = object : BaseCallback() {
                override fun onFailure(call: Call, e: IOException) {
                    super.onFailure(call, e)
                    event.reply("Uh oh, there was an error reaching the UrbanDictionary API!")
                }

                override fun onResponse(call: Call, response: Response) {
                    super.onResponse(call, response)
                    val json = response.asJson()
                    if (!json.has("list") || json.getJSONArray("list").isEmpty) {
                        event.reply("Your search returned no results! Zero.")
                        return
                    }

                    val result = json.getJSONArray("list")
                        .map { it as JSONObject }
                        .sortedBy {
                            return@sortedBy it.getInt("thumbs_up") - it.getInt("thumbs_down")
                        }.last()
                    val word = result.getString("word")
                    val def = result.getString("definition")
                    val example = result.getString("example")
                    val up = result.getInt("thumbs_up")
                    val down = result.getInt("thumbs_down")

                    val sb = StringBuilder("__**`-=UrbanDictionary: ").append(word).append("=-`**__\n")
                        .append("\n**Definition:**\n").append(def.noBrackets()).append("\n")
                    if (!example.isEmpty()) {
                        sb.append("\n**Example:**\n").append(example.noBrackets()).append("\n")
                    }
                    sb.append("\n[**+**] ").append(up).append(" / [**-**] ").append(down)

                    if (sb.length > 2000) {
                        event.reply("The response is too big!\nPlease click here: <${result.getString("permalink")}>")
                    } else {
                        event.reply(sb.toString())
                    }
                }
            })
        }
    }

    private fun String.noBrackets() : String {
        return this.replace("[", "").replace("]", "")
    }
}
