package expression.exceptions;

import expression.UltimateExpression;

public class CheckedNegate implements UltimateExpression {

    private final UltimateExpression expression;

    public CheckedNegate(UltimateExpression expression) {
        this.expression = expression;
    }

    private int calculate(int b1) {
        if (b1 == Integer.MIN_VALUE) {
            throw new OverflowException("overflow");
        }
        return -b1;
    }

    @Override
    public int evaluate(int x) {
        return calculate(expression.evaluate(x));
    }

    @Override
    public int evaluate(int x, int y, int z) {
        return calculate(expression.evaluate(x, y ,z));
    }

    @Override
    public String toString() {
        String res = expression.toString();
        return "-(" + res + ")";
    }
}