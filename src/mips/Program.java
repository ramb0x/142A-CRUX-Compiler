package mips;

import java.io.PrintStream;
import java.util.ArrayList;


public class Program {
    
    private ArrayList<String> codeSegment;
    private ArrayList<String> dataSegment;
    private int labelCounter;
    private final boolean DEBUG = true;

    
    public Program() {
        labelCounter = -1;
        codeSegment = new ArrayList<String>();
        dataSegment = new ArrayList<String>();
    }
    
    public String newLabel() {
        return "label." + ++labelCounter;
    }

    public int appendInstruction(String instr) {
        codeSegment.add(instr);
        return codeSegment.size() - 1;
    }

    
    public void replaceInstruction(int pos, String instr) {
        codeSegment.set(pos, instr);
    }

    
    public void insertInstruction(int pos, String instr) {
        codeSegment.add(pos, instr);
    }

    
    public void appendData(String data) {
        dataSegment.add(data);
    }

    
    public void pushInt(String reg) {
        
        appendInstruction("subu $sp, $sp, 4");
        appendInstruction("sw " + reg + ", 0($sp)");
    }

    
    public void pushFloat(String reg) {
        appendInstruction("subu $sp, $sp, 4");
        appendInstruction("swc1 " + reg + ", 0($sp)");
    }

    
    public void popInt(String reg) {
        appendInstruction("lw " + reg + ", 0($sp)");
        appendInstruction("addiu $sp, $sp, 4");
    }

    
    public void popFloat(String reg) {
        appendInstruction("lwc1 " + reg + ", 0($sp)");
        appendInstruction("addiu $sp, $sp, 4");
    }

    
    public void insertPrologue(int pos, int frameSize) {
        ArrayList<String> prologue = new ArrayList<String>();
        prologue.add("subu $sp, $sp, 8");
        prologue.add("sw $fp, 0($sp)");
        prologue.add("sw $ra, 4($sp)");
        prologue.add("addi $fp, $sp, 8");
        if (frameSize > 0 )
            prologue.add("subu $sp, $sp, " + frameSize);

        codeSegment.addAll(pos, prologue);
    }

    
    public void appendEpilogue(int frameSize, boolean M) {
        
        if (frameSize > 0)
            appendInstruction("addu $sp, $sp, " + frameSize);


        if (M)
            appendExitSequence();
         else {
            
            appendInstruction("lw $ra, 4($sp)");
            appendInstruction("lw $fp, 0($sp)");
            appendInstruction("addu $sp, $sp, 8" );

            appendInstruction("jr $ra");
        }
    }

    
    public void appendExitSequence() {
        codeSegment.add("li $v0, 10");
        codeSegment.add("syscall");
    }

    
    public void print(PrintStream s) {
        s.println(".data                         # BEGIN Data Segment");
        for (String data : dataSegment) {
            s.println(data);
        }
        s.println("data.newline:      .asciiz       \"\\n\"");
        s.println("data.floatquery:   .asciiz       \"float?\"");
        s.println("data.intquery:     .asciiz       \"int?\"");
        s.println("data.trueString:   .asciiz       \"true\"");
        s.println("data.falseString:  .asciiz       \"false\"");
        s.println("                              # END Data Segment");

        s.println(".text                         # BEGIN Code Segment");
        // provide the built-in functions
        funcPrintBool(s);
        funcPrintFloat(s);
        funcPrintInt(s);
        funcPrintln(s);
        funcReadFloat(s);
        funcReadInt(s);

        s.println(".text                         # BEGIN Crux Program");
        // write out the crux program
        for (String code : codeSegment) {
            s.println(code);
        }
        s.println("                              # END Code Segment");
    }


    // Syscall documentation: http://courses.missouristate.edu/kenvollmar/mars/help/syscallhelp.html

    
    public void funcPrintInt(PrintStream s) {
        s.println("func.printInt:");
        s.println("lw   $a0, 0($sp)");
        s.println("li   $v0, 1");
        s.println("syscall");
        s.println("jr $ra");
    }

    
    public void funcPrintBool(PrintStream s) {
        s.println("func.printBool:");
        s.println("lw $a0, 0($sp)");
        s.println("beqz $a0, label.printBool.loadFalse");
        s.println("la $a0, data.trueString");
        s.println("j label.printBool.join");
        s.println("label.printBool.loadFalse:");
        s.println("la $a0, data.falseString");
        s.println("label.printBool.join:");
        s.println("li   $v0, 4");
        s.println("syscall");
        s.println("jr $ra");
    }

    
    private void funcPrintFloat(PrintStream s) {
        s.println("func.printFloat:");
        s.println("l.s  $f12, 0($sp)");
        s.println("li   $v0,  2");
        s.println("syscall");
        s.println("jr $ra");
    }

    
    private void funcPrintln(PrintStream s) {
        s.println("func.println:");
        s.println("la   $a0, data.newline");
        s.println("li   $v0, 4");
        s.println("syscall");
        s.println("jr $ra");
    }

    
    private void funcReadInt(PrintStream s) {
        s.println("func.readInt:");
        s.println("la   $a0, data.intquery");
        s.println("li   $v0, 4");
        s.println("syscall");
        s.println("li   $v0, 5");
        s.println("syscall");
        s.println("jr $ra");
    }

    
    private void funcReadFloat(PrintStream s) {
        s.println("func.readFloat:");
        s.println("la   $a0, data.floatquery");
        s.println("li   $v0, 4");
        s.println("syscall");
        s.println("li   $v0, 6");
        s.println("syscall");
        s.println("mfc1 $v0, $f0");
        s.println("jr $ra");
    }

    

}

