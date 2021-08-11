package language;

class Return extends RuntimeException {
    final Object value;

    Return(Object value) {
        // disable stack traces + similar
        super(null, null, false, false);
        this.value = value;
    }
}