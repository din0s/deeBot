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

package me.din0s.deebot.cmds.guild.role.impl

import me.din0s.deebot.cmds.Command
import me.din0s.deebot.cmds.guild.role.RoleBase
import me.din0s.util.getAllArgs
import me.din0s.util.getRoleOrShowError
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * This class is used to indicate a subcommand for the [RoleBase] command.
 *
 * @author Dinos Papakostas
 */
abstract class RoleSub(
    subName: String,
    subDescription: String,
    subAlias: Set<String> = emptySet(),
    subMinArgs: Int = 0,
    subMaxArgs: Int = Integer.MAX_VALUE,
    subRequiredParams: Array<String> = emptyArray(),
    subOptionalParams: Array<String> = emptyArray(),
    subFlags: Map<String, String> = emptyMap(),
    subVariables: Map<String, String> = emptyMap(),
    subExamples: Array<String> = emptyArray()
) : Command(
    name = "${RoleBase.name} $subName",
    description = subDescription,
    alias = subAlias.map { "${RoleBase.name} $it" }.toSet(),
    guildOnly = true,
    minArgs = subMinArgs,
    maxArgs = subMaxArgs,
    requiredParams = subRequiredParams,
    optionalParams = subOptionalParams,
    flags = subFlags,
    variables = subVariables,
    examples = subExamples
) {
    /**
     * Returns the role mentioned in the message received, or throws an error if
     * there were either too many or no matches.
     *
     * @param hierarchy Whether to filter roles based on the hierarchy.
     * @param dropLast Whether to not include the last argument of the message.
     * @return The role mentioned if the criteria are met, null otherwise.
     */
    fun MessageReceivedEvent.getMentionedRoleOrError(hierarchy: Boolean = true, dropLast: Boolean = false) : Role? {
        val args = getAllArgs().substringAfter(' ')
        val roleName = when {
            dropLast -> args.substringBeforeLast(' ')
            else -> args
        }
        return getRoleOrShowError(roleName, hierarchy)
    }
}
