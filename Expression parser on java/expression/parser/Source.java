package expression.parser;

public class Source {
    private static final char END = '\0';
    String source;
    int pos = 0;

    public Source(String source) {
        this.source = source;
    }

    public boolean hasNext() {
        return pos < source.length() && source.charAt(pos) != END;
    }

    public char next() {
        if (hasNext()) {
            return source.charAt(pos++);
        } else {
            return END;
        }
    }
}
