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

import me.din0s.const.Unicode
import me.din0s.deebot.entities.BaseCallback
import me.din0s.deebot.entities.Command
import me.din0s.deebot.reply
import me.din0s.deebot.util.HttpUtil
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import okhttp3.Call
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class DiscordStatus : Command(
    name = "discordstatus",
    description = "Get info about discord's servers' status",
    alias = setOf("api", "apistatus", "status")
) {
    private val BASE_URL = "https://srhpyqt94yxb.statuspage.io/api/v2/summary.json"
    private val STATUS_URL = "**https://status.discordapp.com**"
    private val componentSet = setOf("API", "Gateway", "CloudFlare", "Media Proxy", "Voice")

    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        event.channel.sendTyping().queue {
            HttpUtil.get(BASE_URL, cb = object : BaseCallback() {
                override fun onFailure(call: Call, e: IOException) {
                    super.onFailure(call, e)
                    event.reply("The status page is down as well... Tragedy!")
                }

                override fun onResponse(call: Call, response: Response) {
                    super.onResponse(call, response)
                    val json = response.asJson()
                    val sb = StringBuilder(STATUS_URL).append("\n")
                    val status = json.getJSONObject("status")
                    val description = status.getString("description")

                    // TODO: reformat as embed

                    // OVERVIEW
                    if (description.isNotOperational()) {
                        sb.append("_")
                            .append("                          ")
                            .append("~~~")
                            .append("                          ")
                            .append("_")
                            .append("\n")
                            .append(description)

                        val indicator = status.getString("indicator")
                        sb.addImpact(indicator).append("\n")
                    }
                    sb.append("\n")

                    // INCIDENTS
                    json.getJSONArray("incidents").forEach {
                        it as JSONObject
                        val name = it.getString("name")
                        sb.append("**~ ").append(name).append("**")
                        if (name.isNotOperational()) {
                            sb.addImpact(name).append(Unicode.EXCLAMATION)
                        }
                        sb.append("\n\n")
                    }

                    // COMPONENTS
                    json.getJSONArray("components")
                        .map {
                            it as JSONObject
                            Pair(it.getString("name"), it.getString("status"))
                        }
                        .filter { componentSet.contains(it.first) }
                        .forEach {
                            sb.append("__**").append(it.first).append("**__: ")
                            val componentStatus = it.second
                            sb.append(componentStatus.capitalize()).append(" ")
                            if (componentStatus == "operational") {
                                sb.append(Unicode.CHECK)
                            } else {
                                sb.append(Unicode.EXCLAMATION)
                            }
                            sb.append("\n")
                        }

                    if (sb.length <= 2000) {
                        event.reply(sb.toString())
                    } else {
                        event.reply("The detailed report is too long!\n Please visit $STATUS_URL")
                    }
                }
            })
        }
    }

    private fun String.isNotOperational() : Boolean {
        return !endsWith("Operational")
    }

    private fun StringBuilder.addImpact(indicator: String) : StringBuilder {
        append(" - `").append(indicator.capitalize()).append(" Impact`")
        return this
    }
}
