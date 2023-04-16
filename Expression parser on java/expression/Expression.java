package expression;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * One-argument arithmetic expression over integers.
 *
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
@FunctionalInterface
@SuppressWarnings("ClassReferencesSubclass")
public interface Expression extends ToMiniString {
    int evaluate(int x);

    private static Const c(final int c) {
        return new Const(c);
    }
}
