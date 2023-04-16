package expression.exceptions;

import expression.AbstractOperator;
import expression.UltimateExpression;

public class CheckedDivide extends AbstractOperator {
    public CheckedDivide(UltimateExpression a1, UltimateExpression a2) {
        super(a1, a2, "/");
    }

    public int calculate (int b1, int b2) {
        if (b2 == 0) {
            throw new OverflowException("divizion by zero");
        }
        if (b1 == Integer.MIN_VALUE && b2 == -1) {
            throw new OverflowException("overflow");
        }
        return b1 / b2;
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
