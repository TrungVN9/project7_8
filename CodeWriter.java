import java.io.*;

public class CodeWriter {

    private final PrintWriter writer;
    private String fileName;        
    private int labelCounter = 0;   

    public CodeWriter(File outputFile) throws IOException {
        writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
    }

    //Set file name without extension included
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    // Process command lines of arithmetic
    public void writeArithmetic(String command) {
        writeComment(command);
        switch (command) {
            case "add": writeBinaryOperations("M=D+M"); break;
            case "sub": writeBinaryOperations("M=M-D"); break;
            case "and": writeBinaryOperations("M=D&M"); break;
            case "or":  writeBinaryOperations("M=D|M"); break;
            case "neg": writeUnaryOperations("M=-M");   break;
            case "not": writeUnaryOperations("M=!M");   break;
            case "eq":  writeComparableOperations("JEQ");  break;
            case "gt":  writeComparableOperations("JGT");  break;
            case "lt":  writeComparableOperations("JLT");  break;
            default:
                throw new IllegalArgumentException("Unknown arithmetic command: " + command);
        }
    }

    // Pops 2 values and process binary operations
    private void writeBinaryOperations(String operation) {
        // Pop top D
        helper_print("@SP");
        helper_print("AM=M-1");
        helper_print("D=M");
        // Peek is at SP
        helper_print("A=A-1");
        helper_print(operation);
    }

    // Process unary operations
    private void writeUnaryOperations(String operation) {
        helper_print("@SP");
        helper_print("A=M-1");
        helper_print(operation); 
    }

    private void writeComparableOperations(String jump) {
        String trueLabel  = "TRUE_"  + labelCounter;
        String endLabel   = "END_"   + labelCounter;
        labelCounter++;

        helper_print("@SP");
        helper_print("AM=M-1");
        helper_print("D=M");
        helper_print("A=A-1");
        helper_print("D=M-D");         

        helper_print("@" + trueLabel);
        helper_print("D;" + jump);     

        // Branch if its false
        helper_print("@SP");
        helper_print("A=M-1");
        helper_print("M=0");
        helper_print("@" + endLabel);
        helper_print("0;JMP");

        // branch if its true
        helper_print("(" + trueLabel + ")");
        helper_print("@SP");
        helper_print("A=M-1");
        helper_print("M=-1");

        helper_print("(" + endLabel + ")");
    }

    // handling push pop command
    public void writePushPop(Parser.TYPECOMMAND type, String segment, int index) {
        writeComment((type == Parser.TYPECOMMAND.C_PUSH ? "push " : "pop ") + segment + " " + index);

        if (type == Parser.TYPECOMMAND.C_PUSH) {
            writePush(segment, index);
        } else {
            writePop(segment, index);
        }
    }

    private void writePush(String segment, int index) {
        // Load value into D
        switch (segment) {
            case "constant":
                helper_print("@" + index);
                helper_print("D=A");
                break;
            case "local":
                loadFromBasePtr("LCL", index);
                break;
            case "argument":
                loadFromBasePtr("ARG", index);
                break;
            case "this":
                loadFromBasePtr("THIS", index);
                break;
            case "that":
                loadFromBasePtr("THAT", index);
                break;
            case "temp":
                helper_print("@" + (5 + index));
                helper_print("D=M");
                break;
            case "pointer":
                helper_print("@" + (3 + index));
                helper_print("D=M");
                break;
            case "static":
                helper_print("@" + fileName + "." + index);
                helper_print("D=M");
                break;
            default:
                throw new IllegalArgumentException("Unknown push segment: " + segment);
        }

        // Push D onto stack
        pushDToStack();
    }

    private void loadFromBasePtr(String base, int index) {
        helper_print("@" + base);
        helper_print("D=M");
        helper_print("@" + index);
        helper_print("A=D+A");
        helper_print("D=M");
    }

    private void pushDToStack() {
        helper_print("@SP");
        helper_print("A=M");
        helper_print("M=D");
        helper_print("@SP");
        helper_print("M=M+1");
    }

    private void writePop(String segment, int index) {
        switch (segment) {
            case "local":
                storeToBasePtr("LCL", index);
                break;
            case "argument":
                storeToBasePtr("ARG", index);
                break;
            case "this":
                storeToBasePtr("THIS", index);
                break;
            case "that":
                storeToBasePtr("THAT", index);
                break;
            case "temp":
                helper_print("@SP");
                helper_print("AM=M-1");
                helper_print("D=M");
                helper_print("@" + (5 + index));
                helper_print("M=D");
                break;
            case "pointer":
                helper_print("@SP");
                helper_print("AM=M-1");
                helper_print("D=M");
                helper_print("@" + (3 + index));
                helper_print("M=D");
                break;
            case "static":
                helper_print("@SP");
                helper_print("AM=M-1");
                helper_print("D=M");
                helper_print("@" + fileName + "." + index);
                helper_print("M=D");
                break;
            case "constant":
                throw new IllegalArgumentException("Cannot pop to constant segment.");
            default:
                throw new IllegalArgumentException("Unknown pop segment: " + segment);
        }
    }

    private void storeToBasePtr(String base, int index) {
        // Computing addresses
        helper_print("@" + base);
        helper_print("D=M");
        helper_print("@" + index);
        helper_print("D=D+A");
        helper_print("@R13");
        helper_print("M=D");

        helper_print("@SP");
        helper_print("AM=M-1");
        helper_print("D=M");

        helper_print("@R13");
        helper_print("A=M");
        helper_print("M=D");
    }

    private void helper_print(String instruction) {
        writer.println(instruction);
    }

    /** Writes a comment line (for readability / debugging of .asm output). */
    private void writeComment(String comment) {
        writer.println("// " + comment);
    }

    /** Closes the output file. */
    public void close() {
        writer.close();
    }
}