package expression;

public abstract class Equality {
    @Override
    public boolean equals(Object obj) {
        if (obj != null) {
            return obj.hashCode() == this.hashCode();
        }
        return false;
    }
}
