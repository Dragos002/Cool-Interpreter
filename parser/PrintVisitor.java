package cool.parser;
import cool.parser.ASTNode.*;
public class PrintVisitor implements ASTVisitor<Void>{
    private int indent = 0;
    void printIndent(String str) {
        for (int i = 0; i < indent; i++)
            System.out.print("  ");
        System.out.println(str);
    }

    @Override
    public Void visit(Id id) {
        printIndent(id.token.getText());
        return null;
    }

    @Override
    public Void visit(ClassDef classDef) {
        indent++;
        printIndent("class");
        indent++;
        classDef.type.accept(this);
        if (classDef.parent.token != null) {
            classDef.parent.accept(this);
        }
        for (Feature feature : classDef.features) {
            if (feature != null) {
                if (feature instanceof MethodDef) {
                    printIndent("method");
                    indent++;
                    feature.accept(this);
                    indent--;
                } else {
                    printIndent("attribute");
                    indent++;
                    feature.accept(this);
                    indent--;
                }
            }
        }
        indent-= 2;
        return null;
    }

    @Override
    public Void visit(Formal formal) {
        printIndent("formal");
        indent++;
        formal.name.accept(this);
        formal.type.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(MethodDef method) {
        method.name.accept(this);
        for (Formal formal : method.formals) {
            formal.accept(this);
        }
        method.returnType.accept(this);

        method.body.accept(this);

        return null;
    }

    @Override
    public Void visit(AttrDef attr) {
        attr.name.accept(this);
        attr.type.accept(this);
        if (attr.init != null) {
            attr.init.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(IntLiteral intt) {
        printIndent(intt.token.getText());
        return null;
    }

    @Override
    public Void visit(Feature feature) {
        feature.accept(this);
        return null;
    }

    @Override
    public Void visit(Type type) {
        printIndent(type.token.getText());
        return null;

    }

    @Override
    public Void visit(Program program) {
        printIndent("program");
        program.classes.forEach(c -> c.accept(this));
        return null;
    }

    @Override
    public Void visit(StringLiteral stringLiteral) {
        String str_print = stringLiteral.token.getText();
        str_print = str_print.substring(1, str_print.length() - 1);
        str_print = str_print.replace("\\t","\t");
        str_print = str_print.replace("\\n","\n");
        if (str_print.contains("\\\\"))
            str_print = str_print.replace("\\\\","\\");
        else
            str_print = str_print.replace("\\","");
        printIndent(str_print);
        return null;
    }

    @Override
    public Void visit(Arithmetic arithmetic) {
        printIndent(arithmetic.getToken().getText());
        indent++;
        arithmetic.left.accept(this);
        arithmetic.right.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(UnaryMinus unaryMinus) {
        printIndent("~");
        indent++;
        unaryMinus.expr.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Comparison comparison) {
        printIndent(comparison.getToken().getText());
        indent++;
        comparison.left.accept(this);
        comparison.right.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Not not) {
        printIndent("not");
        indent++;
        not.expr.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Assign assign) {
        printIndent("<-");
        indent++;
        assign.name.accept(this);
        assign.value.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Isvoid isvoid) {
        printIndent("isvoid");
        indent++;
        isvoid.expr.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(New aNew) {
        printIndent("new");
        indent++;
        aNew.type.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(MethodCall methodCall) {
        printIndent("implicit dispatch");
        indent++;
        methodCall.method.accept(this);
        for (Expression expr : methodCall.args) {
            expr.accept(this);
        }
        indent--;
        return null;
    }

    @Override
    public <T> T visit(ClassMethodCall classMethodCall) {
        printIndent(".");
        indent++;
        classMethodCall.class_name.accept(this);
        if (classMethodCall.type.token != null) {
            classMethodCall.type.accept(this);
        }
        classMethodCall.method.accept(this);
        for (Expression expr : classMethodCall.args) {
            expr.accept(this);
        }
        indent--;
        return null;
    }

    @Override
    public Void visit(IF anIf) {
        printIndent("if");
        indent++;
        anIf.cond.accept(this);
        anIf.thenBranch.accept(this);
        anIf.elseBranch.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(While aWhile) {
        printIndent("while");
        indent++;
        aWhile.cond.accept(this);
        aWhile.body.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(LetVar letVar) {
        printIndent("local");
        indent++;
        letVar.name.accept(this);
        letVar.type.accept(this);
        if (letVar.init != null) {
            letVar.init.accept(this);
        }
        indent--;
        return null;
    }

    @Override
    public Void visit(Let let) {
        printIndent("let");
        indent++;
        for (LetVar var : let.vars) {
            var.accept(this);
        }
        let.body.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(CaseBranch caseBranch) {
    printIndent("case branch");
    indent++;
    caseBranch.name.accept(this);
    caseBranch.type.accept(this);
    caseBranch.body.accept(this);
    indent--;
    return null;
    }

    @Override
    public Void visit(Case aCase) {
        printIndent("case");
        indent++;
        aCase.expr.accept(this);
        for (CaseBranch branch : aCase.branches) {
            branch.accept(this);
        }
        indent--;
        return null;
    }

    @Override
    public Void visit(Block block) {
        printIndent("block");
        indent++;
        for (Expression expr : block.body) {
            expr.accept(this);
        }
        indent--;
        return null;
    }


}
