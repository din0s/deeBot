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
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import okhttp3.Call
import okhttp3.Response
import org.apache.logging.log4j.LogManager
import java.io.IOException

/**
 * Polls a Chuck Norris API for a random fact.
 *
 * @author Dinos Papakostas
 */
object ChuckNorris : Command(
    name = "chucknorris",
    description = "Gets the random fact about Chuck Norris",
    alias = setOf("chuck", "norris")
) {
    private val log = LogManager.getLogger()
    private const val BASE_URL = "http://api.icndb.com/jokes/random"

    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        event.channel.sendTyping().queue {
            HttpUtil.get(BASE_URL, cb = object : BaseCallback() {
                override fun onFailure(call: Call, e: IOException) {
                    super.onFailure(call, e)
                    noChuck()
                }

                override fun handleResponse(call: Call, response: Response) {
                    val json = response.asJson()

                    if (json.has("value") && json.getJSONObject("value").has("joke")) {
                        val joke = json.getJSONObject("value")
                            .getString("joke")
                            .replace("&quot;", "\"")
                        event.reply(joke)
                    } else {
                        log.warn("Response from API didn't contain a value!\n{}", json)
                        noChuck()
                    }
                }

                fun noChuck() {
                    event.reply("*There was an issue with the Chuck Norris database, please try again later.*")
                }
            })
        }
    }
}
