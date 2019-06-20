package lui798.folkbot.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class YouTubeHelper {

    private static final String youTubeUrlRegEx = "^(https?)?(://)?(www.)?(m.)?((youtube.com)|(youtu.be))/";

    private static final String VIDEO_ID_REGEX = "(?<v>[a-zA-Z0-9_-]{11})";
    private static final Pattern directVideoIdPattern = Pattern.compile("^" + VIDEO_ID_REGEX + "$");

    public static String extractVideoIdFromUrl(String url) {
        String youTubeLinkWithoutProtocolAndDomain = youTubeLinkWithoutProtocolAndDomain(url);

        Matcher matcher = directVideoIdPattern.matcher(youTubeLinkWithoutProtocolAndDomain);
        if(matcher.find()){
            return matcher.group(1);
        }

        return null;
    }

    private static String youTubeLinkWithoutProtocolAndDomain(String url) {
        Pattern compiledPattern = Pattern.compile(youTubeUrlRegEx);
        Matcher matcher = compiledPattern.matcher(url);

        if(matcher.find()){
            return url.replace(matcher.group(), "");
        }
        return url;
    }

    public static String videoUrlProcess(String id) {
        try {
            Runtime rt = Runtime.getRuntime();
            String[] commands = {"bin\\youtube-dl.exe", "-g", "-f", "bestaudio[ext=webm]", id};
            Process proc = rt.exec(commands);

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()));

            // read the output from the command
            String s = null;
            if ((s = stdInput.readLine()) != null) {
                return s;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean isValidUrl(String url) {
        try {
            new URL(url).toURI();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }
}
