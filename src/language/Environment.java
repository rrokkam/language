package language;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> values = new HashMap<>();

    Object get(Token name) {
        if (values.containsKey(name.lexeme())) {
            return values.get(name.lexeme());
        }
        throw new RuntimeError(name, String.format("Undefined variable %s.", name.lexeme()));
    }

    void define(String name, Object value) {
        values.put(name, value);
    }
}
