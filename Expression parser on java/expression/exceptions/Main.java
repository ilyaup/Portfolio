package expression.exceptions;

import expression.*;

public class Main {

    public static void main(String args[]) {
        TripleParser exp = new ExpressionParser();
        try {
            int a1 = 2;
            int a2 = 3;
            System.out.println(exp.parse("t0x").toString());
            System.out.println(exp.parse("t0x").evaluate(0,0,0));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}