package lui798.folkbot.util;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Config {
    private final String FILE_NAME;
    private Properties prop;

    public Config(String filename) {
        FILE_NAME = filename;
        prop = new Properties();
        noFile();
        loadValues();
        emptyFile();
    }

    public void setProp(String key, String value) {
        prop.setProperty(key, value);
        saveValues();
    }

    public String getProp(String key) {
        return prop.getProperty(key);
    }

    public List<String> getList(String key) {
        return Arrays.asList(prop.getProperty(key).split("\\s*,\\s*"));
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
        prop.setProperty("prefix", "$");
        prop.setProperty("discordToken", "unchanged");
        prop.setProperty("guildID", "unchanged");
        prop.setProperty("twitchUser", "unchanged");
        prop.setProperty("twitchClientID", "unchanged");
        prop.setProperty("liveChannel", "unchanged");

        saveValues();
    }

    private void saveValues() {
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

    private void loadValues() {
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
