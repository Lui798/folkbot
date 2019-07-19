package lui798.folkbot.halo.util;

import lui798.folkbot.halo.object.ChatMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexParser {
    private static String cregex = "(\\[)((?:[0]?[1-9]|[1][012])[-:\\/.](?:(?:[0-2]?\\d{1})|(?:[3][01]{1}))[-:\\/.](?:(?:\\d{1}\\d{1})))(?![\\d])( )((?:(?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):(?:[0-5][0-9])(?::[0-5][0-9])?(?:\\s?(?:am|AM|pm|PM))?)(\\])( )(<)((?:.*))(\\/)((?:.*))(\\/)((?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?))(?![\\d])(>)( )((?:.*))";

    public static boolean isMessage(String line) {
        ChatMessage cm = parseMessage(line);
        if (cm != null)
            if (cm.name != null && cm.ip != null && cm.message != null && cm.uid != null)
                if(!cm.name.equals("") && !cm.ip.equals("") && !cm.message.equals("") && !cm.uid.equals(""))
                    return true;

        return false;
    }

    public static ChatMessage parseMessage(String message) {
        Pattern p = Pattern.compile(cregex);
        Matcher m = p.matcher(message);

        if (m.find()) {
            ChatMessage msg = new ChatMessage();
            msg.date = m.group(2);
            msg.time = m.group(4);
            msg.name = m.group(8);
            msg.uid = m.group(10);
            msg.ip = m.group(12);
            msg.message = m.group(15);

            return msg;
        }

        return null;
    }
}
