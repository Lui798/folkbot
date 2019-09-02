package lui798.folkbot.halo;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lui798.folkbot.halo.command.util.HaloCommandRunner;
import lui798.folkbot.halo.object.ChatMessage;
import lui798.folkbot.halo.object.Player;
import lui798.folkbot.halo.object.ServerInfo;
import lui798.folkbot.halo.util.Convert;
import lui798.folkbot.halo.object.PlayerList;
import lui798.folkbot.halo.util.RegexParser;
import lui798.folkbot.halo.util.TimedCommand;
import lui798.folkbot.util.Config;
import lui798.folkbot.util.json.JsonThing;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.protocols.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.*;

public class ServerConnection extends WebSocketClient {
    private final Logger log = LoggerFactory.getLogger(ServerConnection.class);

    private Timer update;
    private List<TimedCommand> timedCommands;
    private PlayerList oldPlayers = null;
    private PlayerList players;
    private List<String> admins;
    private JsonThing serverJson;
    private ServerInfo serverInfo;
    private TextChannel channel;
    private String password;
    private String lastStatus = null;
    private Config config;

    private HaloCommandRunner commandRunner;

    public ServerConnection(String ip, String rconPort, String gamePort, String password, TextChannel channel, List<String> admins, Config config) {
        super(URI.create("ws://" + ip + ":" + rconPort), new Draft_6455(Collections.emptyList(), Collections.singletonList(new Protocol("dew-rcon"))));

        String address = "http://" + ip + ":" + gamePort;
        this.password = password;
        this.channel = channel;
        this.timedCommands = new ArrayList<>();
        this.admins = admins;
        this.config = config;
        this.commandRunner = new HaloCommandRunner(this);

        try {
            this.serverJson = new JsonThing(address);
        }
        catch (MalformedURLException e) {
            log.error(e.getMessage());
        }

        timedCommands.add(new TimedCommand(this,
                config.getList("timedCommands"), 300000));

        connect();

        Timer reconnect = new Timer();
        reconnect.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!isOpen()) {
                    log.info("Reconnecting to WebSocket...");
                    reconnect();
                }
            }
        }, 5000, 20000);
    }

    private void update() {
        serverJson.getJsonFromUrl();
        serverInfo = new Gson().fromJson(serverJson.element(), ServerInfo.class);
        updatePlayers();
        getStats();
    }

    private void updatePlayers() {
        if (serverJson.element().getAsJsonObject().get("players") != null) {
            PlayerList playerIP = null;
            if (players != null)
                playerIP = players;

            JsonArray array = serverJson.element().getAsJsonObject().get("players").getAsJsonArray();
            this.players = new PlayerList();

            for (JsonElement e : array) {
                players.add(new Gson().fromJson(e, Player.class));
            }
            if (playerIP != null) {
                for (Player p : players) {
                    Player p2 = playerIP.find(p.name, p.uid);
                    if (p2 != null && p2.ip != null)
                        p.ip = p2.ip;
                }
            }
        }
    }

    private boolean listContainsPlayer(List<Player> list, Player ply) {
        for (Player p : list) {
            if (p.name.equals(ply.name) && p.uid.equals(ply.uid)) {
                return true;
            }
        }
        return false;
    }

    private void getStats() {
        String currentStatus;
        if (serverInfo.status.equals("InLobby"))
            currentStatus = "In Lobby | " + serverInfo.numPlayers + "/" + serverInfo.maxPlayers + " Players";
        else
            currentStatus = serverInfo.variant + " on " + serverInfo.map + " | " + serverInfo.numPlayers + "/" + serverInfo.maxPlayers + " Players";
        if (lastStatus == null) {
            lastStatus = currentStatus;
            channel.getManager().setTopic(currentStatus).queue();
        }
        else if (!currentStatus.equals(lastStatus)) {
            lastStatus = currentStatus;
            channel.getManager().setTopic(currentStatus).queue();
        }

        if (oldPlayers == null) {
            oldPlayers = players;
            for (Player p : oldPlayers) {
                //onJoin(p);
            }
        }
        else {
            for (Player p : players) {
                if (!listContainsPlayer(oldPlayers, p))
                    if (!p.name.trim().equals("") || !p.uid.startsWith("0000"))
                        onJoin(p);
            }
            for (Player p : oldPlayers) {
                if (!listContainsPlayer(players, p))
                    if (!p.name.trim().equals("") || !p.uid.startsWith("0000"))
                        onLeave(p);
            }
        }

        oldPlayers = players;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        send(password);
        send("Server.SendChatToRconClients 1");
        send("Server.ShouldAnnounce 1");
        send("Server.Announce");

        update = new Timer();
        update.schedule(new TimerTask() {
            @Override
            public void run() {
                update();
            }
        }, 5000, 1000);

        for (TimedCommand t : timedCommands) {
            t.start();
        }
    }

    @Override
    public void onMessage(String message) {
        final String PREFIX = "!";

        if (message.startsWith("accept")) {
            log.info("Successfully connected to Rcon");
        }
        else if (RegexParser.isMessage(message)) {
            try {
                ChatMessage c = RegexParser.parseMessage(message);
                String r = null;

                if (!c.name.equals("SERVER")) {
                    if (oldPlayers.find(c.name).ip == null)
                        oldPlayers.find(c.name).ip = c.ip;

                    if (commandRunner.isCommand(c.message, PREFIX)) {
                        r = commandRunner.runCommand(c, admins.contains(c.uid));
                    }
                    if (r != null && !r.trim().equals("")) {
                        sendPM(c.name, r);
                    }

                    sendToDiscord(embedMessage(c.name, c.message, Convert.hex2Rgb(players.find(c.name).primaryColor)));
                }
            }
            catch (NullPointerException e) { }
        }
        else {
            if (!message.equals(""))
                sendToDiscord(embedMessage("ServerResponse", message, Convert.hex2Rgb("#000000")));
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.warn("Lost connection to WebSocket - Code: " + code + " | Reason: " + reason);
        serverInfo = null;
        players = null;
        oldPlayers = null;

        update.cancel();
        update = null;

        for (TimedCommand t : timedCommands) {
            t.stop();
        }
    }

    @Override
    public void onError(Exception ex) {
        log.error(ex.getMessage());
    }

    private void onJoin(Player p) {
        sendToRcon(p.name + " [" + p.serviceTag + "] has joined.");
        sendToDiscord(embedMessage("Player Joined", "Name - " + p.name +
                "\nUID - " + p.uid, Convert.hex2Rgb(p.primaryColor)));

        List<String> rules = config.getList("serverRules");

        for (String r : rules) {
            if (r.equals("{listAdmins}")) {
                List<String> adminsOnline = new ArrayList<>();
                for (Player pl : players) {
                    if (admins.contains(pl.uid))
                        adminsOnline.add(pl.name);
                }
                if (!adminsOnline.isEmpty())
                    sendPM(p.name, "Admins online: " + String.join(", ", adminsOnline));
                else
                    sendPM(p.name, "There are no admins online.");
            }
            else sendPM(p.name, r);
        }
    }

    private void onLeave(Player p) {
        sendToRcon(p.name + " [" + p.serviceTag + "] has left.");
        sendToDiscord(embedMessage("Player Left", "Name - " + p.name + "\nIP - " + p.ip +
                "\nUID - " + p.uid, Convert.hex2Rgb(p.primaryColor)));
    }

    public PlayerList getPlayers() {
        return players;
    }

    public List<String> getAdmins() {
        return admins;
    }

    public void sendToRcon(String message) {
        send("Server.Say \"" + message + "\"");
    }

    public void sendPM(String user, String message) {
        send("Server.PM \"" + user + "\" \"" + message + "\"");
    }

    public void sendToDiscord(MessageEmbed embed) throws NullPointerException {
        channel.sendMessage(embed).queue();
    }

    public MessageEmbed embedMessage(String title, String message, int color) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.addField(title, message, false);
        builder.setColor(color);
        return builder.build();
    }
}
