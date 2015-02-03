package crux;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.RandomAccessFile;

public class AutoTesterProject6 {

    public static final int PASS = 0;
    public static final int FAIL = 1;
    public static final int NOT_ACCESSABLE = 2;
    public static final int IO_ERROR = 3;
    public static final int SLEEP_ERROR = 4;

    public static int testPrivate(int testNum) throws IOException, InterruptedException {
        String inputFilename = String.format("crux/tests/testP%02d.crx", testNum);
        String inFilename = String.format("crux/tests/testP%02d.in", testNum);
        String outputFilename = String.format("crux/tests/testP%02d.rea", testNum);
        String expectedFilename = String.format("crux/tests/testP%02d.out", testNum);
        String asmFilename = String.format("crux/tests/testP%02d.asm", testNum);


        System.out.println("Running  " + inputFilename);
        Scanner s = null;
        try {
            s = new Scanner(new FileReader(inputFilename));
        } catch (IOException e) {
            e.printStackTrace();
            return NOT_ACCESSABLE;
        }

        Parser p = new Parser(s);
        ast.Command syntaxTree = p.parse();
        try {
            PrintStream outputStream = new PrintStream(outputFilename);
            if (p.hasError()) {
                outputStream.println("Error parsing file.");
                outputStream.println(p.errorReport());
                outputStream.close();
                //System.exit(-3);
            } else {
                types.TypeChecker tc = new types.TypeChecker();
                tc.check(syntaxTree);
                if (tc.hasError()) {
                    outputStream.println("Error type-checking file.");
                    outputStream.println(tc.errorReport());
                    outputStream.close();
                } else {
                    outputStream.println("Crux Program has no type errors.");
                }

                mips.CodeGen cg = new mips.CodeGen(tc);
                cg.generate(syntaxTree);
                if (cg.hasError()) {
                    outputStream.println("Error type-checking file.");
                    outputStream.println(cg.errorReport());
                    outputStream.close();

                }

                PrintStream asmStream = new PrintStream(asmFilename);
                mips.Program prog = cg.getProgram();
                prog.print(asmStream);
                asmStream.close();
            }

        } catch (IOException e) {
            System.err.println("Error opening output file: \"" + outputFilename + "\"");
            e.printStackTrace();
            return IO_ERROR;
        }

        System.out.println("Running spim on " + asmFilename);
        // run spim on the output asm
        Process spim = Runtime.getRuntime().exec("spim -file " + asmFilename);
        RandomAccessFile inputFile = new RandomAccessFile(inFilename, "r");
        byte input[] = new byte[(int) inputFile.length()];
        inputFile.read(input);
        spim.getOutputStream().write(input);
        spim.getOutputStream().flush();
        inputFile.close();

        // wait 10 secs for spim to execute
        Thread.sleep(1000 * 10);

        // read back what spim produced
        PrintStream outputStream = new PrintStream(outputFilename);
        BufferedReader spimOutput = new BufferedReader(new InputStreamReader(spim.getInputStream()));
        for (int i = 0; i < 5; ++i) // skip spim preamble
            spimOutput.readLine();
        while (spimOutput.ready()) {
            outputStream.append((char) spimOutput.read());
        }

        spim.destroy();
        outputStream.close();

        BufferedReader bufferedexpected;
        BufferedReader bufferedoutput;

        String lineExpected;
        String lineOutput;

        try {
            bufferedexpected = new BufferedReader(new FileReader(expectedFilename));
            bufferedoutput = new BufferedReader(new FileReader(outputFilename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return IO_ERROR;
        }

        int result = PASS;

        try {
            while ((lineExpected = bufferedexpected.readLine()) != null) {
                lineOutput = bufferedoutput.readLine();
                if (lineOutput == null) {
                    result = FAIL;
                    break;
                }
                lineExpected = lineExpected.replaceAll("\\s+$", "");
                lineOutput = lineOutput.replaceAll("\\s+$", "");
                if (!lineExpected.equals(lineOutput)) {
                    result = FAIL;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = IO_ERROR;
        }

        try {
            bufferedoutput.close();
            bufferedexpected.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static int testPublic(int testNum) throws IOException, InterruptedException {
        String inputFilename = String.format("crux/tests/test%02d.crx", testNum);
        String inFilename = String.format("crux/tests/test%02d.in", testNum);
        String outputFilename = String.format("crux/tests/test%02d.rea", testNum);
        String expectedFilename = String.format("crux/tests/test%02d.out", testNum);
        String asmFilename = String.format("crux/tests/test%02d.asm", testNum);

        System.out.println("Running  " + inputFilename);
        Scanner s = null;
        try {
            s = new Scanner(new FileReader(inputFilename));
        } catch (IOException e) {
            e.printStackTrace();
            return NOT_ACCESSABLE;
        }

        Parser p = new Parser(s);
        ast.Command syntaxTree = p.parse();

        try {
            PrintStream outputStream = new PrintStream(outputFilename);
            if (p.hasError()) {
                outputStream.println("Error parsing file.");
                outputStream.println(p.errorReport());
                outputStream.close();
                //System.exit(-3);
            } else {
                types.TypeChecker tc = new types.TypeChecker();
                tc.check(syntaxTree);
                if (tc.hasError()) {
                    outputStream.println("Error type-checking file.");
                    outputStream.println(tc.errorReport());
                    outputStream.close();
                } else {
                    outputStream.println("Crux Program has no type errors.");
                }

                mips.CodeGen cg = new mips.CodeGen(tc);
                cg.generate(syntaxTree);
                if (cg.hasError()) {
                    outputStream.println("Error type-checking file.");
                    outputStream.println(cg.errorReport());
                    outputStream.close();

                }

                PrintStream asmStream = new PrintStream(asmFilename);
                mips.Program prog = cg.getProgram();
                prog.print(asmStream);
                asmStream.close();
            }
        } catch (IOException e) {
            System.err.println("Error opening output file: \"" + outputFilename + "\"");
            e.printStackTrace();
            return IO_ERROR;
        }

        System.out.println("Running spim on " + asmFilename);
        // run spim on the output asm
        Process spim = Runtime.getRuntime().exec("spim -file " + asmFilename);
        RandomAccessFile inputFile = new RandomAccessFile(inFilename, "r");
        byte input[] = new byte[(int) inputFile.length()];
        inputFile.read(input);
        spim.getOutputStream().write(input);
        spim.getOutputStream().flush();
        inputFile.close();

        // wait 10 secs for spim to execute
        Thread.sleep(1000 * 10);

        // read back what spim produced
        PrintStream outputStream = new PrintStream(outputFilename);
        BufferedReader spimOutput = new BufferedReader(new InputStreamReader(spim.getInputStream()));
        for (int i = 0; i < 5; ++i) // skip spim preamble
            spimOutput.readLine();
        while (spimOutput.ready()) {
            outputStream.append((char) spimOutput.read());
        }

        spim.destroy();
        outputStream.close();

        BufferedReader bufferedexpected;
        BufferedReader bufferedoutput;

        String lineExpected;
        String lineOutput;

        try {
            bufferedexpected = new BufferedReader(new FileReader(expectedFilename));
            bufferedoutput = new BufferedReader(new FileReader(outputFilename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return IO_ERROR;
        }

        int result = PASS;

        try {
            while ((lineExpected = bufferedexpected.readLine()) != null) {
                lineOutput = bufferedoutput.readLine();
                if (lineOutput == null) {
                    result = FAIL;
                    break;
                }
                lineExpected = lineExpected.replaceAll("\\s+$", "");
                lineOutput = lineOutput.replaceAll("\\s+$", "");
                if (!lineExpected.equals(lineOutput)) {
                    result = FAIL;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = IO_ERROR;
        }

        try {
            bufferedoutput.close();
            bufferedexpected.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static void main(String args[]) throws IOException {

        String studentName = Compiler.studentName;
        String studentID = Compiler.studentID;
        String uciNetID = Compiler.uciNetID;

        int publicTestcaseNum = 22;
        int privateTestcaseNum = 10;

        int publicPass = 0;
        for (int i = 1; i <= publicTestcaseNum; ++i) {
            try {
                if (testPublic(i) == PASS) {
                    ++publicPass;
                } else {
                    System.out.println("failed:" + i);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int privatePass = 0;
        for (int i = 1; i <= privateTestcaseNum; ++i) {
            try {
                if (testPrivate(i) == PASS) {
                    ++privatePass;
                } else {
                    System.out.println("failed:" + i);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        System.out.print(studentID);
        System.out.print("\t");
        System.out.print(uciNetID);
        System.out.print("\t");
        System.out.print(" Passed Public Cases: ");
        System.out.print(publicPass);
        System.out.print("/");
        System.out.print(publicTestcaseNum);
        System.out.print(" Passed Private Cases: ");
        System.out.print(privatePass);
        System.out.print("/");
        System.out.println(privateTestcaseNum);

    }
}