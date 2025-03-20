package cool.structures;

import cool.parser.ASTNode;
import cool.parser.ASTVisitor;
import org.antlr.runtime.Token;

public class ResolutionPassVisitor implements ASTVisitor<TypeSymbol> {

    Scope globalScope;
    public ResolutionPassVisitor(Scope globalScope) {
        this.globalScope = globalScope;
    }

    @Override
    public TypeSymbol visit(ASTNode.Id id) {
        var symbol = id.symbol;
        var scope = id.scope;
        if (id.scope.lookup(id.getToken().getText()) == null) {
            SymbolTable.error(id.getCtx(), id.getToken(), "Undefined identifier " + id.getToken().getText());
            return null;
        }
        id.symbol.type = ((IdSymbol)id.scope.lookup(id.getToken().getText())).type;
        return new TypeSymbol(id.symbol.type, id.getToken());
    }

    @Override
    public TypeSymbol visit(ASTNode.ClassDef classDef) {
        var classname = classDef.type.getToken().getText();
        var classSymbol = (ClassSymbol)globalScope.lookup(classname);
        ClassSymbol safeClassSymbol = classSymbol;
        if (classDef.parent != null) {
            var parent = classDef.parent;
            if (parent.getToken() != null) {
                var parentSymbol = (ClassSymbol)globalScope.lookup(parent.getToken().getText());
                classSymbol.inherited_scope = parentSymbol;
                if (parentSymbol == null) {
                    if (!ClassSymbol.nonInherit.contains(parent.getToken().getText())) {
                        SymbolTable.error(parent.getCtx(), parent.getToken(), "Class " + classname + " has undefined parent " + parent.getToken().getText());
                        return null;
                    }
                }
                if (ClassSymbol.nonInherit.contains(parent.getToken().getText())) {
                    SymbolTable.error(parent.getCtx(), parent.getToken(), "Class " + classname + " has illegal parent " + parent.getToken().getText());
                    return null;
                }
                if (parentSymbol != null && !ClassSymbol.nonInherit.contains(parent.getToken().getText()) ) {
                    var parentscope = classSymbol.inherited_scope;

                    while (parentscope != null) {
                        //System.out.println(parentscope.getName());
                        if (parentscope.getName().equals(classname)) {
                            SymbolTable.error(classDef.getCtx(), classDef.type.getToken(), "Inheritance cycle for class " + classname);
                            return null;
                        }
                        classSymbol = (ClassSymbol) classSymbol.getParent().lookup(parentscope.getName());
                        parentscope = classSymbol.inherited_scope;
                    }
                    //System.out.println("\n");
                }

            }
        }
//        globalScope = safeClassSymbol;
        //print number of features

        for (var feature : classDef.features) {
            feature.accept(this);
        }
//        globalScope = globalScope.getParent();
        return null;
    }

    @Override
    public TypeSymbol visit(ASTNode.Formal formal) {
        var methodSymbol = (FunctionSymbol) formal.scope;
        var classSymbol = (ClassSymbol) methodSymbol.getParent();

        if (formal.symbol.has_errors_in_definitionpass) {
            return null;
        }
        if (globalScope.lookup(formal.type.getToken().getText()) == null) {
            SymbolTable.error(formal.ctx, formal.type.getToken(), "Method " + methodSymbol + " of class " + classSymbol + " has formal parameter " + formal.name.getToken().getText() + " with undefined type " + formal.type.getToken().getText());
            return null;
        }
        return null;
    }

