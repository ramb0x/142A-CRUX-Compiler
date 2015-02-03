
package mips;

import ast.*;
import types.*;

import java.util.concurrent.Callable;


public class CodeGen implements ast.CommandVisitor {

    private StringBuilder errorBuffer = new StringBuilder();
    private TypeChecker tc;
    private Program program;
    private String rLab;
    private ActivationRecord currentFunction;

    public CodeGen(TypeChecker tc) {
        this.tc = tc;
        this.program = new Program();
    }

    public boolean hasError() {
        return errorBuffer.length() != 0;
    }

    public String errorReport() {
        return errorBuffer.toString();
    }

    private class CodeGenException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public CodeGenException(String errorMessage) {
            super(errorMessage);
        }
    }

    public boolean generate(Command ast) {

        try {
            currentFunction = ActivationRecord.newGlobalFrame();
            ast.accept(this);

        } catch (CodeGenException cge) {
            return true;
        }
       return hasError();
    }

    public Program getProgram() {
        return program;
    }

    public void popType(String register, Type t) {
        if (t.equivalent(new FloatType())) {
            program.popFloat(register);
        } else if ( t.equivalent(new IntType()) || t.equivalent(new BoolType()))
            program.popInt(register);

    }
    public void pop(char t, String arg1, String arg2) {
        if (t == 'i') {
            program.popInt(arg1);
            program.popInt(arg2);
        }
        if (t=='f') {
            program.popFloat(arg1);
            program.popFloat(arg2);
        }
    }


    @Override
    public void visit(ExpressionList node) {
        for (Expression expr : node) {
            expr.accept(this);
        }
    }

    @Override
    public void visit(DeclarationList node) {
        for (Declaration decl : node) {
            decl.accept(this);
        }
    }



    public void visit(StatementList node) {
        for (Statement s : node) {
            s.accept(this);
            if (s instanceof Call) {
                Type retType = tc.getType((Call)s);
                if (!retType.equivalent(new VoidType()))
                   popType("$t0",retType);

            }
        }
    }

    @Override
    public void visit(AddressOf node) {
        currentFunction.getAddress(program, "$t0" , node.symbol());
        program.pushInt("$t0");
    }

    @Override
    public void visit(LiteralBool node) {

       int cast = (node.value() == LiteralBool.Value.TRUE ? 1 : 0);
        program.appendInstruction("li $t0, " + cast);
        program.pushInt("$t0");
    }

    @Override
    public void visit(LiteralFloat node) {

        program.appendInstruction("li.s $f0, " +  node.value());
        program.pushFloat("$f0");
    }

    @Override
    public void visit(LiteralInt node) {

       program.appendInstruction("li $t0, " +  node.value());
       program.pushInt("$t0");
    }

    @Override
    public void visit(VariableDeclaration node) {
        currentFunction.add(program, node);
    }

    @Override
    public void visit(ArrayDeclaration node) {
        currentFunction.add(program, node);
    }

    @Override
    public void visit(FunctionDefinition node) {
        rLab = program.newLabel();
        currentFunction = new ActivationRecord(node, currentFunction);
        String funcName = node.symbol().name().equals("main") ? node.symbol().name() : "cruxfunc." + node.function().name();
        int iPos = program.appendInstruction(funcName + ":");
        node.body().accept(this);
        program.insertPrologue((iPos + 1), currentFunction.stackSize());
        program.appendInstruction(rLab + ":");
        Type t = tc.getType(node);
        if (!t.equivalent(new VoidType()))
           popType("$v0",t);

        program.appendEpilogue(currentFunction.stackSize(),  node.symbol().name().equals("main"));
        currentFunction = currentFunction.parent();
    }

    @Override
    public void visit(Addition node) {
        node.leftSide().accept(this);
        node.rightSide().accept(this);
        Type type = tc.getType(node);
        if ( type.equivalent(new IntType()) || type.equivalent(new BoolType())) {
            pop('i', "$t1", "$t0");
        program.appendInstruction("add $t2, $t0, $t1");
        program.pushInt("$t2");
        } else if (type.equivalent(new FloatType())) {
            pop('f', "$f2", "$f0");
            program.appendInstruction("add.s $f4, $f0, $f2");
            program.pushFloat("$f4");
        }
    }

    @Override
    public void visit(Subtraction node) {
      
        node.leftSide().accept(this);
        node.rightSide().accept(this);
        Type type = tc.getType(node);
        if (type.equivalent(new FloatType())) {
            pop('f', "$f2", "$f0");
            program.appendInstruction("sub.s $f4, $f0, $f2");

            program.pushFloat("$f4");
        } else if ( type.equivalent(new IntType()) || type.equivalent(new BoolType())) {
            pop('i',"$t1","$t0");
            program.appendInstruction("sub $t3, $t0, $t1");

            program.pushInt("$t3");
        }

    }



    public void visit(Multiplication node) {


        node.leftSide().accept(this);
        node.rightSide().accept(this);
        Type type = tc.getType(node);
        
        if (type.equivalent(new FloatType())) {
            pop('f',"$f2","$f0");
            program.appendInstruction("mul.s $f4, $f0, $f2");

            program.pushFloat("$f4");
        } else if ( type.equivalent(new IntType()) || type.equivalent(new BoolType())) {
            pop('i', "$t1", "$t0");
                    program.appendInstruction("mul $t3, $t0, $t1");

            program.pushInt("$t3");
        }

    }

    @Override
    public void visit(Division node) {


        node.leftSide().accept(this);

        node.rightSide().accept(this);
        Type type = tc.getType(node);
        if (type.equivalent(new FloatType())) {
            pop('f', "$f2", "$f0");
            program.appendInstruction("div.s $f4, $f0, $f2");

            program.pushFloat("$f4");

        } else if (type.equivalent(new IntType()) || type.equivalent(new BoolType())) {
            pop('i', "$t1", "$t0");
            program.appendInstruction("div $t3, $t0, $t1");

            program.pushInt("$t3");
        }

    }

    @Override
    public void visit(LogicalAnd node) {


        node.leftSide().accept(this);
        node.rightSide().accept(this);
        pop('i',"$t1","$t0");
        program.appendInstruction("and $t2, $t0, $t1");
        program.pushInt("$t2");

    }

    @Override

    public void visit(LogicalOr node) {

        node.leftSide().accept(this);
        node.rightSide().accept(this);
        pop('i', "$t1", "$t0");
        program.appendInstruction("or $t2, $t0, $t1");
        program.pushInt("$t2");

    }

    @Override
    public void visit(LogicalNot node) {


        String f = program.newLabel();
        String p = program.newLabel();
        node.expression().accept(this);

       negate(f,p);

    }


