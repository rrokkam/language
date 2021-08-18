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
        Instance instance = new Instance(this);
        Function initializer = findMethod("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }

    @Override
    public int arity() {
        Function initializer = findMethod("init");
        if (initializer == null) {
            return 0;
        }
        return initializer.arity();
    }

    public Function findMethod(String name) {
        return methods.getOrDefault(name, null);
    }
}
