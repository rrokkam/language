package language;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expr, Integer> locals = new HashMap<>();

    Interpreter() {
        globals.define("clock", new Callable() {
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double) System.currentTimeMillis() / 1000.0;
            }

            @Override
            public int arity() {
                return 0;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private Object execute(Stmt stmt) {
        return stmt.accept(this);
    }

    void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Main.runtimeError(error);
        }
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance, expr.name, value);
        } else {
            globals.assign(expr.name, value);
        }
        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type()) {
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double) left / (double) right;
            case ASTERISK:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }
                throw new RuntimeError(expr.operator,
                        "Operands must be two numbers or two strings.");
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            case EXCLAMATION_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
        }
        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);
        if (callee instanceof Callable func) {
            if (expr.arguments.size() != func.arity()) {
                throw new RuntimeError(expr.paren,
                        String.format("Expected %s arguments but got %s.",
                                func.arity(), expr.arguments.size()));
            }

            return func.call(this, expr.arguments.stream().map(this::evaluate).toList());
        }
        throw new RuntimeError(expr.paren, "Can only call functions and classes.");
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.object);
        if (object instanceof Instance instance) {
            return instance.get(expr.name);
        }
        throw new RuntimeError(expr.name, "Only instances have properties.");
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);
        if (expr.operator.type() == TokenType.OR) {
            if (truthiness(left)) return left;
        } else {
            if (!truthiness(left)) return left;
        }
        return evaluate(expr.right);
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        if (evaluate(expr.object) instanceof Instance instance) {
            Object value = evaluate(expr.value);
            instance.set(expr.name, value);
            return value;
        }
        throw new RuntimeError(expr.name, "Only instances have fields.");
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type()) {
            case EXCLAMATION:
                return !truthiness(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double) right;
        }

        // Unreachable
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return lookUpVariable(expr.name, expr);
    }

    private Object lookUpVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            // we trust the Resolver to have given us the right distance.
            // This is tight coupling between the Interpreter and the Resolver.
            return environment.getAt(distance, name.lexeme());
        } else {
            return globals.get(name);
        }
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (!(operand instanceof Double))
            throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (!(left instanceof Double) || !(right instanceof Double)) {
            throw new RuntimeError(operator, "Operands must be numbers.");
        }
    }

    private boolean isEqual(Object left, Object right) {
        return left == null && right == null || left != null && left.equals(right);
    }

    private boolean truthiness(Object object) {
        return object != null
                && (!(object instanceof Boolean) || (boolean) object);
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        environment.define(stmt.name.lexeme(), null);
        Map<String, Function> methods = new HashMap<>();
        for (Stmt.Function method : stmt.methods) {
            Function function = new Function(method, environment);
            methods.put(method.name.lexeme(), function);
        }

        LangClass klass = new LangClass(stmt.name.lexeme(), methods);
        environment.assign(stmt.name, klass);
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        throw new Break();
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        // A bit of a hack. Better: pass down the current environment in the visit methods.
        Environment previous = this.environment;
        this.environment = environment;
        try {
            statements.forEach(this::execute);
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        Function function = new Function(stmt, environment);
        environment.define(stmt.name.lexeme(), function);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (truthiness(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) {
            value = evaluate(stmt.value);
        }
        throw new Return(value);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme(), value);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (truthiness(evaluate(stmt.condition))) {
            try {
                execute(stmt.body);
            } catch (Break e) {
                break;
            }
        }
        return null;
    }

    private class Break extends RuntimeException {
    }
}

