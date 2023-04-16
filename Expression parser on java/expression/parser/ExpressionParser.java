package expression.parser;

import expression.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExpressionParser implements Parser {
    private static final char END = '\0';
    Source source;
    private char ch = 0xffff;
    @Override
    public TripleExpression parse(String expression) {
        this.source = new Source(expression);
        take();
        UltimateExpression result;
        skipWhitespace();
        UltimateExpression a = parseElement();
        result = parseMax(a);
        skipWhitespace();
        return result;
    }

    /*
    a1 - первый аргумент, a2 - второй. Если одиночное выражение, то возвращается а1, если 2 аргумента, то (а1, а2).
    Если +, то return Add(a1, parseExpression). Если *, то return (
     */

    private UltimateExpression parseMax(UltimateExpression a1) {
        skipWhitespace();
        UltimateExpression a2;
        String operation;
        while (source.hasNext() && !test(')')) {
            skipWhitespace();
            if (test('m')) {
                operation = parseOperation();
                skipWhitespace();
                a2 = parseElement();
                skipWhitespace();
                if (contain("*/")) {
                    a2 = parseMul(a2);
                    skipWhitespace();
                } else if(contain("+-")) {
                    a2 = parseAdd(a2);
                    skipWhitespace();
                }
                if (operation.equals("max")) {
                    a1 = new Max(a1, a2);
                } else {
                    a1 = new Min(a1, a2);
                }
                skipWhitespace();
            } else {
                if (contain("+-")) {
                    a1 = parseAdd(a1);
                    skipWhitespace();
                } else if (contain("*/")) {
                    a1 = parseMul(a1);
                    skipWhitespace();
                }
            }
            skipWhitespace();
        }
        return a1;
    }

    private UltimateExpression parseAdd(UltimateExpression a1) {
        skipWhitespace();
        UltimateExpression a2;
        String operation;
        while (source.hasNext() && !test(')')) {
            skipWhitespace();
            if (contain("+-")) {
                operation = parseOperation();
                skipWhitespace();
                a2 = parseElement();
                skipWhitespace();
                if (contain("*/")) {
                    a2 = parseMul(a2);
                    skipWhitespace();
                }
                if (operation.equals("+")) {
                    a1 = new Add(a1, a2);
                } else {
                    a1 = new Subtract(a1, a2);
                }
                skipWhitespace();
            } else {
                if (contain("*/")) {
                    a1 = parseMul(a1);
                    skipWhitespace();
                } else {
                    return a1;
                }
            }
            skipWhitespace();
        }
        return a1;
    }

    private UltimateExpression parseMul(UltimateExpression a1) {
        skipWhitespace();
        UltimateExpression a2;
        String operation;
        while(source.hasNext() && !test(')')) {
            skipWhitespace();
            if (contain("*/")) {
                operation = parseOperation();
                skipWhitespace();
                a2 = parseElement();
                skipWhitespace();
                if (operation.equals("*")) {
                    a1 = new Multiply(a1, a2);
                } else {
                    a1 = new Divide(a1, a2);
                }
            } else {
                return a1;
            }
        }
        return a1;
    }

    private UltimateExpression parseElement() {
        UltimateExpression a = null;
        skipWhitespace();
        if (take('-')) {
            int negNum = 1;
            skipWhitespace();
            while (take('-')) {
                skipWhitespace();
                negNum++;
            }
            if (between('1', '9')) {
                a = parseInteger(negNum % 2 == 1);
            } else {
                skipWhitespace();
                take('(');
                a = parseElement();
                a = new Negate(parseMax(a));
                take(')');
            }
        } else if (between('0', '9')) {
            a = parseInteger(false);
        } else if (take('(')) {
            skipWhitespace();
            a = parseElement();
            a = parseMax(a);
            take(')');
        } else if (between('a', 'z')){
            a = parseVariable();
        } else {
            skipWhitespace();
        }
        if (a == null) {
            take();
            a = parseElement();
        }
        skipWhitespace();
        return a;
    }

    private String parseOperation() {
        String operation = "";
        if (take('+')) {
            operation = "+";
        } else if (take('-')) {
            operation =  "-";
        } else if (take('*')) {
            operation = "*";
        } else if (take('/')){
            operation = "/";
        } else {
            take('m');
            if (take('a')) {
                take('x');
                operation =  "max";
            } else if(take('i')) {
                take('n');
                operation = "min";
            }
        }
        return operation;
    }

    private UltimateExpression parseInteger(boolean negate) {
        UltimateExpression num;
        StringBuilder sb = new StringBuilder();
        if (negate) {
            sb.append('-');
        }
        while (between('0', '9')) {
            sb.append(take());
        }
        skipWhitespace();
        return new Const(Integer.parseInt(sb.toString()));
    }

    private UltimateExpression parseVariable() {
        UltimateExpression var;
        StringBuilder sb = new StringBuilder();
        while (between('a', 'z')) {
            sb.append(take());
        }
        skipWhitespace();
        return new Variable(sb.toString());
    }

    private char take() {
        final char result = ch;
        ch = source.hasNext() ? source.next() : END;
        return result;
    }


    private boolean test(final char expected) {
        return ch == expected;
    }

    private boolean take(final char expected) {
        if (test(expected)) {
            take();
            return true;
        }
        return false;
    }

    private boolean between(final char from, final char to) {
        return from <= ch && ch <= to;
    }

    private boolean contain(final String list) {
        return list.indexOf(ch) != -1;
    }

    private boolean eof() {
        return take(END);
    }

    private void skipWhitespace() {
        while (!between('0', '9') && !between('a', 'z') && !between('A', 'Z') && !contain("()-/+*") && source.hasNext()) {
            take();
        }
//        while (take(' ') || take('\r') || take('\n') || take('\t')) {
//            // skip
//        }
    }
}
