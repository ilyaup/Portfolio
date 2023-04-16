package expression;

public class L0 implements UltimateExpression{

    private final UltimateExpression expression;

    public L0(UltimateExpression expression) {
        this.expression = expression;
    }

    @Override
    public int evaluate(int x) {
        return calculate(expression.evaluate(x));
    }

    @Override
    public int evaluate(int x, int y, int z) {
        return calculate(expression.evaluate(x, y ,z));
    }

    private int calculate(int a) {
        if (a == 0) {
            return 32;
        }
        return 32 - Integer.toBinaryString(a).length();
    }

    @Override
    public String toString() {
        String res = expression.toString();
        return "l0(" + res + ")";
    }
}
