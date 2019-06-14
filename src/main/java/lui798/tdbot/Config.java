package lui798.tdbot;

import java.io.*;
import java.util.Properties;

public class Config {
    private final String FILE_NAME = "tdbot.conf";
    private File file;
    private Properties prop;

    public Config() {
        file = new File(FILE_NAME);
        prop = new Properties();
        noFile();
        loadValues();
        emptyFile();
    }

    public String getToken() {
        return getProp("discordToken");
    }

    public String getPrefix() {
        return getProp("prefix");
    }

    public String getClient() {
        return getProp("twitchClientID");
    }

    public String getUser() {
        return getProp("twitchUser");
    }

    public String getLiveChannel() {
        return getProp("liveChannel");
    }

    public String getProp(String key) {
        return prop.getProperty(key);
    }

    public void setProp(String key, String value) {
        prop.setProperty(key, value);
        saveValues();
    }

    private void noFile() {
        try {
            InputStream is = new FileInputStream(FILE_NAME);
        } catch (FileNotFoundException e) {
            System.out.println("No config file found, will create new one\n" +
                    "Please fill out \"" + FILE_NAME + "\" and launch the bot again\n" +
                    "Press enter to exit");

            setDefaultValues();

            try {
                System.in.read();
            } catch (IOException eIO) {
                eIO.printStackTrace();
            }

            System.exit(0);
        }
    }

    private void emptyFile() {
        if (prop.isEmpty()) {
            System.out.println("Empty config file, will set default values\n" +
                    "Please fill out \"" + FILE_NAME + "\" and launch the bot again\n" +
                    "Press enter to exit");

            setDefaultValues();

            try {
                System.in.read();
            } catch (IOException eIO) {
                eIO.printStackTrace();
            }

            System.exit(0);
        }
    }

    private void setDefaultValues() {
        prop.setProperty("prefix", "?");
        prop.setProperty("discordToken", "bot-token-here");
        prop.setProperty("twitchUser", "twitch-username");
        prop.setProperty("twitchClientID", "twitch-client-id");
        prop.setProperty("liveChannel", "live-channel-id");
        prop.setProperty("chatChannel", "chat-channel-id");
        prop.setProperty("ircUser", "twitch-chatbot-username");
        prop.setProperty("ircOAuth", "oauth:password-here");
        saveValues();
    }

    public void saveValues() {
        FileOutputStream f;
        try {
            f = new FileOutputStream(FILE_NAME);

            try {
                prop.store(f, null);
                f.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            noFile();
        }
    }

    public void loadValues() {
        FileInputStream f;
        try {
            f = new FileInputStream(FILE_NAME);

            try {
                prop.load(f);
                f.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            noFile();
        }
    }
}
