/*
 * MIT License
 *
 * Copyright (c) 2020 Dinos Papakostas
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

import me.din0s.const.Regex
import me.din0s.deebot.cmds.Command
import me.din0s.deebot.entities.BaseCallback
import me.din0s.util.*
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import okhttp3.Call
import okhttp3.Response
import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.function.Function

/**
 * Uses the MemeGen API to create a custom meme.
 *
 * @author Dinos Papakostas
 */
object Meme : Command(
    name = "meme",
    description = "Generate a meme using a template",
    alias = setOf("memegen", "memegenerator"),
    minArgs = 1,
    botPermissions = arrayOf(Permission.MESSAGE_ATTACH_FILES),
    requiredParams = arrayOf("type | top line | bottom line"),
    examples = arrayOf("xy | generate | all the memes!", "kermit", "list")
) {
    private val log = LogManager.getLogger()
    private val helpCmds = setOf("help", "list", "templates", "info")
    private val templateHelp = mutableMapOf<String, String>()
    private lateinit var templatePages: List<String>
    private lateinit var templates: Set<String>

    private const val URL = "https://memegen.link/"
    private const val GENERATOR_URL = "$URL%s/%s/%s.jpg"
    private const val TEMPLATE_URL = "${URL}api/templates/"
    private const val PAGE_SIZE = 10

    override fun postRegister() {
        HttpUtil.get(TEMPLATE_URL, cb = object : BaseCallback() {
            override fun onFailure(call: Call, e: IOException) {
                super.onFailure(call, e)
                log.error("Could not load meme templates!")
                templatePages = emptyList()
            }

            override fun handleResponse(call: Call, response: Response) {
                val json = response.asJson()
                val set = mutableSetOf<String>()

                templatePages = json.keySet()
                    .sorted()
                    .filter { it.isNotBlank() }
                    .paginate(Function {
                        val template = json.getString(it).substringAfterLast("/")
                        set.add(template)
                        "+ $it ($template)\n"
                    }, PAGE_SIZE)
                templates = set

                templates.forEach {
                    HttpUtil.get("$TEMPLATE_URL$it", cb = object : BaseCallback() {
                        override fun handleResponse(call: Call, response: Response) {
                            val exampleJson = response.asJson()
                            if (!exampleJson.has("example")) {
                                return
                            }
                            val example = exampleJson.getString("example").substringAfter("/api/templates/")
                            templateHelp[it] = "$URL$example.jpg"

                            if (templateHelp.size == templates.size) {
                                log.info("Loaded all template help URLs")
                            }
                        }
                    })
                }
            }
        })
    }

    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        val prefix = event.getPrefix().escaped()
        if (helpCmds.contains(args[0].toLowerCase())) {
            val arg1 = args[0].toLowerCase()
            if (templates.contains(arg1)) {
                event.sendExample(arg1)
                return
            }

            val index = when {
                args.size == 2 && args[1].matches(Regex.INTEGER) -> args[1].toInt()
                else -> 1
            }

            if (index > templatePages.size || index <= 0) {
                event.reply("That's not a valid page index!")
                return
            }

            val sb = StringBuilder("```diff\n")
                .append(templatePages[index - 1])

            if (index != templatePages.size) {
                sb.append("\nUse ${prefix}meme list ${index + 1} to see the next page")
            }

            sb.append("\n```")
            event.reply(sb.toString())
            return
        }
        val allArgs = event.getAllArgs()
        val template = args[0].toLowerCase()
        if (!allArgs.contains('|')) {
            if (templates.contains(template)) {
                event.sendExample(template)
            } else {
                event.reply("**That's not a valid template!**\nUse ${prefix}meme list to see the full template list.")
            }
        } else {
            val lines = allArgs.split(Regex.PIPE).drop(1)
            if (lines.size > 2) {
                event.showUsage(this)
            } else {
                if (!templates.contains(template)) {
                    event.reply("**That's not a valid template!**\nUse ${prefix}meme list to see the full template list.")
                    return
                }
                val top = lines[0].encode()
                val bot = when {
                    lines.size == 1 -> "_"
                    else -> lines[1].encode()
                }

                event.channel.sendTyping().queue {
                    HttpUtil.get(GENERATOR_URL.format(template, top, bot), cb = object : BaseCallback() {
                        override fun onFailure(call: Call, e: IOException) {
                            super.onFailure(call, e)
                            event.reply("Something went wrong! Please try again later.")
                        }

                        override fun handleResponse(call: Call, response: Response) {
                            event.channel.sendMessage(event.author.asMention)
                                .addFile(response.toBytes(), "meme.jpg")
                                .queue()
                        }
                    })
                }
            }
        }
    }

    /**
     * Send an example usage of the template specified.
     *
     * @param template The name of the template to use.
     */
    private fun MessageReceivedEvent.sendExample(template: String) {
        if (!templateHelp.containsKey(template)) {
            reply("*The example for this meme type is currently unavailable.*")
            return
        }
        channel.sendTyping().queue {
            HttpUtil.get(templateHelp[template]!!, cb = object : BaseCallback() {
                override fun onFailure(call: Call, e: IOException) {
                    super.onFailure(call, e)
                    reply("Something went wrong! Please try again later.")
                }

                override fun handleResponse(call: Call, response: Response) {
                    channel.sendMessage("${author.asMention}: Here's what the meme looks like")
                        .addFile(response.toBytes(), "meme.jpg")
                        .queue()
                }
            })
        }
    }

    /**
     * Encodes the String according to the API's requirements.
     *
     * @return The URL encoded String.
     */
    private fun String.encode() : String {
        return when {
            isBlank() -> "_"
            else -> {
                URLEncoder.encode(
                    this.replace("\"", "''")
                        .replace("~q", "~-q")
                        .replace("~p", "~-p")
                        .replace("?", "~q")
                        .replace("%", "~p")
                        .replace("-", "--")
                        .replace("_", "__")
                        .trim()
                        .replace(" ", "_"),
                    StandardCharsets.UTF_8.displayName()
                )
            }
        }
    }
}
