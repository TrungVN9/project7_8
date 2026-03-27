import java.io.*;
import java.util.*;

public class Parser {

    public enum TYPECOMMAND {
        C_ARITHMETIC,
        C_PUSH,
        C_POP,
        C_LABEL,
        C_GOTO,
        C_IF,
        C_FUNCTION,
        C_RETURN,
        C_CALL
    }

    private static final Set<String> COMMAND_ARITHMETIC = new HashSet<>(Arrays.asList(
        "add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not"
    ));

    private final List<String> lines = new ArrayList<>();
    private int currentIndex = -1;
    private String currentCommand = "";

    // Open VM input file to process
    public Parser(File inputFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String raw;
            while ((raw = br.readLine()) != null) {
                // Strip inline comments and trim whitespace
                int commentIdx = raw.indexOf("//");
                String line = (commentIdx >= 0) ? raw.substring(0, commentIdx) : raw;
                line = line.trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        }
    }

    // Returns true if there are more lines
    public boolean hasMoreLines() {
        return currentIndex < lines.size() - 1;
    }

    // Proceeds to the next command line and call hasMoreLines() when its true
    public void advance() {
        currentIndex++;
        currentCommand = lines.get(currentIndex);
    }

    // Returns VM command type
    public TYPECOMMAND commandType() {
        String first = currentCommand.split("\\s+")[0].toLowerCase();
        if (COMMAND_ARITHMETIC.contains(first)) return TYPECOMMAND.C_ARITHMETIC;
        switch (first) {
            case "push":     return TYPECOMMAND.C_PUSH;
            case "pop":      return TYPECOMMAND.C_POP;
            case "label":    return TYPECOMMAND.C_LABEL;
            case "goto":     return TYPECOMMAND.C_GOTO;
            case "if-goto":  return TYPECOMMAND.C_IF;
            case "function": return TYPECOMMAND.C_FUNCTION;
            case "call":     return TYPECOMMAND.C_CALL;
            case "return":   return TYPECOMMAND.C_RETURN;
            default:
                throw new IllegalStateException("Unknown command: " + currentCommand);
        }
    }

    // Returns first arg of the command line
    public String arg1() {
        if (commandType() == TYPECOMMAND.C_ARITHMETIC) {
            return currentCommand.split("\\s+")[0].toLowerCase();
        }
        return currentCommand.split("\\s+")[1];
    }

    // Return second arg of the command line as INT
    public int arg2() {
        return Integer.parseInt(currentCommand.split("\\s+")[2]);
    }
}