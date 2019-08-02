package lui798.folkbot.halo.util;

import lui798.folkbot.halo.ServerConnection;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TimedCommand {

    private Timer timer;
    private TimerTask task;
    private int period;
    private List<String> commands;
    private ServerConnection server;

    public TimedCommand(ServerConnection server, List<String> commands, int period) {
        this.server = server;
        this.period = period;
        this.commands = commands;
    }

    public void stop() {
        timer.cancel();
        timer = null;
        task = null;
    }

    public void start() {
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                for (String c : commands) {
                    server.send(c);
                }
            }
        };
        timer.schedule(task, 5000, period);
    }
}
