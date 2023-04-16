package expression;

public class Main {
    public static void main(String[] args) {
        final Variable vx = new Variable("x");
        final Variable vy = new Variable("y");
        final Variable vz = new Variable("z");
        UltimateExpression exp1 = new Multiply(new Const(2), new Variable("x"));
        UltimateExpression exp2 = new Multiply(new Const(2), new Variable("x"));
        System.out.println(exp1.equals(exp2));
    }
}
