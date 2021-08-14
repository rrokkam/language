package language;

import java.util.List;
import java.util.Map;

public class LangClass implements Callable {
    final String name;
    final Map<String, Function> methods;

    LangClass(String name, Map<String, Function> methods) {
        this.name = name;
        this.methods = methods;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        return new Instance(this);
    }

    @Override
    public int arity() {
        return 0;
    }

    public Function findMethod(String name) {
        return methods.getOrDefault(name, null);
    }
}
