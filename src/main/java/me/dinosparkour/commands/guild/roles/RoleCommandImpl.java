package me.dinosparkour.commands.guild.roles;

import me.dinosparkour.commands.impls.GuildCommand;
import net.dv8tion.jda.Permission;

import java.util.Collections;
import java.util.List;

abstract class RoleCommandImpl extends GuildCommand {

    @Override
    public List<Permission> requiredPermissions() {
        return Collections.singletonList(Permission.MANAGE_ROLES);
    }
}