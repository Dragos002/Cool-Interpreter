package cool.parser;
import org.antlr.v4.runtime.Token;

import java.util.*;
import cool.parser.ASTNode.*;
public class ASTConstructionVisitor extends CoolParserBaseVisitor<ASTNode> {
    @Override
    public ASTNode visitId(CoolParser.IdContext ctx) {
        return new Id(ctx.ID().getSymbol(), ctx);
    }

    @Override
    public ASTNode visitClass_def(CoolParser.Class_defContext ctx) {
        LinkedList<ASTNode.Feature> features = new LinkedList<>();
        for (CoolParser.FeatureContext featureCtx : ctx.feature()) {
            features.add((ASTNode.Feature) visit(featureCtx));
        }
        return new ASTNode.ClassDef(new ASTNode.Id(ctx.type,ctx), new ASTNode.Id(ctx.parent,ctx), features,ctx.start, ctx);
    }

    @Override
    public ASTNode visitFormal(CoolParser.FormalContext ctx) {
        return new Formal(new Id(ctx.name,ctx), new Type(ctx.type), ctx.start,ctx);
    }

    @Override
    public ASTNode visitProgram(CoolParser.ProgramContext ctx) {
        LinkedList<ClassDef> classes = new LinkedList<>();
        for (var child : ctx.children) {
            ASTNode node = visit(child);
            if (node != null){
                node.debugStr = child.getText();
                classes.add((ClassDef) node);
            }
        }
        return new Program(classes, ctx.start);
    }

    @Override
    public ASTNode visitAttribute_def(CoolParser.Attribute_defContext ctx) {
        if (ctx.value != null) {
            return new AttrDef(new Id(ctx.name,ctx), new Type(ctx.type), (Expression) visit(ctx.value), ctx.start,ctx);
        } else {
            return new AttrDef(new Id(ctx.name,ctx), new Type(ctx.type), null, ctx.start,ctx);
        }
    }

    @Override
    public ASTNode visitInt(CoolParser.IntContext ctx) {
        return new IntLiteral(ctx.INT().getSymbol());
    }

    @Override
    public ASTNode visitString(CoolParser.StringContext ctx) {
        return new StringLiteral(ctx.STRING().getSymbol());
    }


    @Override
    public ASTNode visitMethod_def(CoolParser.Method_defContext ctx) {
        LinkedList<Formal> formals = new LinkedList<>();
        for (CoolParser.FormalContext formalCtx : ctx.formal()) {
            formals.add((Formal) visit(formalCtx));
        }
        return new MethodDef(new Id(ctx.name,ctx), formals, new Type(ctx.type), (Expression) visit(ctx.body), ctx.start, ctx);
    }

    @Override
    public ASTNode visitArithmetic(CoolParser.ArithmeticContext ctx) {
        return new Arithmetic((Expression) visit(ctx.left), (Expression) visit(ctx.right), ctx.op, ctx);
    }

    @Override
    public ASTNode visitParen_expr(CoolParser.Paren_exprContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public ASTNode visitUnaryMinus(CoolParser.UnaryMinusContext ctx) {
        return new UnaryMinus((Expression) visit(ctx.expr()), ctx.start, ctx);
    }

    @Override
    public ASTNode visitComparison(CoolParser.ComparisonContext ctx) {
        return new Comparison((Expression) visit(ctx.left), (Expression) visit(ctx.right), ctx.op,ctx);
    }

    @Override
    public ASTNode visitNot(CoolParser.NotContext ctx) {
        return new Not((Expression) visit(ctx.expr()), ctx.start,ctx);
    }

    @Override
    public ASTNode visitAssign(CoolParser.AssignContext ctx) {
        return new Assign(new Id(ctx.name,ctx), (Expression) visit(ctx.assign), ctx.start,ctx);
    }

    @Override
    public ASTNode visitIsvoid(CoolParser.IsvoidContext ctx) {
        return new Isvoid((Expression) visit(ctx.expr()), ctx.start,ctx);
    }

    @Override
    public ASTNode visitNew(CoolParser.NewContext ctx) {
        return new New(new Type(ctx.type), ctx.start,ctx);
    }

    @Override
    public ASTNode visitMethod_call(CoolParser.Method_callContext ctx) {
        LinkedList<Expression> args = new LinkedList<>();
        for (CoolParser.ExprContext exprCtx : ctx.expr()) {
            args.add((Expression) visit(exprCtx));
        }
        return new MethodCall(new Id(ctx.name,ctx), args, ctx.start);
    }

    @Override
    public ASTNode visitClass_method_call(CoolParser.Class_method_callContext ctx) {
        LinkedList<Expression> args = new LinkedList<>();
        for (CoolParser.ExprContext exprCtx : ctx.args) {
            args.add((Expression) visit(exprCtx));
        }
        return new ClassMethodCall((Expression) visit(ctx.class_name),new Type(ctx.type) ,new Id(ctx.func,ctx), args, ctx.start);
    }

    @Override
    public ASTNode visitIf(CoolParser.IfContext ctx) {
        return new IF((Expression) visit(ctx.cond), (Expression) visit(ctx.thenBranch), (Expression) visit(ctx.elseBranch), ctx.start,ctx);
    }

    @Override
    public ASTNode visitWhile(CoolParser.WhileContext ctx) {
        return new While((Expression) visit(ctx.cond), (Expression) visit(ctx.body), ctx.start,ctx);
    }

    @Override
    public ASTNode visitLet_vars(CoolParser.Let_varsContext ctx) {
        if (ctx.expr() != null) {
            return new LetVar(new Id(ctx.name,ctx), new Type(ctx.type), (Expression) visit(ctx.value), ctx.start, ctx);
        } else {
            return new LetVar(new Id(ctx.name,ctx), new Type(ctx.type), ctx.start,ctx);
        }
    }

    @Override
    public ASTNode visitLet(CoolParser.LetContext ctx) {
        LinkedList<LetVar> vars = new LinkedList<>();
        for (CoolParser.Let_varsContext varCtx : ctx.let_vars()) {
            vars.add((LetVar) visit(varCtx));
        }
        return new Let(vars, (Expression) visit(ctx.body), ctx.start,ctx);
    }

    @Override
    public ASTNode visitCase_branch(CoolParser.Case_branchContext ctx) {
        return new CaseBranch(new Id(ctx.name,ctx), new Type(ctx.type), (Expression) visit(ctx.body), ctx.start,ctx);
    }

    @Override
    public ASTNode visitCase(CoolParser.CaseContext ctx) {
        LinkedList<CaseBranch> branches = new LinkedList<>();
        for (CoolParser.Case_branchContext branchCtx : ctx.case_branch()) {
            branches.add((CaseBranch) visit(branchCtx));
        }
        return new Case((Expression) visit(ctx.expr()), branches, ctx.start);
    }

    @Override
    public ASTNode visitBlock(CoolParser.BlockContext ctx) {
        LinkedList<Expression> exprs = new LinkedList<>();
        for (CoolParser.ExprContext exprCtx : ctx.expr()) {
            exprs.add((Expression) visit(exprCtx));
        }
        return new Block(exprs, ctx.start,ctx);
    }

}