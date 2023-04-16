package expression;

import java.util.*;

public class Variable implements UltimateExpression{
    private final String varName;

    public Variable(String varName) {
        this.varName = varName;
    }

    public int evaluate(int x, int y, int z) {
        return switch (varName) {
            case "x" -> x;
            case "y" -> y;
            case "z" -> z;
            default -> x;
        };
    }

    public int evaluate(int x) {
        return x;
    }

    @Override
    public String toString() {
        return varName;
    }

    @Override
    public int hashCode() {
        return varName.hashCode();
    }
}
