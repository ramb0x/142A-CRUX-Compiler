package crux;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class Compiler {
    public static String studentName = "Ram Bhakta";
    public static String studentID = "40993350";
    public static String uciNetID = "hrbhakta";

    public static void main(String[] args) {
        for (int i = 1; i < 23; i++) {
            String sourceFilename = "tests/test" + (i > 9 ? i : "0" + i) + ".crx";

            Scanner s = null;
            try {
                s = new Scanner(new FileReader(sourceFilename));
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error accessing the source file: \"" + sourceFilename + "\"");
                System.exit(-2);
            }

            Parser p = new Parser(s);
            ast.Command syntaxTree = p.parse();
            if (p.hasError()) {
                System.out.println("Error parsing file " + sourceFilename);
                System.out.println(p.errorReport());
                System.exit(-3);
            }

            types.TypeChecker tc = new types.TypeChecker();
            tc.check(syntaxTree);
            if (tc.hasError()) {
                System.out.println("Error type-checking file " + sourceFilename);
                System.out.println(tc.errorReport());
                System.exit(-4);
            }

            mips.CodeGen cg = new mips.CodeGen(tc);
            cg.generate(syntaxTree);
            if (cg.hasError()) {
                System.out.println("Error generating code for file " + sourceFilename);
                System.out.println(cg.errorReport());
                System.exit(-5);
            }

            String asmFilename = sourceFilename.replace(".crx", ".asm");
            try {
                mips.Program prog = cg.getProgram();
                File asmFile = new File(asmFilename);
                PrintStream ps = new PrintStream(asmFile);
                prog.print(ps);
                ps.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error writing assembly file: \"" + asmFilename + "\"");
                System.exit(-6);
            }
        }
    }
}
    
