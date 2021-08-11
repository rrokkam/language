package language;

import java.util.List;

public interface Function {
    Object call(Interpreter interpreter, List<Object> arguments);
    int arity();
}
