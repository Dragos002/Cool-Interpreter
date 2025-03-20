package cool.structures;

import cool.parser.ASTNode;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.LinkedHashMap;
import java.util.Map;

public class CaseBranchSymbol extends IdSymbol implements Scope {
    protected Map<String,Symbol> symbols = new LinkedHashMap<>();
    public Scope parent;
    public ASTNode.Type type;
    public Scope scope;
    public CaseBranchSymbol(String name) {
        super(name);
    }

    @Override
    public boolean add(Symbol sym) {

        symbols.put(sym.getName(), sym);

        return true;
    }

    @Override
    public Symbol lookup(String str) {
        var sym = symbols.get(str);

        if (sym != null)
            return sym;

        if (parent != null)
            return parent.lookup(str);

        return null;
    }

    @Override
    public Scope getParent() {
        return parent;
    }
}