    @Override
    public TypeSymbol visit(ASTNode.MethodDef method) {
        var methodSymbol = (FunctionSymbol) method.scope;
        var classSymbol = (ClassSymbol) methodSymbol.getParent();
        if (methodSymbol.has_errors_in_definitionpass) {
            return null;
        }
        if (classSymbol.inherited_scope != null) {
            var parent = classSymbol.inherited_scope;
            while (parent != null) {
                var inheritedMethod = (FunctionSymbol)parent.lookup(method.name.getToken().getText());
                if (inheritedMethod != null ) {
                    if ( inheritedMethod.returnType != null && !method.returnType.getToken().getText().equals(inheritedMethod.returnType.getToken().getText())) {
                        SymbolTable.error(method.ctx, method.returnType.getToken(), "Class " + classSymbol + " overrides method " + method.name.getToken().getText() + " but changes return type from " + inheritedMethod.returnType.getToken().getText() + " to " + method.returnType.getToken().getText());
                        return null;
                    }
                    if (inheritedMethod.formals.size() != method.formals.size()) {
                        SymbolTable.error(method.ctx, method.name.getToken(), "Class " + classSymbol + " overrides method " + method.name.getToken().getText() + " with different number of formal parameters");
                        return null;
                    }
                    for (int i=0; i < method.formals.size(); i++) {
                        if (!inheritedMethod.formals.get(i).type.getToken().getText().equals(method.formals.get(i).type.getToken().getText())) {
                            SymbolTable.error(method.ctx, method.formals.get(i).type.getToken(), "Class " + classSymbol + " overrides method " + method.name.getToken().getText() + " but changes type of formal parameter " + method.formals.get(i).name.getToken().getText() + " from " + inheritedMethod.formals.get(i).type.getToken().getText() + " to " + method.formals.get(i).type.getToken().getText());
                            return null;
                        }
                    }
                }
                parent = parent.inherited_scope;
            }
        }
        for (var formal : method.formals) {
            formal.accept(this);
        }
        var bodytype = method.body.accept(this);
        var btype = "";
        if (bodytype != null) {
            btype = bodytype.type_symbol;
        }
        if (btype != null && !btype.equals("")) {
            if (!method.returnType.getToken().getText().equals(btype)) {

                if (method.body instanceof ASTNode.Id) {
                    var id = (ASTNode.Id) method.body;
                    var classsym = (ClassSymbol)globalScope.lookup(btype);
                    if (classsym.inherited_scope != null) {
                        var parent = classsym.inherited_scope;
                        while (parent !=null) {
                            if (parent.name.equals(method.returnType.getToken().getText())) {
                                return null;
                            }
                            parent = parent.inherited_scope;
                        }

                    }

                }
                if (method.returnType.getToken().getText().equals(("Object"))) {
                        return null;
                    }
                SymbolTable.error(method.ctx, method.body.getToken(), "Type " + btype + " of the body of method " + method.name.getToken().getText() + " is incompatible with declared return type " + method.returnType.getToken().getText());
                return null;
            }
        }
        return null;
    }

    @Override
    public TypeSymbol visit(ASTNode.AttrDef attr) {

        var classSymbol = (ClassSymbol)attr.scope;
        classSymbol = (ClassSymbol) globalScope.lookup(classSymbol.toString());
        if (attr.symbol.has_errors_in_definitionpass) {
            return null;
        }
        var type = attr.type.getToken().getText();
        attr.symbol.type = type;
        if (globalScope.lookup(type) == null) {
            SymbolTable.error(attr.ctx, attr.type.getToken(), "Class " + attr.scope + " has attribute " + attr.getToken().getText() + " with undefined type " + type);
            return null;
        }
        var parent = classSymbol.inherited_scope;
        while (parent != null) {
            var inheritedAttr = parent.lookup(attr.getToken().getText());
            if (inheritedAttr != null) {
                SymbolTable.error(attr.ctx, attr.getToken(), "Class " + attr.scope + " redefines inherited attribute " + attr.getToken().getText());
                return null;
            }
            parent = parent.inherited_scope;
        }
//        System.out.println("type of attr: " + attr.getToken().getText() + " is " + attr.type.getToken().getText());
        var init_type = "";
        if (attr.init != null) {
            var attr_iniitype = attr.init.accept(this);
            if (attr_iniitype != null) {
                init_type = attr_iniitype.type_symbol;
            }
            if (!init_type.equals("") && !init_type.equals(type)) {
                if (attr.init instanceof ASTNode.Id) {
                    var id = (ASTNode.Id) attr.init;
                    var classsym = (ClassSymbol)globalScope.lookup(id.symbol.type);
                    if (classsym.inherited_scope != null) {
                        var parentt = classsym.inherited_scope;
                        while (parentt !=null) {
                            if (parentt.getName().equals(type)) {
                                return new TypeSymbol(type, attr.getToken());
                            }
                            parentt = parentt.inherited_scope;
                        }

                    }

                }
                if (attr.init instanceof ASTNode.New) {
                    var neww = (ASTNode.New) attr.init;
                    var classsym = (ClassSymbol)globalScope.lookup(neww.type.getToken().getText());
                    if (classsym.inherited_scope != null) {
                        var parentt = classsym.inherited_scope;
                        while (parentt !=null) {
                            if (parentt.getName().equals(type)) {
                                return new TypeSymbol(type, attr.getToken());
                            }
                            parentt = parentt.inherited_scope;
                        }

                    }
                }
                SymbolTable.error(attr.ctx, attr.init.getToken(), "Type " + init_type + " of initialization expression of attribute " + attr.getToken().getText() + " is incompatible with declared type " + type);
                return null;
            }
        }
        return new TypeSymbol(type, attr.getToken());
    }

