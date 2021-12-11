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

import me.din0s.const.Regex
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.sharding.ShardManager

/**
 * Returns all members in the guild that match the given string.
 *
 * @param str The string to match.
 * @return A set of all members, or an empty set if none match.
 */
fun Guild.matchMembers(str: String) : Set<Member> {
    val set = mutableSetOf<Member>()
    Regex.USER_TAG.findAll(str)
        .mapNotNull { members.find { m -> m.user.asTag == str } }
        .forEach { set.add(it) }

    Regex.DISCORD_ID.findAll(str)
        .mapNotNull { getMemberById(it.value) }
        .forEach { set.add(it) }

    if (set.isEmpty()) {
        members.filter { it.user.name == str }.forEach { set.add(it) }
    }
    if (set.isEmpty()) {
        members.filter { it?.nickname == str }.forEach { set.add(it) }
    }
    return set
}

/**
 * Returns the IDs of all users in the list that match the given string.
 *
 * @param str The string to match.
 * @return A set of all user IDs, or an empty set if none match.
 */
fun List<User>.matchUserIds(str: String) : Set<String> {
    val set = mutableSetOf<String>()
    Regex.USER_TAG.findAll(str)
        .mapNotNull { find { u -> u.asTag == it.value }?.id }
        .forEach { set.add(it) }

    Regex.DISCORD_ID.findAll(str)
        .map { it.value }
        .forEach { set.add(it) }

    if (set.isEmpty()) {
        filter { it.name == str }.forEach { set.add(it.id) }
    }
    return set
}

/**
 * Returns the IDs of all users from the current [JDA] instance that match the given string.
 *
 * @param str The string to match.
 * @return A set of all user IDs, or an empty set if none match.
 */
fun JDA.matchUserIds(str: String) : Set<String> {
    return users.matchUserIds(str)
}

/**
 * Returns the IDs of all users from the [ShardManager] instance that match the given string.
 *
 * @param str The string to match.
 * @return A set of all user IDs, or an empty set if none match.
 */
fun ShardManager.matchUserIds(str: String) : Set<String> {
    return users.matchUserIds(str)
}

/**
 * Returns all of the users in the list that match the given string.
 *
 * @param str The string to match.
 * @return A set of all users, or an empty set if none match.
 */
fun List<User>.matchUsers(str: String) : Set<User> {
    val set = mutableSetOf<User>()
    Regex.USER_TAG.findAll(str)
        .mapNotNull { find { u -> u.asTag == it.value } }
        .forEach { set.add(it) }

    Regex.DISCORD_ID.findAll(str)
        .mapNotNull { find { u -> u.id == it.value } }
        .forEach { set.add(it) }

    if (set.isEmpty()) {
        filter { it.name == str }.forEach { set.add(it) }
    }
    return set
}

/**
 * Returns all of the users from the [ShardManager] instance that match the given string.
 *
 * @param str The string to match.
 * @return A set of all users, or an empty set if none match.
 */
fun ShardManager.matchUsers(str: String) : Set<User> {
    return users.matchUsers(str)
}

