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
import java.io.IOException

class YourMom : Command(
    name = "joke",
    description = "Get a 'Your Mom' joke",
    alias = setOf(
        "yourmom", "urmom", "yomom",
        "yourmomma", "urmomma", "yomomma",
        "yourmama", "urmama", "yomama"
    )
) {
    private val API_URL = "https://api.yomomma.info/"

    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        event.channel.sendTyping().queue {
            HttpUtil.get(API_URL, cb = object : BaseCallback() {
                override fun onFailure(call: Call, e: IOException) {
                    super.onFailure(call, e)
                    fail()
                }

                override fun onResponse(call: Call, response: Response) {
                    super.onResponse(call, response)
                    val json = response.asJson()
                    if (!json.has("joke")) {
                        fail()
                    } else {
                        event.reply(json.getString("joke"))
                    }
                }

                fun fail() {
                    event.reply("Ur mom gay")
                }
            })
        }
    }
}
