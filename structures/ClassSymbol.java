package cool.structures;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ClassSymbol extends IdSymbol implements Scope {

    protected Scope parent;

    static public ClassSymbol OBJECT;
    static public ClassSymbol IO;
    static public ClassSymbol INT;
    static public ClassSymbol STRING;
    static public ClassSymbol BOOL;
    public static List<String> nonInherit;
    public ClassSymbol inherited_scope = null;
    static {
        OBJECT = new ClassSymbol("Object", null);
        IO = new ClassSymbol("IO", OBJECT);
        INT = new ClassSymbol("Int", OBJECT);
        STRING = new ClassSymbol("String", OBJECT);
        BOOL = new ClassSymbol("Bool", OBJECT);

        nonInherit = List.of(OBJECT.getName(), INT.getName(), STRING.getName(), BOOL.getName(), "SELF_TYPE");
    }

    public Map<String, Symbol> var_symbols = new LinkedHashMap<>();
    public Map<String, Symbol> method_symbols = new LinkedHashMap<>();

    public ClassSymbol(String name, Scope parent) {
        super(name);
        this.parent = parent;
    }

    @Override
    public boolean add(Symbol sym) {
        if (sym instanceof FunctionSymbol) {
            if (method_symbols.containsKey(sym.getName()))
                return false;
            method_symbols.put(sym.getName(), (FunctionSymbol) sym);
            return true;
        } else if (sym instanceof IdSymbol) {
            if (var_symbols.containsKey(sym.name))
                return false;
            var_symbols.put(sym.getName(), (IdSymbol) sym);
            return true;
        }
        return false;
    }

    @Override
    public Symbol lookup(String str) {
        var sym = var_symbols.get(str);
        if (sym != null)
            return sym;
        sym = method_symbols.get(str);
        if (sym != null)
            return sym;
        if (parent != null)
            return (ClassSymbol)parent.lookup(str);
        return null;
    }

    @Override
    public Scope getParent() {
        return parent;
    }


}
