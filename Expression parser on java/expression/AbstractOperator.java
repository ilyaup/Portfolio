package expression;

import java.util.*;

public abstract class AbstractOperator implements UltimateExpression {
    protected UltimateExpression a1;
    protected UltimateExpression a2;
    protected String operatorSign;

    protected AbstractOperator(UltimateExpression a1, UltimateExpression a2, String operatorSign) {
        this.a1 = a1;
        this.a2 = a2;
        this.operatorSign = operatorSign;
    }

    @Override
    public String toString() {
        return "(" + a1.toString() + " " + operatorSign + " " + a2.toString() + ")";
    }

    @Override
    public int hashCode() {
        return ((operatorSign.hashCode() * 17 + a1.hashCode()) * 17 + a2.hashCode()) * 17;
    }

    public boolean equals(Object obj) {
        if (obj != null) {
            if (obj.getClass() == this.getClass()) {
                if (obj.hashCode() == this.hashCode()) {
                    AbstractOperator that = (AbstractOperator) obj;
                    return this.a1.equals(that.a1) && this.a2.equals(that.a2);
                }
                return false;
            }
        }
        return false;
    }
}
