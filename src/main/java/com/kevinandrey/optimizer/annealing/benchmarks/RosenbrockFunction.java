package com.kevinandrey.optimizer.annealing.benchmarks;

import com.kevinandrey.optimizer.annealing.ObjectiveFunction;
import java.util.Arrays;

/**
 * Rosenbrock Valley / Banana benchmark:
 * Minimize f(x) = sum_{i=1}^{d-1} [ 100*(x_{i+1} - x_i^2)^2 + (1 - x_i)^2 ]
 * Bounds: [-5.0, 5.0]
 * Global minimum: x* = (1, 1, ..., 1) with f(x*) = 0.0.
 */
public class RosenbrockFunction implements ObjectiveFunction {
    private final int dimension;
    private final double[] lowerBounds;
    private final double[] upperBounds;

    public RosenbrockFunction(int dimension) {
        if (dimension < 2) {
            throw new IllegalArgumentException("Rosenbrock requires at least 2 dimensions.");
        }
        this.dimension = dimension;
        this.lowerBounds = new double[dimension];
        this.upperBounds = new double[dimension];
        Arrays.fill(lowerBounds, -5.0);
        Arrays.fill(upperBounds, 5.0);
    }

    public RosenbrockFunction() {
        this(2);
    }

    @Override
    public String getName() {
        return "Rosenbrock Valley Function (" + dimension + "D)";
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    @Override
    public double[] getLowerBounds() {
        return Arrays.copyOf(lowerBounds, dimension);
    }

    @Override
    public double[] getUpperBounds() {
        return Arrays.copyOf(upperBounds, dimension);
    }

    @Override
    public boolean isMaximization() {
        return false;
    }

    @Override
    public double evaluate(double[] point) {
        double sum = 0.0;
        for (int i = 0; i < dimension - 1; i++) {
            double term1 = point[i + 1] - (point[i] * point[i]);
            double term2 = 1.0 - point[i];
            sum += (100.0 * term1 * term1) + (term2 * term2);
        }
        return sum;
    }
}
