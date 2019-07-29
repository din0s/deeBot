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

package me.din0s.sql.tables

import org.jetbrains.exposed.sql.Table

/**
 * Table for all of the timers & their info,
 * which is their unique ID, their message,
 * the user's ID, the channel's ID that they
 * will be sent in, whether they're private,
 * the datetime for the next occurrence and
 * optionally a period of how often to be
 * repeated in days.
 *
 * @author Dinos Papakostas
 */
object Timers : Table() {
    val uuid = long("id").autoIncrement().primaryKey()
    val message = varchar("message", 2000)
    val userId = long("user_id")
    val channelId = long("channel_id")
    val private = bool("is_private")
    val time = varchar("date_time", 64)
    val period = long("period_days").nullable()
}
