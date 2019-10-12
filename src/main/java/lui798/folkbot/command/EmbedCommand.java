package lui798.folkbot.command;

import lui798.folkbot.command.util.CommandResult;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EmbedCommand extends Command {

    private final Logger LOG = LoggerFactory.getLogger(EmbedCommand.class);

    public EmbedCommand() {
        setName("embed");
        setPerms(Arrays.asList(Permission.MESSAGE_WRITE, Permission.MESSAGE_MANAGE));
    }

    @Override
    public CommandResult run(Message message, List<String> arguments) {
        EmbedBuilder builder = new EmbedBuilder();
        List<String> lines = new ArrayList<>(parseInput(message.getContentRaw()));
        
        for (String line : lines) {
            List<String> parts = new ArrayList<>(parseLine(line));

            switch (parts.get(0)) {
                case "addField":
                    builder.addField(parts.get(1), parts.get(2), Boolean.valueOf(parts.get(3)));
                    break;
                case "setColor":
                    builder.setColor(Integer.parseInt(parts.get(1).replace("#", ""), 16));
                    break;
                case "setTitle":
                    if (parts.size() > 2)
                        builder.setTitle(parts.get(1), parts.get(2));
                    else
                        builder.setTitle(parts.get(1));
                    break;
                case "setFooter":
                    if (parts.get(3).equals("guildIcon"))
                        builder.setFooter(parts.get(1), message.getGuild().getIconUrl());
                    else
                        builder.setFooter(parts.get(1), parts.get(2));
                    break;
                case "setAuthor":
                    if (parts.size() == 2)
                        builder.setAuthor(parts.get(1));
                    else if (parts.size() == 3)
                        builder.setAuthor(parts.get(1), parts.get(2));
                    else
                        if (parts.get(3).equals("guildIcon"))
                            if (parts.get(2).equals("null"))
                                builder.setAuthor(parts.get(1), null, message.getGuild().getIconUrl());
                            else
                                builder.setAuthor(parts.get(1), parts.get(2), message.getGuild().getIconUrl());
                        else
                            builder.setAuthor(parts.get(1), parts.get(2), parts.get(3));
                    break;
                case "setThumbnail":
                    builder.setThumbnail(parts.get(1));
                    break;
                case "setImage":
                    builder.setImage(parts.get(1));
                    break;
            }
        }

        message.getTextChannel().sendMessage(builder.build()).queue();
        message.delete().queue();
        return null;
    }

    private List<String> parseInput(String message) {
        return Arrays.asList(message.split("\\s*\\n\\s*"));
    }

    private List<String> parseLine(String input) {
        List<String> list = Arrays.asList(input.split("\\s*,\\s*\""));
        for (int i = 0; i < list.size(); i++) {
            String s = list.get(i);
            if (s.substring(s.length()-1).equals("\"")) {
                list.set(i, s.substring(0, s.length()-1));
            }
        }
        return list;
    }
}
