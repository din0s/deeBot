package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import me.dinosparkour.utils.MessageUtil;
import me.dinosparkour.utils.UserUtil;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class WhoisCommand extends GlobalCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        User lookup;
        if (args.length > 0) {
            List<User> users = new UserUtil().getMentionedUsers(e.getMessage(), args,
                    e.isFromType(ChannelType.PRIVATE)
                            ? Collections.singletonList(e.getAuthor())
                            : e.getGuild().getMembers().stream().map(Member::getUser).collect(Collectors.toList()));
            switch (users.size()) {
                case 0:
                    chat.sendMessage("No users match the criteria!");
                    return;

                case 1:
                    lookup = users.get(0);
                    break;

                default:
                    chat.sendMessage("Your query returned too many users!\nPlease narrow down your search.");
                    return;
            }
        } else {
            lookup = e.getAuthor();
        }

        StringBuilder sb = new StringBuilder();

        String username = MessageUtil.userDiscrimSet(lookup);
        String id = lookup.getId();
        String creationDate = MessageUtil.formatDate(lookup.getCreationTime());
        String avatar = lookup.getAvatarUrl();

        sb.append("**__USER__**\n")
                .append("**Username**: ").append(username).append("\n")
                .append("**ID**: ").append(id).append("\n")
                .append("**Creation Date**: ").append(creationDate).append("\n")
                .append("**Avatar**: ").append(avatar).append("\n\n");

        if (e.isFromType(ChannelType.TEXT)) {
            Member member = e.getGuild().getMember(lookup);
            String joinDate = MessageUtil.formatDate(member.getJoinDate());
            String roles = String.join(", ", member.getRoles().stream().map(Role::getName).collect(Collectors.toList()));
            String nickname = member.getNickname();
            if (nickname != null) {
                nickname = MessageUtil.stripFormatting(nickname);
            }

            sb.append("**__GUILD__**\n");
            if (nickname != null) {
                sb.append("**Nickname**: ").append(nickname).append("\n");
            }
            sb.append("**Join Date**: ").append(joinDate).append("\n")
                    .append("**Roles**: ").append(roles.isEmpty() ? "@everyone" : roles);
        }

        chat.sendMessage(sb.toString());
    }

    @Override
    public String getName() {
        return "whois";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "userinfo");
    }

    @Override
    public String getDescription() {
        return "Returns useful information related to either the command issuer\nor the specific user, if mentioned.";
    }

    @Override
    public List<String> getOptionalParams() {
        return Collections.singletonList("user");
    }

    @Override
    public String getExample() {
        return "dinos#0649";
    }
}