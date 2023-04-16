package expression.exceptions;

import expression.AbstractOperator;
import expression.UltimateExpression;

public class CheckedMultiply extends AbstractOperator {
    public CheckedMultiply(UltimateExpression a1, UltimateExpression a2) {
        super(a1, a2, "*");
    }

    private int calculate (int b1, int b2) {
        int res = b1 * b2;
        if (b1 == 0 || b2 == 0) {
            return res;
        }
        if (res / b1 != b2 || res / b2 != b1) {
            throw new OverflowException("overflow");
        }
        return res;
    }

    public int evaluate(int x, int y, int z) {
        int res = 0;
        int b1 = a1.evaluate(x, y, z);
        int b2 = a2.evaluate(x, y, z);
        return calculate(b1, b2);
    }

    public int evaluate(int x) {
        return calculate(a1.evaluate(x), a2.evaluate(x));
    }
}
