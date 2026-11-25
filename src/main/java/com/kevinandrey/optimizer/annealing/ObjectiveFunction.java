package com.kevinandrey.optimizer.annealing;

/**
 * Objective function contract for continuous optimization problems.
 */
public interface ObjectiveFunction {
    /**
     * Unique descriptive name of the objective function.
     */
    String getName();

    /**
     * Number of decision variables (dimension).
     */
    int getDimension();

    /**
     * Lower bounds vector for each variable.
     */
    double[] getLowerBounds();

    /**
     * Upper bounds vector for each variable.
     */
    double[] getUpperBounds();

    /**
     * Whether the problem is maximization (true) or minimization (false).
     */
    boolean isMaximization();

    /**
     * Evaluates the objective function at the given solution point.
     *
     * @param point candidate solution vector
     * @return objective value (energy/fitness)
     */
    double evaluate(double[] point);
}
