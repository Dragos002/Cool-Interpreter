package cool.structures;

import java.util.LinkedHashMap;
import java.util.Map;

public class LetSymbol extends IdSymbol implements Scope {
    public Map<String,Symbol> symbols = new LinkedHashMap<>();
    public Scope parent;

    public LetSymbol(String name) {
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
