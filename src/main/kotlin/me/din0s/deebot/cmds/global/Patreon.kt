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

import com.patreon.PatreonAPI
import com.patreon.resources.Campaign
import me.din0s.deebot.Config
import me.din0s.deebot.entities.Command
import me.din0s.deebot.reply
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class Patreon : Command(
    name = "patreon",
    description = "Learn how to support the development of this bot"
) {
    private val URL = "<http://patreon.deebot.xyz>"
    private val enabled = Config.patreon.isNotBlank()
    private val campaign : Campaign?

    init {
        if (enabled) {
            campaign = PatreonAPI(Config.patreon).fetchCampaigns().get()[0]
        } else {
            campaign = null
        }
    }

    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        if (campaign == null) {
            event.reply("Patreon API currently unavailable!\nClick on this link: $URL")
        } else {
            val sb = StringBuilder(event.author.asMention).append("\n")
            sb.append("Patrons: **").append(campaign.patronCount).append("** (thank you!)\n")
            sb.append("Pledged: **$").append(campaign.pledgeSum / 100.0).append("**")

            val nextGoal = campaign.goals.find { it.completedPercentage != 100 }
            if (nextGoal != null) {
                sb.append("/ **$").append(nextGoal.amountCents / 100).append("**")
            }

            sb.append("\n\nClick on this link for more info:\n").append(URL)
            event.reply(sb.toString())
        }
    }
}
