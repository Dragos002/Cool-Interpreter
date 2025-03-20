package cool.structures;

import org.antlr.v4.runtime.Token;

import java.util.LinkedList;
import java.util.List;

public class TypeSymbol {
    public String type_symbol;
    public Token token;
    public TypeSymbol(String type_symbol, Token token) {
        this.type_symbol = type_symbol;
        this.token = token;
    }
    public static String look_common_anscestor(ClassSymbol class1, ClassSymbol class2) {
        if (class1.name.equals(class2.name)) {
            return class1.type;
        }
        List<String> class1_ancestors = new LinkedList<>();
        List<String> class2_ancestors = new LinkedList<>();
        class1_ancestors.add(class1.name);
        class2_ancestors.add(class2.name);
        while (class1.inherited_scope != null) {
            class1_ancestors.add(class1.inherited_scope.name);
            class1 = class1.inherited_scope;
        }
        while (class2.inherited_scope != null) {
            class2_ancestors.add(class2.inherited_scope.name);
            class2 = class2.inherited_scope;
        }
//        System.out.println(class1_ancestors);
//        System.out.println(class2_ancestors);

        if (class1_ancestors.size() < class2_ancestors.size()) {
            while (class1_ancestors.size() < class2_ancestors.size())
                class1_ancestors.addAll(class1_ancestors.reversed());
        }
        else if (class2_ancestors.size() < class1_ancestors.size()) {
            while (class2_ancestors.size() < class1_ancestors.size())
                class2_ancestors.addAll(class2_ancestors.reversed());
        }
        int shorter_size = Math.min(class1_ancestors.size(), class2_ancestors.size());
        for (int i = 0; i < shorter_size; i++) {
            if (class1_ancestors.get(i).equals(class2_ancestors.get(i))) {
                return class1_ancestors.get(i);
            }
        }
        return "Object";
    }
}
