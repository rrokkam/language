package language;

public class LangClass {
    final String name;

    LangClass(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
