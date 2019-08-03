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
import me.din0s.util.reply
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Replies with the 'Rekt' checklist.
 *
 * @author Dinos Papakostas
 */
object Rekt : Command(
    name = "rekt",
    description = "Rekt copypasta",
    alias = setOf("erekt")
) {
    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        event.reply("""
            __REKT checklist:__
            ⬜ Not Rekt
            ✅ REKT
            ✅ REKTangle
            ✅ Tyrannosaurus REKT
            ✅ Caught REKT handed
            ✅ Singing in the REKT
            ✅ The REKT Prince of Bel-Air
            ✅ REKTflix
            ✅ REKT it like it's hot
            ✅ REKT and Roll
            ✅ REKT Paper Scissors
            ✅ REKTcraft
            ✅ Grand REKT Auto V
            ✅ Left 4 REKT
            ✅ www.REKT.com
            ✅ Pokemon: Fire REKT
            ✅ The Good, the bad, and the REKT
            ✅ shREKT
            ✅ eREKTile dysfunction
        """.trimIndent())
    }
}