public void compare(Type t, ast.Comparison.Operation o) {
        boolean isFloat = t instanceof FloatType;
        if (isFloat)
            pop('f',"$f2 ","$f0");
        else
            pop('i',"$t1","$t0");

            switch (o) {
                case LT:

                    if (isFloat)
                        program.appendInstruction("c.lt.s $f0, $f2");
                    else
                    program.appendInstruction("slt $t2, $t0, $t1");
                    break;

                case GT:
                    if (isFloat)
                    program.appendInstruction("c.gt.s $f0, $f2");
                    else
                        program.appendInstruction("sgt $t2, $t0, $t1");

                    break;
                case LE:
                    if (isFloat)
                    program.appendInstruction("c.le.s $f0, $f2");
                    else
                        program.appendInstruction("sle $t2, $t0, $t1");

                    break;
                case GE:
                    if (isFloat)
                    program.appendInstruction("c.ge.s $f0, $f2");
                    else
                        program.appendInstruction("sge $t2, $t0, $t1");

                    break;
                case EQ:
                    if (isFloat)
                    program.appendInstruction("c.eq.s $f0, $f2");
                    else
                        program.appendInstruction("seq $t2, $t0, $t1");

                    break;
                case NE:
                    if (isFloat)
                    program.appendInstruction("c.ne.s $f0, $f2");
                    else
                        program.appendInstruction("sne $t2, $t0, $t1");
                    floatEpilogue();
                    break;
        }
        if (isFloat)
            floatEpilogue();
        else
            program.pushInt("$t2");


    }
    void negate(String f, String p) {

        program.popInt("$t0");
        program.appendInstruction("beqz $t0, " + f);
        program.appendInstruction("li $t1, 0");
        program.appendInstruction("b " + p);
        program.appendInstruction(f + ":");
        program.appendInstruction("li $t1, 1");
        program.appendInstruction(p + ":");
        program.pushInt("$t1");
    }
    public void visit(Comparison node) {
        node.leftSide().accept(this);
        node.rightSide().accept(this);
        Type type = tc.getType((Command) node.leftSide());
        compare(type,node.operation());

    }

    
    private void floatEpilogue() {

        String e = program.newLabel();
        String j = program.newLabel();
        program.appendInstruction("bc1f " + e);
        program.appendInstruction("li $t0, 1");
        program.appendInstruction("b " + j);
        program.appendInstruction(e + ":");
        program.appendInstruction("li $t0, 0");
        program.appendInstruction(j + ":");
        program.pushInt("$t0");
    }

    @Override
    public void visit(Dereference node) {

        node.expression().accept(this);
        program.popInt("$t0"); // Contains address to type ,/
        Type type = tc.getType(node);

        if (type.equivalent(new FloatType())) {
            program.appendInstruction("lwc1 $f0, 0($t0)");
            program.pushFloat("$f0");
        } else if (type.equivalent(new IntType()) || type.equivalent(new BoolType())) {
            program.appendInstruction("lw $t1, 0($t0)");
            program.pushInt("$t1");
        }
    }

    @Override
    public void visit(Index node) {
        node.base().accept(this);
        node.amount().accept(this);
        pop('i',"$t0","$t1");
       Type type = tc.getType(node);
        program.appendInstruction("li $t2, " + ActivationRecord.getSize(type));
        program.appendInstruction("mul $t3, $t0, $t2");
        program.appendInstruction("add $t4, $t1, $t3");
        program.pushInt("$t4");
    }

    @Override
    public void visit(Assignment node) {


        node.destination().accept(this);
        node.source().accept(this);
        Type type = tc.getType(node);
        if (type.equivalent(new FloatType())) {

            program.popFloat("$f0");
            program.popInt("$t0");

            program.appendInstruction("swc1 $f0, 0($t0)");
        } else if (type.equivalent(new IntType()) || type.equivalent(new BoolType())){

          pop('i',"$t0","$t1");

            program.appendInstruction("sw $t0, 0($t1)");
        }

    }


    public String getPrefix(String name) {
        if (!name.matches("(print|read)(Bool|Float|Int|ln)"))
           return "cruxfunc." +name;

          return   "func." + name;
    }

    public void visit(Call node) {
        node.arguments().accept(this);
        String funcName =  getPrefix(node.function().name());
        program.appendInstruction("jal " + funcName);
        if (node.arguments().size() > 0) {
            int a = 0;
            for (Expression expr : node.arguments())
                      a +=  ActivationRecord.numBytes(tc.getType((Command) expr)) ;

            program.appendInstruction("addi $sp, $sp, " + a);
        }
        if (!( (FuncType) node.function().type()).returnType().equivalent(new VoidType())) {
            program.appendInstruction("subu $sp, $sp, 4");
            program.appendInstruction("sw $v0, 0($sp)");
        }

    }

    @Override
    public void visit(IfElseBranch node) {

        String e = program.newLabel();
        String j = program.newLabel();
        node.condition().accept(this);
        program.popInt("$t7");
        program.appendInstruction("beqz $t7, " + e);
        node.thenBlock().accept(this);
        program.appendInstruction("b " + j);


        program.appendInstruction(e + ":");
        node.elseBlock().accept(this);


        program.appendInstruction(j + ":");

    }

    @Override
    public void visit(WhileLoop node) {

        String c = program.newLabel();
        String j = program.newLabel();



        program.appendInstruction(c + ":");
        node.condition().accept(this);


        program.popInt("$t7");
        program.appendInstruction("beqz $t7, " + j);
        node.body().accept(this);
        program.appendInstruction("b " + c);
        program.appendInstruction(j + ":");

    }

    @Override
    public void visit(Return node) {

        node.argument().accept(this);


        program.appendInstruction("b " + rLab);
    }

    @Override
    public void visit(ast.Error node) {
        String message = "CodeGen cannot compile a " + node;
        errorBuffer.append(message);
        throw new CodeGenException(message);
    }

    @Override
    public void visit(ReadSymbol readSymbol) {

    }
}

