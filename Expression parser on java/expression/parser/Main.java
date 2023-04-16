package expression.parser;

import expression.*;

public class Main {
    public static void main(String args[]) {
        Parser exp = new ExpressionParser();
        String expression = args[0];
        String x = args[1];
        String y = args[2];
        String z = args[3];
        System.out.println(exp.parse(expression).evaluate(Integer.parseInt(x),Integer.parseInt(y),Integer.parseInt(z)));
    }
}