    @Override
    public TypeSymbol visit(ASTNode.IntLiteral intt) {

        return new TypeSymbol("Int", intt.getToken());
    }

    @Override
    public TypeSymbol visit(ASTNode.Feature feature) {
        return null;
    }

    @Override
    public TypeSymbol visit(ASTNode.Type type) {
        return null;
    }

    @Override
    public TypeSymbol visit(ASTNode.Program program) {
        globalScope = SymbolTable.globals;
        for (var classDef : program.classes) {
            classDef.accept(this);
        }
        return null;
    }

    @Override
    public TypeSymbol visit(ASTNode.StringLiteral stringLiteral) {
        return new TypeSymbol("String", stringLiteral.getToken());
    }

    @Override
    public TypeSymbol visit(ASTNode.Arithmetic arithmetic) {
        var lefttype = arithmetic.left.accept(this);
        String left_type = "";
        if (lefttype != null) {
            if (!lefttype.toString().equals("Int")) {
                left_type = lefttype.type_symbol;
            }
        }
        var righttype = arithmetic.right.accept(this);
        String right_type = "";
        if (righttype != null) {
            if (!righttype.toString().equals("Int")) {
                right_type = righttype.type_symbol;
            }
        }

        var op = arithmetic.getToken().getText();

        if (right_type != null && !right_type.equals("Int") && !right_type.equals("")) {
            SymbolTable.error(arithmetic.ctx, arithmetic.right.getToken(), "Operand of " + op + " has type " + right_type + " instead of Int");
            return null;
        }
        if (left_type != null && !left_type.equals("Int") && !left_type.equals("")) {
            SymbolTable.error(arithmetic.ctx, arithmetic.left.getToken(), "Operand of " + op + " has type " + left_type + " instead of Int");
            return null;
        }
        return new TypeSymbol("Int", arithmetic.ctx.start);
    }

    @Override
    public TypeSymbol visit(ASTNode.UnaryMinus unaryMinus) {
        var expr = unaryMinus.expr.accept(this);
        String type = "";
        if (expr != null) {
            type = expr.type_symbol;
        }
//        System.out.println("expr: " + unaryMinus.expr.getToken().getText() + " type: " + type);
        if (type != null && !type.equals("Int")) {
            SymbolTable.error(unaryMinus.ctx, unaryMinus.expr.getToken(), "Operand of ~ has type " + type + " instead of Int");
            return null;
        }
        return new TypeSymbol(expr.type_symbol, unaryMinus.getToken());
    }

