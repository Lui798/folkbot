package lui798.folkbot.command;

import lui798.folkbot.command.util.CommandResult;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.List;

public class StatsCommand extends Command {

    public StatsCommand() {
        setName("stats");
        setPerms(null);
    }

    @Override
    public CommandResult run(Message message, List<String> arguments) {
        message.getTextChannel().sendMessage(statsEmbed(message)).complete();
        return null;
    }

    public MessageEmbed statsEmbed(Message message) {
        Guild guild = message.getGuild();
        List<Member> members = guild.getMembers();
        EmbedBuilder builder = new EmbedBuilder();

        builder.setColor(CommandResult.DEFAULT_COLOR);
        builder.setAuthor(guild.getName(), null, message.getGuild().getIconUrl());

        List<Member> owners = new ArrayList<>(members);
        List<String> ownerString = new ArrayList<>();
        ownerString.add("<@" + guild.getOwner().getUser().getId() + ">");
        if (!guild.getRolesByName("Owner", true).isEmpty()) {
            owners.removeIf(m -> !m.getRoles().contains(guild.getRolesByName("Owner", true).get(0)));
            owners.remove(guild.getOwner());
            for (Member m : owners) {
                ownerString.add("<@" + m.getUser().getId() + ">");
            }
        }
        builder.addField("Owners", stringBuilder(ownerString), true);

        List<Member> admins = new ArrayList<>(members);
        List<String> adminsString = new ArrayList<>();
        if (!guild.getRolesByName("Admin", true).isEmpty()) {
            admins.removeIf(m -> !m.getRoles().contains(guild.getRolesByName("Admin", true).get(0)));
            for (Member m : admins) {
                adminsString.add("<@" + m.getUser().getId() + ">");
            }
        }
        builder.addField("Admins", stringBuilder(adminsString), true);

        List<Member> realMembers = new ArrayList<>(members);
        realMembers.removeIf(m -> m.getUser().isBot());
        builder.addField("Members", Integer.toString(realMembers.size()), true);

        List<Member> bots = new ArrayList<>(members);
        bots.removeIf(m -> !m.getUser().isBot());
        builder.addField("Bots", Integer.toString(bots.size()), true);

        List<Member> online = new ArrayList<>(members);
        online.removeIf(m -> m.getUser().isBot() || m.getOnlineStatus().getKey().equals("offline")
                || m.getOnlineStatus().getKey().equals("invisible"));
        builder.addField("Online", Integer.toString(online.size()), true);

        List<Member> offline = new ArrayList<>(members);
        offline.removeIf(m -> m.getUser().isBot());
        for (Member o : online) {
            offline.remove(o);
        }
        builder.addField("Offline", Integer.toString(offline.size()), true);

        return builder.build();
    }

    public String stringBuilder(List<String> strings) {
        StringBuilder builder = new StringBuilder();

        for (String s : strings) {
            builder.append(s);
            builder.append("\n");
        }

        return builder.toString();
    }
}
