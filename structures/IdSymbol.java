package cool.structures;

public class IdSymbol extends Symbol {
    public ClassSymbol type_class;
    public String type;
    public Boolean has_errors_in_definitionpass = false;

    public Scope parentScope = null;

    public IdSymbol(String name) {
        super(name);
    }
    public IdSymbol(String name, String type) {
        super(name);
        this.type = type;
    }
    public IdSymbol(String name, ClassSymbol type_class) {
        super(name);
        this.type_class = type_class;
    }
}
