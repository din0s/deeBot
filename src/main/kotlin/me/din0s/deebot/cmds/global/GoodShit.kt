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

import me.din0s.deebot.entities.Command
import me.din0s.deebot.reply
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

object GoodShit : Command(
    name = "goodshit",
    description = "The original 'good shit' copypasta",
    alias = setOf("shit")
) {
    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        event.reply(
            "\ud83d\udc4c\ud83c\udffd\uD83D\uDC40\ud83d\udc4c\ud83c\udffd\uD83D\uDC40\ud83d\udc4c\ud83c\udffd" +
                    "\uD83D\uDC40\ud83d\udc4c\ud83c\udffd\uD83D\uDC40\ud83d\udc4c\ud83c\udffd\uD83D\uDC40 good shit go౦ԁ sHit" +
                    "\ud83d\udc4c\ud83c\udffd thats ✔ some good\ud83d\udc4c\ud83c\udffd\ud83d\udc4c\ud83c\udffdshit right\ud83d" +
                    "\udc4c\ud83c\udffd\ud83d\udc4c\ud83c\udffdthere\ud83d\udc4c\ud83c\udffd\ud83d\udc4c\ud83c\udffd\ud83d\udc4c" +
                    "\ud83c\udffd right✔there ✔✔if i do ƽaү so my self \uD83D\uDCAF i say so \uD83D\uDCAF thats what im talking " +
                    "about right there right there (chorus: ʳᶦᵍʰᵗ ᵗʰᵉʳᵉ) mMMMMᎷМ\uD83D\uDCAF \ud83d\udc4c\ud83c\udffd\ud83d\udc4c\ud83c" +
                    "\udffd \ud83d\udc4c\ud83c\udffdНO0ОଠOOOOOОଠଠOoooᵒᵒᵒᵒᵒᵒᵒᵒᵒ\ud83d\udc4c\ud83c\udffd \ud83d\udc4c\ud83c\udffd\ud83d" +
                    "\udc4c\ud83c\udffd \ud83d\udc4c\ud83c\udffd \uD83D\uDCAF \ud83d\udc4c\ud83c\udffd \uD83D\uDC40 \uD83D\uDC40 " +
                    "\uD83D\uDC40 \ud83d\udc4c\ud83c\udffd\ud83d\udc4c\ud83c\udffdGood shit"
        )
    }
}
