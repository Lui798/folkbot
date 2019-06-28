package lui798.folkbot.util;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class DependencyFile {
    private URL url;
    private String dir;
    private String filename;

    public DependencyFile(URL url, String dir, String filename) {
        this.url = url;
        this.dir = dir;
        this.filename = filename;

        if (!this.dir.endsWith(File.separator) && !this.dir.isEmpty()) this.dir += File.separator;
        if (!alreadyExists()) {
            System.out.println("Downloading dependency \"" + filename + "\"...");
            if (download())
                System.out.println("Done!");
            else {
                System.out.println("Failed to download dependency.");

                try { System.in.read(); }
                catch (IOException e) { e.printStackTrace(); }
                System.exit(0);
            }
        }
    }

    private boolean alreadyExists() {
        File temp = new File(dir + filename);
        return temp.exists();
    }

    private boolean download() {
        URLConnection connection;
        DataInputStream inputStream;

        byte[] data;

        try {
            connection = url.openConnection();
            inputStream = new DataInputStream(connection.getInputStream());
            data = new byte[connection.getContentLength()];

            for (int i = 0; i < data.length; i++) {
                data[i] = inputStream.readByte();
            }
            inputStream.close();
            writeFile(new File(dir + filename), data);

            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void writeFile(File file, byte[] data) throws IOException {
        FileOutputStream outputStream;

        File directory = new File(dir);
        if (!directory.exists())
            directory.mkdir();

        file.createNewFile();

        outputStream = new FileOutputStream(file);
        outputStream.write(data);
        outputStream.close();
    }
}
