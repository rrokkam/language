package language;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        switch (args.length) {
            case 0 -> runPrompt();
            case 1 -> runFile(args[0]);
            default -> {
                System.out.println("Usage: main [script]");
                System.exit(64);
            }
        }
    }

    private static void runFile(String path) throws IOException {
        run(Files.readString(Paths.get(path)));
        if (hadError) {
            System.exit(65);
        }
    }

    private static void runPrompt() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    private static void run(String source) {
        List<Token> tokens = new Scanner(source).scanTokens();
        Expr expression = new Parser(tokens).parse();
        if (hadError) return;
        System.out.println(new AstPrinter().print(expression));
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    static void error(Token token, String message) {
        if (token.type() == TokenType.EOF) {
            report(token.line(), " at end", message);
        } else {
            report(token.line(), String.format(" at '%s'", token.lexeme()), message);
        }
    }

    private static void report(int line, String where, String message) {
        System.err.printf("[line %s] Error %s: %s%n", line, where, message);
        hadError = true;
    }
}
