package lui798.folkbot.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class EncodingUtil {

    public static String decodeURIComponent(String s) {
        if (s == null) {
            return null;
        }

        String result = null;
        try {
            result = URLDecoder.decode(s, "UTF-8");
        }
        // This exception should never occur.
        catch (UnsupportedEncodingException e) {
            result = s;
        }

        return result;
    }

    public static String encodeURIComponent(String s) {
        String result = null;
        try {
            result = URLEncoder.encode(s, "UTF-8")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        }
        // This exception should never occur.
        catch (UnsupportedEncodingException e) {
            result = s;
        }

        return result;
    }

    public static String decodeTimeInSeconds(int i) {
        //int s = i % 60;
        int h = i / 60;
        int m = h % 60;

        h = h / 60;

        if (h == 0) {
            return m + "m";
        }
        else {
            return h + "h" + m + "m";
        }
    }

    /**
     * Private constructor to prevent this class from being instantiated.
     */
    private EncodingUtil() {
        super();
    }
}