package cool.parser;
import org.antlr.v4.runtime.*;

public interface ASTVisitor<T> {
    T visit(ASTNode.Id id);
    T visit(ASTNode.ClassDef classDef);

    T visit(ASTNode.Formal formal);
    T visit(ASTNode.MethodDef method);
    T visit(ASTNode.AttrDef attr);

    T visit(ASTNode.IntLiteral intt);


    T visit(ASTNode.Feature feature);
    T visit(ASTNode.Type type);

    T visit(ASTNode.Program program);

    T visit(ASTNode.StringLiteral stringLiteral);

    T visit(ASTNode.Arithmetic arithmetic);

    T visit(ASTNode.UnaryMinus unaryMinus);

    T visit(ASTNode.Comparison comparison);

    T visit(ASTNode.Not not);

    T visit(ASTNode.Assign assign);

    T visit(ASTNode.Isvoid isvoid);

    T visit(ASTNode.New aNew);

    T visit(ASTNode.MethodCall methodCall);

    <T> T visit(ASTNode.ClassMethodCall classMethodCall);

    T visit(ASTNode.IF anIf);

    T visit(ASTNode.While aWhile);


    T visit(ASTNode.LetVar letVar);

    T visit(ASTNode.Let let);

    T visit(ASTNode.CaseBranch caseBranch);

    T visit(ASTNode.Case aCase);

    T visit(ASTNode.Block block);
}