package lui798.folkbot.halo.util;

import java.awt.Color;

public class Convert {

    public static int hex2Rgb(String color) {
        return new Color(
                Integer.valueOf(color.substring(1, 3), 16),
                Integer.valueOf(color.substring(3, 5), 16),
                Integer.valueOf(color.substring(5, 7), 16)).getRGB();
    }
}
