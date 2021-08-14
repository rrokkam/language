package language;

import java.util.HashMap;
import java.util.Map;

public class Instance {
    private LangClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    Instance(LangClass klass) {
        this.klass = klass;
    }

    @Override
    public String toString() {
        return klass.name + " instance";
    }

    public Object get(Token name) {
        if (fields.containsKey(name.lexeme())) {
            return fields.get(name.lexeme());
        }
        Function method = klass.findMethod(name.lexeme());
        if (method != null) {
            return method;
        }
        throw new RuntimeError(name, String.format("Undefined property %s.", name.lexeme()));
    }

    public void set(Token name, Object value) {
        fields.put(name.lexeme(), value);
    }
}
