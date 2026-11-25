package com.kevinandrey.optimizer.annealing;

/**
 * Constraint interface for constrained optimization problems.
 */
public interface Constraint {
    /**
     * Name or identifier of the constraint.
     */
    String getName();

    /**
     * Evaluates whether the solution satisfies the constraint.
     *
     * @param point candidate solution vector
     * @return true if feasible, false otherwise
     */
    boolean isSatisfied(double[] point);

    /**
     * Evaluates constraint left-hand side or raw function value.
     *
     * @param point candidate solution vector
     * @return evaluated constraint value
     */
    double evaluate(double[] point);
}
