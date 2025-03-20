package cool.structures;

import cool.parser.ASTNode;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class FunctionSymbol extends IdSymbol implements Scope {
    protected ClassSymbol parent;
    protected Map<String,Symbol> symbols = new LinkedHashMap<>();

    public LinkedList<ASTNode.Formal> formals = new LinkedList<>();

    public ASTNode.Type returnType = null;

    public boolean has_errors_in_definitionpass = false;

    public FunctionSymbol(ClassSymbol parent, String name) {
        super(name);
        this.parent = parent;

    }

    @Override
    public boolean add(Symbol sym) {
        if (symbols.containsKey(sym.getName()))
            return false;

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
    public Map<String, Symbol> getFormals() {
        return symbols;
    }

}
