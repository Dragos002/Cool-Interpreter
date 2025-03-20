package cool.parser;
import cool.structures.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import java.util.*;

public abstract class ASTNode {
    protected Token token;
    public String debugStr = null;

    ASTNode(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return null;
    }


    public abstract static class Definition extends ASTNode {
        Definition(Token token) {
            super(token);
        }
    }

    public abstract static class Feature extends ASTNode {
        Feature(Token token) {
            super(token);
        }
    }

    public static abstract class Expression extends ASTNode {
        Expression(Token token) {
            super(token);
        }
    }

    public static class Id extends Expression {
        public IdSymbol symbol;
        public Scope scope;
        public ParserRuleContext ctx;
//        Id(Token token) {
//            super(token);
//        }

        Id(Token token, ParserRuleContext ctx) {
            super(token);
            this.ctx = ctx;
        }
        public <T> T accept(ASTVisitor<T> visitor) {
            return visitor.visit(this);
        }

        public IdSymbol getSymbol() {
            return symbol;
        }

        public ParserRuleContext getCtx() {
            return ctx;
        }

        public void setSymbol(IdSymbol symbol) {
            this.symbol = symbol;
        }

        public Scope getScope() {
            return scope;
        }

        public void setScope(Scope scope) {
            this.scope = scope;
        }
    }

    public static class Type extends ASTNode {
        Type(Token token) {
            super(token);
        }

