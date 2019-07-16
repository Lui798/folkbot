package lui798.folkbot.util.config;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class NewConfig {
    private Yaml yaml;
    private File config;

    public NewConfig(String filename) {
        yaml = new Yaml();
        config = new File(filename);
    }

    private Object loadFile() {
        Object obj = null;
        try {
            obj = yaml.load(new FileInputStream(config));
        }
        catch (FileNotFoundException e) {
            newFile();
        }
        return obj;
    }

    private void newFile() {

    }

    private void writeFile() {

    }
}
