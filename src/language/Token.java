package language;

record Token(TokenType type, String lexeme, Object literal, int line) {
    @Override
    public String toString() {
        return String.format("%s %s %s", type, lexeme, literal);
    }
}
