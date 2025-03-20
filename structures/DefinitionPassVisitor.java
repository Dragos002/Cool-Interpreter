package cool.structures;
import org.antlr.v4.runtime.misc.NotNull;
import cool.parser.*;
public class DefinitionPassVisitor implements ASTVisitor<Void> {

    Scope currentScope = null;
    @Override
    public Void visit(ASTNode.Id id) {
        var sym = new IdSymbol(id.getToken().getText());
        id.symbol = sym;
        if (id.getToken().getText().equals("self")) {
            currentScope.add(sym);
        }
        if (id.getToken().getText().equals("true") || id.getToken().getText().equals("false")) {
            sym.type = "Bool";
            currentScope.add(sym);
        }
        id.scope = currentScope;
        return null;

    }

    @Override
    public Void visit(ASTNode.ClassDef classDef) {
        var id = classDef.type;
        var classname = id.getToken().getText();
        var classSymbol = new ClassSymbol(id.getToken().getText(), currentScope);

        if ( classname.equals("SELF_TYPE") ) {
            SymbolTable.error(id.getCtx(), id.getToken(), "Class has illegal name SELF_TYPE");
            return null;
        }
         if (!currentScope.add(classSymbol)) {
            SymbolTable.error(id.getCtx(), id.getToken(), "Class " + id.getToken().getText() + " is redefined");
            return null;
        }
        if (classDef.parent != null) {
            var parent = classDef.parent;
            if (parent.getToken() != null) {
                classSymbol.inherited_scope = (ClassSymbol) currentScope.lookup(parent.getToken().getText());
                if (classSymbol.inherited_scope == null) {
                    classSymbol.inherited_scope = new ClassSymbol(parent.getToken().getText(), currentScope);

                }
            }
        }
        currentScope = classSymbol;
        for (var feature : classDef.features) {
            feature.accept(this);
        }
        currentScope = currentScope.getParent();
        return null;
    }

    @Override
    public Void visit(ASTNode.Formal formal) {
        var sym = new IdSymbol(formal.getToken().getText(), formal.type.getToken().getText());
        formal.symbol = sym;
        formal.scope = currentScope;
        if (formal.name.getToken().getText().equals("self")) {
            formal.symbol.has_errors_in_definitionpass = true;
            SymbolTable.error(formal.ctx, formal.name.getToken(), "Method " + currentScope + " of class " + currentScope.getParent() + " has formal parameter with illegal name self");
            return null;
        }
        if (formal.type.getToken().getText().equals("SELF_TYPE")) {
            formal.symbol.has_errors_in_definitionpass = true;
            SymbolTable.error(formal.ctx, formal.type.getToken(), "Method " + currentScope + " of class " + currentScope.getParent() + " has formal parameter " + formal.name.getToken().getText() + " with illegal type SELF_TYPE");
            return null;
        }
        if (!currentScope.add(sym)) {
            formal.symbol.has_errors_in_definitionpass = true;
            SymbolTable.error(formal.ctx, formal.name.getToken(), "Method " + currentScope + " of class " + currentScope.getParent() + " redefines formal parameter " + formal.name.getToken().getText());
        }

        return null;
    }

    @Override
    public Void visit(ASTNode.MethodDef method) {
        var methodsym = new FunctionSymbol((ClassSymbol) currentScope, method.name.getToken().getText());
        method.symbol = methodsym;
        method.scope = methodsym;
        if (!currentScope.add(methodsym)) {
            method.symbol.has_errors_in_definitionpass = true;
            SymbolTable.error(method.ctx, method.name.getToken(), "Class " + currentScope + " redefines method " + method.name.getToken().getText());
            return null;
        }
        currentScope = methodsym;
        if (method.formals != null) {
            for (var formal : method.formals) {
                method.symbol.formals.add(formal);
                method.symbol.returnType = method.returnType;
                formal.accept(this);
            }
        }
        if (method.body != null) {
            method.body.accept(this);
        }
        currentScope = currentScope.getParent();
        return null;
    }

