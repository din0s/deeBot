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

import me.din0s.config.Config
import me.din0s.deebot.cmds.Command
import me.din0s.deebot.entities.BaseCallback
import me.din0s.util.HttpUtil
import me.din0s.util.escaped
import me.din0s.util.reply
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import okhttp3.Call
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Response
import org.apache.logging.log4j.LogManager
import org.json.JSONObject
import java.io.IOException

/**
 * Uses the Google Custom Search API to perform a search.
 * TODO: Think about scraping.
 *
 * @author Dinos Papakostas
 */
object Google : Command(
    name = "google",
    description = "Search for a term on Google",
    alias = setOf("g", "bing"),
    minArgs = 1,
    requiredParams = arrayOf("your query"),
    examples = arrayOf("elon musk")
) {
    private val log = LogManager.getLogger()
    private const val BASE_URL = "https://www.googleapis.com/customsearch/v1"

    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        val query = args.joinToString(" ")
        val url = BASE_URL.toHttpUrl().newBuilder()
            .addQueryParameter("q", query)
            .addQueryParameter("num", "1")
            .addQueryParameter("key", Config.googleKey)
            .addQueryParameter("cx", Config.googleSearch)
            .build()

        event.channel.sendTyping().queue {
            HttpUtil.get(url, cb = object : BaseCallback() {
                override fun onFailure(call: Call, e: IOException) {
                    super.onFailure(call, e)
                    event.reply("An error has occurred. Please try again later.")
                }

                override fun handleResponse(call: Call, response: Response) {
                    val json = response.asJson()
                    if (!json.has("items")) {
                        log.warn("Reached max searches.")
                        // TODO possibly better error msg
                        event.reply(
                            "__The bot has reached its maximum Google Searches for the day!__\n" +
                                "Please try again tomorrow..."
                        )
                        return
                    }

                    val results = json.getJSONArray("items")
                    if (results.length() == 0) {
                        event.reply("_Your query returned zero results from Google!_")
                        return
                    }
                    val firstResult = results[0] as JSONObject
                    val snippet = firstResult.getString("snippet").replace("\n", "").escaped()
                    val link = firstResult.getString("link")
                    val msg = "**`Google Search Result for:`** ${query.escaped()}\n<$link>\n$snippet"
                    event.reply(msg)
                }
            })
        }
    }
}
