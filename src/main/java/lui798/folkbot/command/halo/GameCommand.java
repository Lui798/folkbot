package lui798.folkbot.command.halo;

import lui798.folkbot.Bot;
import lui798.folkbot.command.Command;
import lui798.folkbot.command.util.CommandResult;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;

import java.util.ArrayList;
import java.util.List;

public class GameCommand extends Command {

    public GameCommand() {
        setName("game");
        setPerms(null);
    }

    @Override
    public CommandResult run(Message message, List<String> arguments) {
        Guild guild = message.getGuild();
        CommandResult result = null;

        final int MAX_GAMES = 10;
        final String G_PREFIX = "Game_";
        Role everyone = guild.getPublicRole();

        if (arguments.get(0).startsWith("new")) {
            List<Permission> membrPerms = new ArrayList<>();
            List<Permission> adminPerms = new ArrayList<>();

            int id = -1;
            for (int i = 0; i < MAX_GAMES; i++) {
                if (guild.getRolesByName(G_PREFIX + i, true).isEmpty()) {
                    id = i;
                    break;
                }
                else {
                    if (message.getMember().getRoles().contains(guild.getRolesByName("(Admin)" + G_PREFIX + i, true).get(0))) {
                        id = -2;
                        break;
                    }
                    else if (message.getMember().getRoles().contains(guild.getRolesByName(G_PREFIX + i, true).get(0))) {
                        id = -3;
                        break;
                    }
                }
            }
            if (id == -1) {
                result = new CommandResult(CommandResult.ERROR, "No empty game slots available.", CommandResult.ERROR_COLOR);
                return result;
            }
            else if (id == -2) {
                result = new CommandResult(CommandResult.ERROR, "You already own a game. Please type\n `" +
                        Bot.prefix + getName() + " end` to end the game");
                return result;
            }
            else if (id == -3) {
                result = new CommandResult(CommandResult.ERROR, "You are already in a game. Please type\n `" +
                        Bot.prefix + getName() + " leave` to leave the game");
                return result;
            }

            //Create chats and roles for new game
            guild.getController().createCategory(G_PREFIX + id).complete();
            Category category = guild.getCategoriesByName(G_PREFIX + id, true).get(0);
            Role admin = guild.getController().createRole().setName("(Admin)" + G_PREFIX + id).complete();
            Role member = guild.getController().createRole().setName(G_PREFIX + id).complete();
            Channel gen = category.createVoiceChannel(G_PREFIX + id).setUserlimit(16).complete();
            Channel red = category.createVoiceChannel("Red Team_" + id).setUserlimit(8).complete();
            Channel blue = category.createVoiceChannel("Blue Team_" + id).setUserlimit(8).complete();

            //Add permissions
            //Member perms
            membrPerms.add(Permission.VIEW_CHANNEL);
            membrPerms.add(Permission.VOICE_CONNECT);
            //Admin perms
            adminPerms.add(Permission.VOICE_MOVE_OTHERS);
            adminPerms.add(Permission.VOICE_DEAF_OTHERS);
            adminPerms.add(Permission.VOICE_MUTE_OTHERS);

            //Combine perms
            List<Permission> combined = new ArrayList<>();
            combined.addAll(membrPerms);
            combined.addAll(adminPerms);

            //Set perms on channels
            category.getManager()
                    .putPermissionOverride(admin, combined, null)
                    .putPermissionOverride(member, membrPerms, null)
                    .putPermissionOverride(everyone, null, membrPerms).complete();
            gen.getManager()
                    .putPermissionOverride(admin, combined, null)
                    .putPermissionOverride(member, membrPerms, null)
                    .putPermissionOverride(everyone, null, membrPerms).complete();
            red.getManager()
                    .putPermissionOverride(admin, combined, null)
                    .putPermissionOverride(member, membrPerms, null)
                    .putPermissionOverride(everyone, null, membrPerms).complete();
            blue.getManager()
                    .putPermissionOverride(admin, combined, null)
                    .putPermissionOverride(member, membrPerms, null)
                    .putPermissionOverride(everyone, null, membrPerms).complete();

            //Set roles on game members
            guild.getController().addSingleRoleToMember(message.getMember(), admin).queue();
            for (Member m : message.getMentionedMembers()) {
                guild.getController().addSingleRoleToMember(m, member).queue();
            }

            result = new CommandResult(CommandResult.SUCCESS, "Your new game has been created with\nid `"
                    + id + "`, please find your new channel.", CommandResult.DEFAULT_COLOR);
        }
        else if (arguments.get(0).startsWith("end")) {
            Role admin = null;
            Role member = null;

            int id = -1;
            for (int i = 0; i < MAX_GAMES; i++) {
                if (!guild.getRolesByName("(Admin)" + G_PREFIX + i, true).isEmpty())
                    if (message.getMember().getRoles().contains(guild.getRolesByName("(Admin)" + G_PREFIX + i, true).get(0))) {
                        id = i;
                        admin = guild.getRolesByName("(Admin)" + G_PREFIX + id, true).get(0);
                        member = guild.getRolesByName(G_PREFIX + id, true).get(0);
                        break;
                    }
            }
            if (id < 0) {
                result = new CommandResult(CommandResult.ERROR, "You are not an admin of a game.", CommandResult.ERROR_COLOR);
                return result;
            }

            admin.delete().queue();
            member.delete().queue();

            guild.getVoiceChannelsByName(G_PREFIX + id, true).get(0).delete().complete();
            guild.getVoiceChannelsByName("Red Team_" + id, true).get(0).delete().complete();
            guild.getVoiceChannelsByName("Blue Team_" + id, true).get(0).delete().complete();
            guild.getCategoriesByName(G_PREFIX + id, true).get(0).delete().complete();
        }
        else if (arguments.get(0).startsWith("add") || arguments.get(0).startsWith("kick")) {
            Role member = null;

            int id = -1;
            for (int i = 0; i < MAX_GAMES; i++) {
                if (!guild.getRolesByName("(Admin)" + G_PREFIX + i, true).isEmpty())
                    if (message.getMember().getRoles().contains(guild.getRolesByName("(Admin)" + G_PREFIX + i, true).get(0))) {
                        id = i;
                        member = guild.getRolesByName(G_PREFIX + id, true).get(0);
                        break;
                    }
            }
            if (id < 0) {
                result = new CommandResult(CommandResult.ERROR, "You are not an admin of a game.", CommandResult.ERROR_COLOR);
                return result;
            }

            for (Member m : message.getMentionedMembers()) {
                if (arguments.get(0).startsWith("add")) {
                    guild.getController().addSingleRoleToMember(m, member).queue();
                }
                else if (arguments.get(0).startsWith("kick")) {
                    guild.getController().removeSingleRoleFromMember(m, member).queue();
                    if (m.getVoiceState().inVoiceChannel())
                        guild.getController().moveVoiceMember(m, guild.getVoiceChannelsByName("General", true).get(0)).queue();
                }
            }
        }
        else if (arguments.get(0).startsWith("leave")) {
            Role member = null;

            int id = -1;
            for (int i = 0; i < MAX_GAMES; i++) {
                if (!guild.getRolesByName( G_PREFIX + i, true).isEmpty())
                    if (message.getMember().getRoles().contains(guild.getRolesByName(G_PREFIX + i, true).get(0))) {
                        id = i;
                        member = guild.getRolesByName(G_PREFIX + id, true).get(0);
                        break;
                    }
            }
            if (id < 0) {
                result = new CommandResult(CommandResult.ERROR, "You are not a member of a game.", CommandResult.ERROR_COLOR);
                return result;
            }

                guild.getController().removeSingleRoleFromMember(message.getMember(), member).queue();
                if (message.getMember().getVoiceState().inVoiceChannel())
                    guild.getController().moveVoiceMember(message.getMember(),
                            guild.getVoiceChannelsByName("General", true).get(0)).queue();
        }

        return result;
    }
}
