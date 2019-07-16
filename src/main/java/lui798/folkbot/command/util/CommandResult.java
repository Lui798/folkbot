package lui798.folkbot.command.util;

public class CommandResult {
    private String result;
    private String desc;
    private int color;

    public static final int DEFAULT_COLOR = 7506394;
    public static final int ERROR_COLOR = 14696512;

    public static String ERROR = "Error";
    public static String SUCCESS = "Success";
    public static String RESULT = "Result";

    public CommandResult(String result, String desc) {
        this(result, desc, DEFAULT_COLOR);
    }

    public CommandResult(String result, String desc, int color) {
        this.result = result;
        this.desc = desc;
        this.color = color;
    }

    public String getDesc() {
        return desc;
    }

    public String getResult() {
        return result;
    }

    public int getColor() {
        return color;
    }
}