    @Override
    public TypeSymbol visit(ASTNode.Comparison comparison) {
        var lefttype = comparison.left.accept(this);
        String left_type = "";
        if (lefttype != null) {
            if (!lefttype.toString().equals("Int")) {
                left_type = lefttype.type_symbol;
            }
        }
        var righttype = comparison.right.accept(this);
        String right_type = "";
        if (righttype != null) {
            if (!righttype.toString().equals("Int")) {
                right_type = righttype.type_symbol;
            }
        }
        var op = comparison.getToken().getText();
        if (op.equals("=")) {
            if (left_type != null && right_type != null) {
                if ((left_type.equals(("Int")) || left_type.equals("String") || left_type.equals("Bool")) && !left_type.equals(right_type)) {
                    SymbolTable.error(comparison.ctx, comparison.getToken(), "Cannot compare " + left_type + " with " + right_type);
                    return null;
                }
//                else if (right_type.equals(left_type)) {
//                    return new TypeSymbol("Bool", comparison.ctx.start);
//                }
            }
            return new TypeSymbol("Bool", comparison.ctx.start);
        }
        if (right_type != null && !right_type.equals("Int") && !right_type.equals("")) {
            SymbolTable.error(comparison.ctx, comparison.right.getToken(), "Operand of " + op + " has type " + right_type + " instead of Int");
            return null;
        }
        if (left_type != null && !left_type.equals("Int") && !left_type.equals("")) {
            SymbolTable.error(comparison.ctx, comparison.left.getToken(), "Operand of " + op + " has type " + left_type + " instead of Int");
            return null;
        }
        return new TypeSymbol("Bool", comparison.ctx.start);
    }

    @Override
    public TypeSymbol visit(ASTNode.Not not) {
        var expr = not.expr.accept(this);
        String type = "";
        if (expr != null) {
            type = expr.type_symbol;
        }
        if (type != null && !type.equals("Bool")) {
            SymbolTable.error(not.ctx, not.expr.getToken(), "Operand of not has type " + type + " instead of Bool");
            return null;
        }
        return new TypeSymbol(expr.type_symbol, not.getToken());
    }

    @Override
    public TypeSymbol visit(ASTNode.Assign assign) {
        if (((IdSymbol)assign.symbol).has_errors_in_definitionpass) {
            return null;
        }
        var def_var = assign.scope.lookup(assign.name.getToken().getText());
        def_var = (IdSymbol)def_var;
        var type = "";
        if (def_var != null) {
            type = ((IdSymbol) def_var).type;
        }
        if (assign.value == null) {
            return new TypeSymbol(type, assign.getToken());
        }
        var expr = assign.value.accept(this);
        String expr_type = "";
        if (expr != null) {
            expr_type = expr.type_symbol;
        }
//        System.out.println(assign.value.getClass());
        if (type != null && expr_type != null) {
            if (!type.equals(expr_type) && !expr_type.equals("")) {
                if (assign.value instanceof ASTNode.Id) {
                    var id = (ASTNode.Id) assign.value;
                    var classsym = (ClassSymbol)globalScope.lookup(id.symbol.type);
                    if (classsym.inherited_scope != null) {
                        var parent = classsym.inherited_scope;
                        while (parent !=null) {
                            if (parent.getName().equals(type)) {
                                return new TypeSymbol(type, assign.getToken());
                            }
                            parent = parent.inherited_scope;
                        }

                    }

                }
                if (assign.value instanceof ASTNode.New) {
                    var neww = (ASTNode.New) assign.value;
                    var classsym = (ClassSymbol)globalScope.lookup(neww.type.getToken().getText());
                    if (classsym.inherited_scope != null) {
                        var parent = classsym.inherited_scope;
                        while (parent !=null) {
                            if (parent.getName().equals(type)) {
                                return new TypeSymbol(type, assign.getToken());
                            }
                            parent = parent.inherited_scope;
                        }

                    }
                }
                if (assign.value instanceof ASTNode.Case) {
                    var acase = (ASTNode.Case) assign.value;

                    var classsym = (ClassSymbol)globalScope.lookup(acase.type);
                }
                SymbolTable.error(assign.ctx, assign.value.getToken(), "Type " + expr_type + " of assigned expression is incompatible with declared type " + type + " of identifier " + assign.name.getToken().getText());
                return null;
            }
            return new TypeSymbol(type, assign.getToken());
        }
        return null;
    }

