package language;

public class Instance {
    private LangClass klass;

    Instance(LangClass klass) {
        this.klass = klass;
    }

    @Override
    public String toString() {
        return klass.name + " instance";
    }
}