        public <T> T accept(ASTVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Formal extends ASTNode {
       public Id name;
       public Type type;
       public IdSymbol symbol = null;
       public Scope scope = null;

       public ParserRuleContext ctx;
        Formal(Id name, Type type, Token token, ParserRuleContext ctx) {
            super(token);
            this.name = name;
            this.type = type;
            this.ctx = ctx;
        }

        public <T> T accept(ASTVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class MethodDef extends Feature {
        public Id name;
        public LinkedList<Formal> formals;
        public Type returnType;
        public Expression body;

        public FunctionSymbol symbol = null;
        public Scope scope = null;

        public ParserRuleContext ctx;

        MethodDef(Id name, LinkedList<Formal> formals, Type returnType, Expression body, Token token, ParserRuleContext ctx) {
            super(token);
            this.name = name;
            this.formals = formals;
            this.returnType = returnType;
            this.body = body;
            this.ctx = ctx;
        }

        public <T> T accept(ASTVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class AttrDef extends Feature {
        public Id name;
        public Type type;
        public Expression init;
        public IdSymbol symbol = null;

        public Scope scope = null;
        public ParserRuleContext ctx;
        AttrDef(Id name, Type type, Expression init, Token token, ParserRuleContext ctx) {
            super(token);
            this.name = name;
            this.type = type;
            this.init = init;
            this.ctx = ctx;
        }

        public <T> T accept(ASTVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class ClassDef extends Definition {
        public Id type;
        public Id parent;
        public ParserRuleContext ctx;
        public LinkedList<Feature> features;
        public ClassSymbol scope = null;

        ClassDef(Id type, Id parent, LinkedList<Feature> features, Token token, ParserRuleContext ctx) {
            super(token);
            this.type = type;
            this.parent = parent;
            this.features = features;
            this.ctx = ctx;
        }

        public ParserRuleContext getCtx() {
            return ctx;
        }

        public <T> T accept(ASTVisitor<T> visitor) {
            return visitor.visit(this);
        }


    }

    public static class IntLiteral extends Expression {
        IntLiteral(Token token) {
            super(token);
        }

        @Override
        public <T> T accept(ASTVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class StringLiteral extends Expression {
        StringLiteral(Token token) {
            super(token);
        }

        @Override
        public <T> T accept(ASTVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Arithmetic extends Expression {
        public Expression left;
        public Expression right;
        public ParserRuleContext ctx;
        Arithmetic(Expression left, Expression right, Token op, ParserRuleContext ctx) {
            super(op);
            this.left = left;
            this.right = right;
            this.ctx = ctx;
        }

        @Override
        public <T> T accept(ASTVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class UnaryMinus extends Expression {
        public Expression expr;
        public ParserRuleContext ctx;

        UnaryMinus(Expression expr, Token token, ParserRuleContext ctx) {
            super(token);
            this.expr = expr;
            this.ctx = ctx;
        }

        @Override
        public <T> T accept(ASTVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Comparison extends Expression {
        public Expression left;
        public Expression right;
        public ParserRuleContext ctx;

        Comparison(Expression left, Expression right, Token op, ParserRuleContext ctx) {
            super(op);
            this.left = left;
            this.right = right;
            this.ctx = ctx;
        }

        @Override
        public <T> T accept(ASTVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Not extends Expression {
        public Expression expr;
        public ParserRuleContext ctx;
        Not(Expression expr, Token token, ParserRuleContext ctx) {
            super(token);
            this.expr = expr;
            this.ctx = ctx;
        }

        @Override
        public <T> T accept(ASTVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Assign extends Expression {
        public Id name;
        public Expression value;
        public ParserRuleContext ctx;
        public Scope scope = null;
        public Symbol symbol = null;
        Assign(Id name, Expression value, Token token, ParserRuleContext ctx) {
            super(token);
            this.name = name;
            this.value = value;
            this.ctx = ctx;
        }

        @Override
        public <T> T accept(ASTVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Isvoid extends Expression {
       public Expression expr;
       public ParserRuleContext ctx;
        Isvoid(Expression expr, Token token, ParserRuleContext ctx) {
            super(token);
            this.expr = expr;
            this.ctx = ctx;
        }

        @Override
        public <T> T accept(ASTVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class New extends Expression {
        public Type type;
        public ParserRuleContext ctx;
        public Scope scope = null;
        New(Type type, Token token, ParserRuleContext ctx) {
            super(token);
            this.type = type;
            this.ctx = ctx;
        }

        @Override
        public <T> T accept(ASTVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class MethodCall extends Expression {
        Id method;
        LinkedList<Expression> args;

        MethodCall(Id method, LinkedList<Expression> args, Token token) {
            super(token);
            this.method = method;
            this.args = args;
        }

        @Override
        public <T> T accept(ASTVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class ClassMethodCall extends Expression {
        Expression class_name;

        Type type;
        Id method;
        LinkedList<Expression> args;

        ClassMethodCall(Expression class_name, Type type, Id method, LinkedList<Expression> args, Token token) {
            super(token);
            this.class_name = class_name;
            this.type = type;
            this.method = method;
            this.args = args;
        }

        @Override
        public <T> T accept(ASTVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }


    public static class IF extends Expression {
        public Expression cond;
        public Expression thenBranch;
        public Expression elseBranch;
        public Scope scope = null;
        public ParserRuleContext ctx;
        IF(Expression cond, Expression thenBranch, Expression elseBranch, Token token, ParserRuleContext ctx) {
            super(token);
            this.cond = cond;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
            this.ctx = ctx;
        }

        @Override
        public <T> T accept(ASTVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class While extends Expression {
        public Expression cond;
        public Expression body;
        public Scope scope = null;
        public ParserRuleContext ctx;
        While(Expression cond, Expression body, Token token, ParserRuleContext ctx) {
            super(token);
            this.cond = cond;
            this.body = body;
            this.ctx = ctx;
        }

        @Override
        public <T> T accept(ASTVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class LetVar extends ASTNode {
        public Id name;
        public Type type;
        public Expression init;

        public Symbol symbol = null;

        public Scope scope = null;
        public ParserRuleContext ctx;
        LetVar(Id name, Type type, Token token, ParserRuleContext ctx) {
            super(token);
            this.name = name;
            this.type = type;
            this.ctx = ctx;
        }

        LetVar(Id name, Type type, Expression init, Token token, ParserRuleContext ctx) {
            super(token);
            this.name = name;
            this.type = type;
            this.init = init;
            this.ctx = ctx;
        }

        public <T> T accept(ASTVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Let extends Expression {
        public LinkedList<LetVar> vars;
        public Expression body;
        public Scope scope = null;
        public ParserRuleContext ctx;

        public IdSymbol symbol = null;

        Let(LinkedList<LetVar> vars, Expression body, Token token, ParserRuleContext ctx) {
            super(token);
            this.vars = vars;
            this.body = body;
            this.ctx = ctx;
        }

        @Override
        public <T> T accept(ASTVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class CaseBranch extends ASTNode {
        public Id name;
        public Type type;
        public Expression body;

        public ParserRuleContext ctx;

        public Scope scope = null;

        CaseBranch(Id name, Type type, Expression body, Token token, ParserRuleContext ctx) {
            super(token);
            this.name = name;
            this.type = type;
            this.body = body;
            this.ctx = ctx;
        }

        public <T> T accept(ASTVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Case extends Expression {
        public Expression expr;
        public LinkedList<CaseBranch> branches;
        public String type;
        Case(Expression expr, LinkedList<CaseBranch> branches, Token token) {
            super(token);
            this.expr = expr;
            this.branches = branches;
        }

        @Override
        public <T> T accept(ASTVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Block extends Expression {
        public LinkedList<Expression> body;
        public Scope scope = null;
        public ParserRuleContext ctx;
        Block(LinkedList<Expression> body, Token token, ParserRuleContext ctx) {
            super(token);
            this.body = body;
            this.ctx = ctx;
        }

        @Override
        public <T> T accept(ASTVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Program extends ASTNode {
        public LinkedList<ClassDef> classes;

        Program(LinkedList<ClassDef> classes, Token token) {
            super(token);
            this.classes = classes;
        }

        public <T> T accept(ASTVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }
}