    @Override
    public TypeSymbol visit(ASTNode.Isvoid isvoid) {
        var expr = isvoid.expr.accept(this);
        return new TypeSymbol("Bool", isvoid.getToken());
    }

    @Override
    public TypeSymbol visit(ASTNode.New aNew) {
        if (globalScope.lookup(aNew.type.getToken().getText()) == null) {
            SymbolTable.error(aNew.ctx, aNew.type.getToken(), "new is used with undefined type " + aNew.type.getToken().getText());
            return null;
        }
        return new TypeSymbol(aNew.type.getToken().getText(), aNew.getToken());
    }

    @Override
    public TypeSymbol visit(ASTNode.MethodCall methodCall) {
        return null;
    }

    @Override
    public <T> T visit(ASTNode.ClassMethodCall classMethodCall) {
        return null;
    }

    @Override
    public TypeSymbol visit(ASTNode.IF anIf) {
        var cond = anIf.cond.accept(this);
        var cond_type = "";
        if (cond != null) {
            cond_type = cond.type_symbol;
        }
        if (cond_type != null && !cond_type.equals("Bool")) {
            SymbolTable.error(anIf.ctx, anIf.cond.getToken(), "If condition has type " + cond_type + " instead of Bool");
            return new TypeSymbol("Object", anIf.getToken());
        }
        var then_branch = anIf.thenBranch.accept(this);
        var then_type = "";
        if (then_branch != null) {
            then_type = then_branch.type_symbol;
        }
        var else_branch = anIf.elseBranch.accept(this);
        var else_type = "";
        if (else_branch != null) {
            else_type = else_branch.type_symbol;
        }
        if (then_type !=null && else_type != null && !then_type.equals("") && !else_type.equals("")) {
            ClassSymbol then_class = (ClassSymbol)globalScope.lookup(then_type);
            ClassSymbol else_class = (ClassSymbol)globalScope.lookup(else_type);
            var common_anscestor = TypeSymbol.look_common_anscestor(then_class, else_class);
            return new TypeSymbol(common_anscestor, anIf.getToken());
        }
        return new TypeSymbol("Object", anIf.getToken());
    }

    @Override
    public TypeSymbol visit(ASTNode.While aWhile) {
        var cond = aWhile.cond.accept(this);
        var cond_type = "";
        if (cond != null) {
            cond_type = cond.type_symbol;
        }
        if (cond_type != null && !cond_type.equals("Bool")) {
            SymbolTable.error(aWhile.ctx, aWhile.cond.getToken(), "While condition has type " + cond_type + " instead of Bool");
            return new TypeSymbol("Object", aWhile.getToken());
        }
        aWhile.body.accept(this);
        return new TypeSymbol("Object", aWhile.getToken());
    }

