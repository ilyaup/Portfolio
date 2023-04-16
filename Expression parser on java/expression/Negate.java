package expression;

public class Negate implements UltimateExpression{

    private final UltimateExpression expression;
    private boolean actuallyNegative;

    public Negate(UltimateExpression expression) {
        this.expression = expression;
        //this.actuallyNegative = !(expression instanceof Negate) || !((Negate) expression).getActuallyNegative();
    }

    public UltimateExpression getPositive() {
        return expression;
    }

    /*public boolean getActuallyNegative() {
        return actuallyNegative;
    }*/

    @Override
    public int evaluate(int x) {
        return -expression.evaluate(x);
    }

    @Override
    public int evaluate(int x, int y, int z) {
        return -expression.evaluate(x, y ,z);
    }

    @Override
    public String toString() {
        String res = expression.toString();
        /*if (expression instanceof Const && ((Const) expression).value == 0) {
            return res;
        }
        if (expression instanceof Negate) {
            if (actuallyNegative) {
                return "-" + res;
            } else {
                return res.substring(1, res.length());
            }
        }
        if (actuallyNegative)  {
            return "-" + res;
        }
        return res;*/
        return "-(" + res + ")";
    }
}
