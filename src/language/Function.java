package language;

import java.util.List;

public class Function implements Callable {
    private final Stmt.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;

    public Function(Stmt.Function declaration, Environment closure, boolean isInitializer) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i=0; i < arity(); i++) {
            environment.define(declaration.params.get(i).lexeme(), arguments.get(i));
        }
        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return e) {
            if (isInitializer) {
                return closure.getAt(0, "this");
            }
            return e.value;
        }

        if (isInitializer) {
            return closure.getAt(0, "this");
        }
        return null;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme() + ">";
    }

    public Function bind(Instance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new Function(declaration, environment, isInitializer);
    }
}
