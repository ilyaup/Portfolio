package expression.exceptions;

import expression.*;
import expression.parser.Source;

import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExpressionParser implements TripleParser {
    private static final char END = '\0';
    Source source;
    String expression;
    private char ch = 0xffff;
    private boolean rightSpace = false;
    @Override
    public TripleExpression parse(String expression) throws Exception {
        //System.err.println(expression);
        this.expression = expression;
        parChecker(expression);
        FirstArgChecker(expression);
        FirstSymbolChecker(expression);
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
    Если +, то return CheckedAdd(a1, parseExpression). Если *, то return (
     */

    private UltimateExpression parseMax(UltimateExpression a1) {
        skipWhitespace();
        UltimateExpression a2;
        String operation;
        while (!eof() && !test(')')) {
            if (test('m')) {
                operation = parseOperation();
                skipWhitespace();
                a2 = parseElement();
                skipWhitespace();
                if (contain("*/")) {
                    a2 = parseMul(a2);
                    skipWhitespace();
                } else if(contain("+-")) {
                    a2 = parseCheckedAdd(a2);
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
                    a1 = parseCheckedAdd(a1);
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

    private UltimateExpression parseCheckedAdd(UltimateExpression a1) {
        skipWhitespace();
        UltimateExpression a2;
        String operation;
        while (!eof() && !test(')')) {
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
                    a1 = new CheckedAdd(a1, a2);
                } else {
                    a1 = new CheckedSubtract(a1, a2);
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
        skipWhitespace();
        return a1;
    }

    private UltimateExpression parseMul(UltimateExpression a1) {
        skipWhitespace();
        UltimateExpression a2;
        String operation;
        while(!eof() && !test(')')) {
            if (contain("*/")) {
                operation = parseOperation();
                skipWhitespace();
                a2 = parseElement();
                skipWhitespace();
                if (operation.equals("*")) {
                    a1 = new CheckedMultiply(a1, a2);
                } else {
                    a1 = new CheckedDivide(a1, a2);
                }
            } else {
                return a1;
            }
            skipWhitespace();
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
                if (negNum % 2 == 0) {
                    a = new CheckedNegate(parseInteger(true));
                } else if (negNum % 2 == 1) {
                    a = parseInteger(true);
                }
            } else {
                skipWhitespace();
                take('(');
                a = parseElement();
                a = new CheckedNegate(parseMax(a));
                take(')');
                rightSpace = true;
                skipWhitespace();
                if (!(contain("mtl-+/*)") || !source.hasNext())) {
                    throw new WrongFormatException("No operation");
                }
            }
        } else if (take('l')) {
            take('0');
            if (!(test(' ') || test('('))) {
                throw new WrongFormatException("Operation and argument are jointed " + expression);
            }
            skipWhitespace();
            take('(');
            a = parseElement();
            a = new L0(parseMax(a));
            take(')');
            rightSpace = true;
            skipWhitespace();
            if (!(contain("mtl-+/*)") || !source.hasNext())) {
                throw new WrongFormatException("No operation");
            }
        } else if (take('t')) {
            take('0');
            if (!(test(' ') || test('('))) {
                throw new WrongFormatException("Operation and argument are jointed" + expression);
            }
            skipWhitespace();
            take('(');
            a = parseElement();
            a = new T0(parseMax(a));
            take(')');
            rightSpace = true;
            skipWhitespace();
            if (!(contain("mtl-+/*)") || !source.hasNext())) {
                throw new WrongFormatException("No operation");
            }
        } else if (between('0', '9')) {
            a = parseInteger(false);
        } else if (take('(')) {
            skipWhitespace();
            a = parseElement();
            a = parseMax(a);
            take(')');
            rightSpace = true;
            skipWhitespace();
            if (!(contain("mtl-+/*)") || !source.hasNext())) {
                throw new WrongFormatException("No operation " + expression);
            }
        } else if (contain("xyz")){
            a = parseVariable();
        } else {
            throw new WrongFormatException("Illegal symbol");
        }
        if (a == null) {
            take();
            a = parseElement();
        }
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
            if (!rightSpace) {
                throw new WrongFormatException("No space " + expression);
            }
            skipWhitespace();
            take('m');
            if (take('a')) {
                take('x');
                operation =  "max";
            } else if(take('i')) {
                take('n');
                operation = "min";
            }
        }
        rightSpace = false;
        return operation;
    }

    private UltimateExpression parseInteger(boolean negate) {
        int pred = 0;
        int cur = 0;
        if (negate) {
            while (between('0', '9')) {
                cur = 10 * pred - Character.getNumericValue(take());
                if (cur > 0) {
                    throw new WrongFormatException("Constant overflow");
                }
                pred = cur;
            }
        } else {
            while (between('0', '9')) {
                cur = 10 * pred + Character.getNumericValue(take());
                if (cur < 0) {
                    throw new WrongFormatException("Constant overflow " + expression);
                }
                pred = cur;
            }
        }
        if (take(' ')) {
            rightSpace = true;
        }
        skipWhitespace();
        if (!(contain("mtl-+/*)") || !source.hasNext())) {
            throw new WrongFormatException("Spaces in numbers");
        }
        return new Const(cur);
    }

    private UltimateExpression parseVariable() {
        UltimateExpression var;
        StringBuilder sb = new StringBuilder();
        while (between('a', 'z')) {
            sb.append(take());
        }
        if (take(' ')) {
            rightSpace = true;
        }
        skipWhitespace();
        if (!(contain("mtl-+/*)") || !source.hasNext())) {
            throw new WrongFormatException("Spaces in numbers");
        }
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
        if (expected == ch) {
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
    private void parChecker(String expr) {
        int numPar = 0;
        for (int i = 0; i < expr.length(); i++) {
            if (expr.charAt(i) == '(') {
                numPar++;
            } else if (expr.charAt(i) == ')') {
                numPar--;
            }
        }
        if (numPar < 0) {
            throw new WrongFormatException("No opening parenthesis");
        } else if (numPar > 0) {
            throw new WrongFormatException("No closing parenthesis");
        }
    }

    private void FirstSymbolChecker (String expr) {
        for (int i = 0; i < expr.length(); i++) {
            if (! (" ()-/+*".indexOf(expr.charAt(i)) != -1 || '0' <= expr.charAt(i) && expr.charAt(i) <= '9'
                    || 'a' <= expr.charAt(i) && expr.charAt(i) <= 'z'
                    || 'A' <= expr.charAt(i) && expr.charAt(i) <= 'Z' || Character.isWhitespace(expr.charAt(i)))) {
                throw new WrongFormatException("Illegal symbol " + expr);
            }
        }
    }

    private void FirstArgChecker(String expr) {
        boolean inNegate = false;
        for (int i = 0; i < expr.length(); i++) {
            if ("+*/".indexOf(expr.charAt(i)) != -1){
                throw new WrongFormatException(" No argument " + expr);
            }
            if ('-' == expr.charAt(i)) {
                inNegate = true;
            }
            if ('0' <= expr.charAt(i) && expr.charAt(i) <= '9' || 'a' <= expr.charAt(i) && expr.charAt(i) <= 'z'
            || 'A' <= expr.charAt(i) && expr.charAt(i) <= 'Z') {
                return;
            }
        }
        if (inNegate) {
            throw new WrongFormatException("No argument " + expr);
        }
    }
}
