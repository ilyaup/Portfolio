package expression;

public class Max extends AbstractOperator {
    public Max(UltimateExpression a1, UltimateExpression a2) {
        super(a1, a2, "max");
    }

    public int evaluate(int x, int y, int z) {
        return Math.max(a1.evaluate(x, y, z), a2.evaluate(x, y, z));
    }

    public int evaluate(int x) {
        return Math.max(a1.evaluate(x), a2.evaluate(x));
    }
}
