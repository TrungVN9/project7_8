import java.io.*;

public class VMTranslator {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: java VMTranslator exampleFileName.vm");
            System.exit(1);
        }

        File inputFile = new File(args[0]);
        if (!inputFile.exists() || !args[0].endsWith(".vm")) {
            System.err.println("Error: FILE NOT EXIST!!!");
            System.exit(1);
        }

        // output .asm file
        String outputPath = args[0].replace(".vm", ".asm");
        String vmFileName = inputFile.getName().replace(".vm", ""); // used for static labels

        Parser    parser      = new Parser(inputFile);
        CodeWriter codeWriter = new CodeWriter(new File(outputPath));
        codeWriter.setFileName(vmFileName);

        while (parser.hasMoreLines()) {
            parser.advance();

            switch (parser.commandType()) {
                case C_ARITHMETIC:
                    codeWriter.writeArithmetic(parser.arg1());
                    break;
                case C_PUSH:
                    codeWriter.writePushPop(Parser.TYPECOMMAND.C_PUSH,
                                           parser.arg1(), parser.arg2());
                    break;
                case C_POP:
                    codeWriter.writePushPop(Parser.TYPECOMMAND.C_POP,
                                           parser.arg1(), parser.arg2());
                    break;
                default:
                    break;
            }
        }

        codeWriter.close();
        System.out.println("Translation complete → " + outputPath);
    }
}