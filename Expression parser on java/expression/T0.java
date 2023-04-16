package expression;

public class T0 implements UltimateExpression{

    private final UltimateExpression expression;

    public T0(UltimateExpression expression) {
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
        String bin = Integer.toBinaryString(a);
        int res = 0;
        for (int i = bin.length() - 1; i >= 0; i--) {
            if (bin.charAt(i) == '1') {
                break;
            }
            res++;
        }
        return res;
    }

    @Override
    public String toString() {
        String res = expression.toString();
        return "t0(" + res + ")";
    }
}
