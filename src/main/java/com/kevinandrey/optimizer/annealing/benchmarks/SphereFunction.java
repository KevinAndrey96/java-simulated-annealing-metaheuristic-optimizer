package com.kevinandrey.optimizer.annealing.benchmarks;

import com.kevinandrey.optimizer.annealing.ObjectiveFunction;
import java.util.Arrays;

/**
 * Continuous quadratic benchmark:
 * Minimize f(x) = sum(x_i^2)
 * Bounds: [-5.12, 5.12]
 * Global minimum: x* = (0, 0, ..., 0) with f(x*) = 0.0.
 */
public class SphereFunction implements ObjectiveFunction {
    private final int dimension;
    private final double[] lowerBounds;
    private final double[] upperBounds;

    public SphereFunction(int dimension) {
        this.dimension = dimension;
        this.lowerBounds = new double[dimension];
        this.upperBounds = new double[dimension];
        Arrays.fill(lowerBounds, -5.12);
        Arrays.fill(upperBounds, 5.12);
    }

    public SphereFunction() {
        this(2);
    }

    @Override
    public String getName() {
        return "Sphere Function (" + dimension + "D)";
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
        for (double v : point) {
            sum += v * v;
        }
        return sum;
    }
}
