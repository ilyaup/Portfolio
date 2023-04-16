package expression;

public class Subtract extends AbstractOperator{
    public Subtract(UltimateExpression a1, UltimateExpression a2) {
        super(a1, a2, "-");
    }

    public int evaluate(int x, int y, int z) {
        return a1.evaluate(x, y, z) - a2.evaluate(x, y, z);
    }

    public int evaluate(int x) {
        return a1.evaluate(x) - a2.evaluate(x);
    }
}
