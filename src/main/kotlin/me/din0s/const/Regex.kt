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

package me.din0s.const

/**
 * This file contains all regex patterns that are used throughout the code.
 *
 * @author Dinos Papakostas
 */
object Regex {
    /**
     * This represents a Discord snowflake ID.
     * It consists of 17 - 19 numbers.
     */
    val DISCORD_ID = "\\d{17,19}".toRegex()

    /**
     * This represents the duration based on an absolute datetime.
     * e.g: Monday 09:00 pm GMT+3
     */
    val DURATION_ABSOLUTE =
        "^(?:([a-zA-Z]+)\\s)?\\s*(\\d{2}):(\\d{2})\\s*([aApP][mM]?)?\\s*([a-zA-Z]+(?:[+\\-]\\d+(?::\\d{2})?)?)?$".toRegex()

    /**
     * This represents the duration relative to the datetime of the command execution.
     * e.g: 5 hours
     */
    val DURATION_RELATIVE = "^(\\d+)\\s*([a-zA-Z]+)$".toRegex()

    /**
     * This represents a color hex value.
     * e.g: #fff, #7289DA
     */
    val HEX = "^#?((?:[0-9a-fA-F]{3}){1,2})$".toRegex()

    /**
     * This represents a positive integer up until 999,999,999.
     */
    val INTEGER = "\\d{1,6}".toRegex()

    /**
     * This is used to split a string around the '|' character.
     */
    val PIPE = "\\s*\\|\\s*".toRegex()

    /**
     * This captures the $random{} values used for Custom Commands.
     */
    val RANDOM = "\\\$random\\{([^}]+)}".toRegex()

    /**
     * This represents a Discord User tag.
     * e.g: dinos#0649
     */
    val USER_TAG = ".{2,32}#\\d{4}".toRegex()

    /**
     * This is used to skip multiple consecutive space characters.
     */
    val WHITESPACE = "\\s+".toRegex()
}
