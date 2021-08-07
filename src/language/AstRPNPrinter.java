package language;

class AstRPNPrinter implements Expr.Visitor<String> {
    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return String.format("%s %s %s", expr.left.accept(this), expr.right.accept(this),
                expr.operator.lexeme());
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        throw new UnsupportedOperationException("no parentheses in rpn");
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return String.format("%s %s", expr.right.accept(this), expr.operator.lexeme());
    }

    public static void main(String[] args) {
        Expr expression = new Expr.Binary(
                new Expr.Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Expr.Literal(123)),
                new Token(TokenType.ASTERISK, "*", null, 1),
                new Expr.Literal(45.67));

        System.out.println(new AstRPNPrinter().print(expression));
    }

}
