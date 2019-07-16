package lui798.folkbot.command;

import lui798.folkbot.Bot;
import lui798.folkbot.command.util.CommandResult;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

public class ClearCommand extends Command {

    public ClearCommand() {
        setName("clear");
        setPerms(Arrays.asList(Permission.MESSAGE_MANAGE));
    }

    @Override
    public CommandResult run(Message message, List<String> arguments) {
        int n;
        String argument;
        TextChannel channel = message.getTextChannel();

        if (arguments.isEmpty())
            return new CommandResult("Wrong input",
                    "Please type a valid integer. **" + Bot.prefix + getName() + "** ***2***", Bot.ERROR_COLOR);
        else
            argument = arguments.get(0);

        try {
            n = Integer.valueOf(argument);
        } catch (NumberFormatException e) {
            return new CommandResult("Wrong input",
                    "Please type a valid integer. **" + Bot.prefix + getName() + "** ***2***", Bot.ERROR_COLOR);
        }

        if (n < 2) {
            return new CommandResult("Wrong input",
                    "Please type an integer greater than 1. **" + Bot.prefix + getName() + "** ***2***", Bot.ERROR_COLOR);
        }

        OffsetDateTime twoWeeksAgo = OffsetDateTime.now().minus(2, ChronoUnit.WEEKS);

        message.delete().complete();
        List<Message> messages = channel.getHistory().retrievePast(n).complete();
        messages.removeIf(m -> m.getCreationTime().isBefore(twoWeeksAgo));

        if (messages.isEmpty())
            return null;

        channel.deleteMessages(messages).complete();
        messages.forEach(m -> System.out.println("Deleted: " + m));

        return null;
    }
}