    @Override
    public TypeSymbol visit(ASTNode.LetVar letVar) {
        var type = letVar.type.getToken().getText();


        if (globalScope.lookup(type) == null) {
            SymbolTable.error(letVar.ctx, letVar.type.getToken(), "Let variable " + letVar.name.getToken().getText() + " has undefined type " + type);
            return null;
        }


        var let_type = "";
        if (letVar.init != null) {
            var lettype = letVar.init.accept(this);
            if (lettype != null) {
                let_type = lettype.type_symbol;
            }
            if (letVar.init != null && !let_type.equals("")) {
                if (!letVar.type.getToken().getText().equals(let_type)) {
                    if (letVar.scope instanceof LetSymbol) {
                        var letSymbol = (LetSymbol) letVar.scope;
                        letVar.scope.add(letVar.symbol);
                    }
                    if (letVar.init instanceof ASTNode.Id) {
                        var id = (ASTNode.Id) letVar.init;
                        var classsym = (ClassSymbol)globalScope.lookup(id.symbol.type);
                        if (classsym.inherited_scope != null) {
                            var parent = classsym.inherited_scope;
                            while (parent !=null) {
                                if (parent.type.equals(let_type)) {

                                    return new TypeSymbol(let_type, letVar.getToken());
                                }
                                parent = parent.inherited_scope;
                            }

                        }

                    }
                    if (letVar.init instanceof ASTNode.New) {
                        var neww = (ASTNode.New) letVar.init;
                        var classsym = (ClassSymbol)globalScope.lookup(neww.type.getToken().getText());
//                        System.out.println("Type of declaration : " + letVar.type.getToken().getText() + " for " + letVar.name.getToken().getText());
//                        System.out.println("Type of initialization : " + neww.type.getToken().getText());
                        if (classsym.inherited_scope != null) {
                            var parent = classsym.inherited_scope;
                            while (parent !=null) {
                                if (parent.name.equals(letVar.type.getToken().getText())) {
                                    return new TypeSymbol(let_type, letVar.getToken());

                                }

                                parent = parent.inherited_scope;
                            }

                        }
                    }
                    SymbolTable.error(letVar.ctx, letVar.init.getToken(), "Type " + let_type + " of initialization expression of identifier " + letVar.name.getToken().getText() + " is incompatible with declared type " + type);
                    return null;
                }
            }

        }
        if (letVar.scope instanceof LetSymbol) {
            var letSymbol = (LetSymbol) letVar.scope;
            letVar.scope.add(letVar.symbol);
        }

        return new TypeSymbol(let_type, letVar.getToken());
    }

    @Override
    public TypeSymbol visit(ASTNode.Let let) {
        for (var var : let.vars) {
            var.accept(this);
        }
        var lettype = let.body.accept(this);
        var let_type = "";
        if (lettype != null && !lettype.type_symbol.equals("")) {
            let_type = lettype.type_symbol;
            return new TypeSymbol(let_type, let.getToken());
        }
        return null;
    }

    @Override
    public TypeSymbol visit(ASTNode.CaseBranch caseBranch) {
        var caseSymbol = (CaseBranchSymbol) caseBranch.scope;
        if (caseSymbol.has_errors_in_definitionpass) {
            return null;
        }
        var type = caseBranch.type.getToken().getText();
        if (globalScope.lookup(type) == null) {
            SymbolTable.error(caseBranch.ctx, caseBranch.type.getToken(), "Case variable " + caseBranch.name.getToken().getText() + " has undefined type " + type);
            return null;
        }
        var body = caseBranch.body.accept(this);
        var body_type = "";
        if (body != null) {
            body_type = body.type_symbol;
        }
        if (!body_type.equals("")) {
            return new TypeSymbol(body_type, caseBranch.getToken());
        }
        return new TypeSymbol("", caseBranch.getToken());
    }

    @Override
    public TypeSymbol visit(ASTNode.Case aCase) {
        aCase.expr.accept(this);
        var case_ret_type = "";
        for (var branch : aCase.branches) {
            var branch_t = branch.accept(this);
            var branch_type = "";
            if (branch_t != null) {
                branch_type = branch_t.type_symbol;
                if (branch_type != null && !branch_type.equals("")) {
                    if (case_ret_type.equals("")) {
                        case_ret_type = branch_type;
                    } else {
                        case_ret_type = TypeSymbol.look_common_anscestor((ClassSymbol)globalScope.lookup(case_ret_type), (ClassSymbol)globalScope.lookup(branch_type));
//                        System.out.println("case_ret_type: " + case_ret_type);
                    }
                }
            }

        }
//        System.out.println();
        if (!case_ret_type.equals("")) {
            aCase.type = case_ret_type;
            return new TypeSymbol(case_ret_type, aCase.getToken());
        }
        aCase.type = case_ret_type;
        return new TypeSymbol("Object", aCase.getToken());
    }

    @Override
    public TypeSymbol visit(ASTNode.Block block) {
        for (var expr : block.body) {
            if (expr != block.body.getLast())
                expr.accept(this);
            else if (expr == block.body.getLast()){
                return new TypeSymbol(expr.accept(this).type_symbol, block.getToken());
            }
        }
        return null;
    }
}
