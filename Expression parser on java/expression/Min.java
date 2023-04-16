package expression;

public class Min extends AbstractOperator {
    public Min(UltimateExpression a1, UltimateExpression a2) {
        super(a1, a2, "min");
    }

    public int evaluate(int x, int y, int z) {
        return Math.min(a1.evaluate(x, y, z), a2.evaluate(x, y, z));
    }

    public int evaluate(int x) {
        return Math.min(a1.evaluate(x), a2.evaluate(x));
    }
}
