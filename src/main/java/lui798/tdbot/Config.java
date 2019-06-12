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
        return (String)prop.get("token");
    }

    public String getPrefix() {
        return (String)prop.get("prefix");
    }

    public void setLiveChannel(String liveChannel) {
        prop.setProperty("liveChannel", liveChannel);
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
                System.out.println(eIO);
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
                System.out.println(eIO);
            }

            System.exit(0);
        }
    }

    private void setDefaultValues() {
        prop.setProperty("token", "000000000000000000000000.000000.000000000000000000000000000");
        prop.setProperty("prefix", "?");
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
                System.out.println(e);
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
                System.out.println(e);
            }
        } catch (FileNotFoundException e) {
            noFile();
        }
    }
}
