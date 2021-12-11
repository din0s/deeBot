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

package me.din0s.util

import me.din0s.const.Unicode
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.util.function.Function

/**
 * Converts a period of time in milliseconds to a duration string.
 *
 * @return The duration in a human readable from.
 */
fun Long.asTime() : String {
    var time = this
    val days = TimeUnit.MILLISECONDS.toDays(time)
    time -= TimeUnit.DAYS.toMillis(days)
    val hours = TimeUnit.MILLISECONDS.toHours(time)
    time -= TimeUnit.HOURS.toMillis(hours)
    val mins = TimeUnit.MILLISECONDS.toMinutes(time)
    time -= TimeUnit.MINUTES.toMillis(mins)
    val secs = TimeUnit.MILLISECONDS.toSeconds(time)

    val sb = StringBuilder()
    for (unit in setOf(Pair(days, "day"), Pair(hours, "hour"), Pair(mins, "minute"), Pair(secs, "second"))) {
        if (unit.first > 0) {
            sb.append(unit.first).append(" ").append(unit.second)
            if (unit.first > 1) {
                sb.append("s")
            }
            sb.append(" ")
        }
    }
    return sb.toString().trim()
}

/**
 * Converts the [OffsetDateTime] into a nice looking form.
 *
 * @return The datetime formatted with [DateTimeFormatter.RFC_1123_DATE_TIME].
 */
fun OffsetDateTime.asString() : String {
    return format(DateTimeFormatter.RFC_1123_DATE_TIME)
//    return format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + " UTC" // Wrong since it might not be UTC
}

/**
 * Escapes all markdown characters within the string.
 *
 * @return The message with escaped markdown symbols.
 */
fun String.escaped() : String {
    return replace("*", "\\*")
        .replace("`", "\\`")
        .replace("_", "\\_")
        .replace("~~", "\\~\\~")
        .replace(">", "${Unicode.ZERO_WIDTH}>")
}

/**
 * Replaces back ticks with a similarly looking characters.
 *
 * @return The message without back ticks.
 */
fun String.noBackTicks() : String {
    return replace("`", Unicode.BACK_TICK)
}

/**
 * Converts a collection of items into a paginated list of strings.
 *
 * @param func The function with which the item will be converted to a string entry.
 * @param size The amount of items per page.
 * @return A list of pages, where each page is a string.
 */
fun <T> Collection<T>.paginate(func: Function<T, String>, size: Int) : List<String> {
    var i = 0
    val pages = mutableListOf<String>()
    val sb = StringBuilder()

    forEach {
        if (i != 0) {
            if (i % size == 0) {
                pages.add(sb.toString())
                sb.clear()
            } else {
                sb.append("\n")
            }
        }

        sb.append(func.apply(it))
        i++
    }

    if (sb.isNotEmpty()) {
        pages.add(sb.toString())
    }
    return pages
}

/**
 * Capitalizes a string.
 */
fun String.firstCaps() = replaceFirstChar { it.uppercase() }
