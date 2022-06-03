package de.zenbit.aws;

import java.util.Stack;

public class Ackermann {

    /**
     * Stack-based Ackermann implemenation following 
     * "An inherently iterative computation of ackermann's function" 
     * by Jerrold W.Grossman and R.Suzanne Zeitman
     */
    static Long ackermann(long m, long n) {

        if (m < 0 || n < 0) {
            throw new IllegalArgumentException("Undefined for negative inputs.");
        }

        final Stack<Long> stack = new Stack<>();

        stack.push(m);
        stack.push(n);

        while (stack.size() > 1) {
            
            n = stack.pop();
            m = stack.pop();
            
            if (m == 0) {
                stack.push(n + 1);
            } else if (n == 0) {
                stack.push(m - 1);
                stack.push(1L);
            } else {
                stack.push(m - 1);
                stack.push(m);
                stack.push(n - 1);
            }
        }

        return stack.pop();

    }

}
