package language;

import java.util.List;

public class LangClass implements Callable {
    final String name;

    LangClass(String name) {
        this.name = name;
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
}
