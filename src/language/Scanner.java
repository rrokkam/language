package language;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static language.TokenType.*;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords = Map.ofEntries(
            Map.entry("and", AND),
            Map.entry("class", CLASS),
            Map.entry("else", ELSE),
            Map.entry("false", FALSE),
            Map.entry("for", FOR),
            Map.entry("function", FUNCTION),
            Map.entry("if", IF),
            Map.entry("nil", NIL),
            Map.entry("or", OR),
            Map.entry("print", PRINT),
            Map.entry("return", RETURN),
            Map.entry("super", SUPER),
            Map.entry("this", THIS),
            Map.entry("true", TRUE),
            Map.entry("var", VAR),
            Map.entry("while", WHILE)
    );

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            // At the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(' -> addToken(OPEN_PAREN);
            case ')' -> addToken(CLOSE_PAREN);
            case '{' -> addToken(OPEN_BRACE);
            case '}' -> addToken(CLOSE_BRACE);
            case ',' -> addToken(COMMA);
            case '.' -> addToken(PERIOD);
            case '-' -> addToken(MINUS);
            case '+' -> addToken(PLUS);
            case ';' -> addToken(SEMICOLON);
            case '*' -> addToken(ASTERISK);
            case '!' -> addToken(match('=') ? EXCLAMATION_EQUAL : EXCLAMATION);
            case '=' -> addToken(match('=') ? EQUAL_EQUAL : EQUAL);
            case '<' -> addToken(match('=') ? LESS_EQUAL : LESS);
            case '>' -> addToken(match('=') ? GREATER_EQUAL : GREATER);
            case '/' -> {
                if (match('/')) comment();
                else addToken(SLASH);
            }
            case ' ', '\r', '\t' -> {
            }
            case '\n' -> line++;
            case '"' -> string();
            default -> {
                if (isDigit(c)) {
                    number();
                } else if (isLetter(c)) {
                    identifier();
                } else {
                    Main.error(line, "Unexpected character");
                }
            }
        }
    }

    private void number() {
        while (isDigit(peek())) advance();

        // Decimal point followed by more digits
        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void identifier() {
        while (isAlphanumeric(peek())) advance();
        String text = source.substring(start, current);
        addToken(keywords.getOrDefault(text, IDENTIFIER));
    }

    private boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphanumeric(char c) {
        return isLetter(c) || isDigit(c);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
            }
            advance();
        }

        if (isAtEnd()) {
            Main.error(line, "Unterminated string");
            return;
        }

        // The closing "
        advance();

        // Trim the quotes
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private void comment() {
        while (peek() != '\n' && !isAtEnd()) {
            advance();
        }
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(current + 1);
    }

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) {
            return false;
        }
        current++;
        return true;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        tokens.add(new Token(type, source.substring(start, current), literal, line));
    }
}
