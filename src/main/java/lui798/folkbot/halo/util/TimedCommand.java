package lui798.folkbot.halo.util;

import lui798.folkbot.halo.ServerConnection;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TimedCommand {

    private Timer timer;
    private TimerTask task;
    private int period;

    public TimedCommand(ServerConnection server, List<String> commands, int period) {
        this.timer = new Timer();
        this.task = new TimerTask() {
            @Override
            public void run() {
                for (String c : commands) {
                    server.send(c);
                }
            }
        };
        this.timer.schedule(task, 5000, period);
    }

    public void stop() {
        this.timer.cancel();
    }

    public void start() {
        this.timer.schedule(task, 5000, period);
    }
}
