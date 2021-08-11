package language;

import java.util.List;

public class Function implements Callable {
    private final Stmt.Function declaration;

    public Function(Stmt.Function declaration) {
        this.declaration = declaration;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(interpreter.globals);
        for (int i=0; i < arity(); i++) {
            environment.define(declaration.params.get(i).lexeme(), arguments.get(i));
        }
        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return e) {
            return e.value;
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
}