    @Override
    public Void visit(ASTNode.AttrDef attr) {
        var sym = new IdSymbol(attr.getToken().getText(), attr.type.getToken().getText());
        attr.symbol = sym;
        attr.scope = currentScope;
        attr.symbol.type = attr.type.getToken().getText();
        if (attr.getToken().getText().equals("self")) {
            attr.symbol.has_errors_in_definitionpass = true;
            SymbolTable.error(attr.ctx, attr.getToken(), "Class " + attr.scope + " has attribute with illegal name self");
            return null;
        }
        if (!currentScope.add(sym)) {
            attr.symbol.has_errors_in_definitionpass = true;
            SymbolTable.error(attr.ctx, attr.getToken(), "Class " + currentScope + " redefines attribute " + attr.getToken().getText());
        }
        if (attr.init != null) {
            attr.init.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ASTNode.IntLiteral intt) {
        return null;
    }

    @Override
    public Void visit(ASTNode.Feature feature) {
        return null;
    }

    @Override
    public Void visit(ASTNode.Type type) {
        return null;
    }

    @Override
    public Void visit(ASTNode.Program program) {
        currentScope = SymbolTable.globals;
        currentScope.add(ClassSymbol.OBJECT);
        currentScope.add(ClassSymbol.IO);
        currentScope.add(ClassSymbol.INT);
        currentScope.add(ClassSymbol.STRING);
        currentScope.add(ClassSymbol.BOOL);
        for (var classDef : program.classes) {
            classDef.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ASTNode.StringLiteral stringLiteral) {
        return null;
    }

    @Override
    public Void visit(ASTNode.Arithmetic arithmetic) {
        arithmetic.left.accept(this);
        arithmetic.right.accept(this);

        return null;
    }

    @Override
    public Void visit(ASTNode.UnaryMinus unaryMinus) {
        unaryMinus.expr.accept(this);
        return null;
    }

    @Override
    public Void visit(ASTNode.Comparison comparison) {
        comparison.left.accept(this);
        comparison.right.accept(this);
        return null;
    }

    @Override
    public Void visit(ASTNode.Not not) {
        not.expr.accept(this);
        return null;
    }

    @Override
    public Void visit(ASTNode.Assign assign) {
        assign.symbol = new IdSymbol(assign.name.getToken().getText());
        if (assign.name.getToken().getText().equals("self")) {
            ((IdSymbol) assign.symbol).has_errors_in_definitionpass = true;
            SymbolTable.error(assign.ctx, assign.name.getToken(), "Cannot assign to self");
            return null;
        }
        assign.scope = currentScope;
        if (assign.value != null)
            assign.value.accept(this);
        return null;
    }

    @Override
    public Void visit(ASTNode.Isvoid isvoid) {
        isvoid.expr.accept(this);
        return null;
    }

    @Override
    public Void visit(ASTNode.New aNew) {
        aNew.scope = currentScope;
        return null;
    }

    @Override
    public Void visit(ASTNode.MethodCall methodCall) {
        return null;
    }

    @Override
    public <T> T visit(ASTNode.ClassMethodCall classMethodCall) {
        return null;
    }

    @Override
    public Void visit(ASTNode.IF anIf) {
        anIf.scope = currentScope;
        anIf.cond.accept(this);
        anIf.thenBranch.accept(this);
        anIf.elseBranch.accept(this);
        return null;
    }

    @Override
    public Void visit(ASTNode.While aWhile) {
        aWhile.scope = currentScope;
        aWhile.cond.accept(this);
        aWhile.body.accept(this);
        return null;
    }

    @Override
    public Void visit(ASTNode.LetVar letVar) {
        var letvarsym = new IdSymbol(letVar.getToken().getText(), letVar.type.getToken().getText());
        letVar.symbol = letvarsym;
        if (letVar.init != null)
            letVar.init.accept(this);
        letVar.scope = currentScope;
        letvarsym.type = letVar.type.getToken().getText();
        if (letVar.getToken().getText().equals("self")) {
            letvarsym.has_errors_in_definitionpass = true;
            SymbolTable.error(letVar.ctx, letVar.getToken(), "Let variable has illegal name self");
            return null;
        }

        return null;
    }

    @Override
    public Void visit(ASTNode.Let let) {
        var letsym = new LetSymbol(let.getToken().getText());
        letsym.parent = currentScope;
        currentScope = letsym;
        for (var letvar : let.vars) {
            letvar.accept(this);
        }
        let.body.accept(this);
        currentScope = currentScope.getParent();
        return null;
    }

    @Override
    public Void visit(ASTNode.CaseBranch caseBranch) {
        var casebrsymbol = new CaseBranchSymbol(caseBranch.name.getToken().getText());
        casebrsymbol.parent = currentScope;
        caseBranch.scope = casebrsymbol;
        currentScope = casebrsymbol;
        if (caseBranch.name.getToken().getText().equals("self")) {
            casebrsymbol.has_errors_in_definitionpass = true;
            SymbolTable.error(caseBranch.ctx, caseBranch.name.getToken(), "Case variable has illegal name self");
            return null;
        }
        if (caseBranch.type.getToken().getText().equals("SELF_TYPE")) {
            casebrsymbol.has_errors_in_definitionpass = true;
            SymbolTable.error(caseBranch.ctx, caseBranch.type.getToken(), "Case variable " + caseBranch.name.getToken().getText() + " has illegal type SELF_TYPE");
            return null;
        }
        caseBranch.body.accept(this);
        currentScope = currentScope.getParent();
        return null;
    }

    @Override
    public Void visit(ASTNode.Case aCase) {
        aCase.expr.accept(this);
        for (var branch : aCase.branches) {
            branch.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ASTNode.Block block) {
        block.scope = currentScope;
        for (var expr : block.body) {
            expr.accept(this);
        }
        return null;
    }
}
