package lui798.tdbot.command;

public class Command {

    private String name;
    private RunnableC com;

    public Command(String name) {
        this.name = name;
    }

    public void setCom(RunnableC com) {
        this.com = com;
    }

    public String getName() {
        return name;
    }

    public String getArgument(String input) {
        String result;
        try {
            result = input.substring(input.indexOf(" "));
            result = input.substring(input.indexOf(" ")+1);
        }
        catch (StringIndexOutOfBoundsException e) {
            result = null;
        }
        return result;
    }

    public boolean equalsInput(String input) {
        if (getArgument(input) == null) {
            if (input.substring(1).equals(name))
                return true;
        }
        else {
            if (input.substring(1, input.indexOf(" ")).equals(name))
                return true;
        }
        return false;
    }

    public void run(String input) {
        if (getArgument(input) == null) {
            com.run();
        }
        else {
            com.run(getArgument(input));
        }
    }
}
