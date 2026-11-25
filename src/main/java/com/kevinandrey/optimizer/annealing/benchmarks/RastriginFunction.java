package com.kevinandrey.optimizer.annealing.benchmarks;

import com.kevinandrey.optimizer.annealing.ObjectiveFunction;
import java.util.Arrays;

/**
 * Highly multimodal benchmark with thousands of local extrema:
 * Minimize f(x) = 10*d + sum(x_i^2 - 10*cos(2*pi*x_i))
 * Bounds: [-5.12, 5.12]
 * Global minimum: x* = 0 with f(x*) = 0.0.
 */
public class RastriginFunction implements ObjectiveFunction {
    private final int dimension;
    private final double[] lowerBounds;
    private final double[] upperBounds;

    public RastriginFunction(int dimension) {
        this.dimension = dimension;
        this.lowerBounds = new double[dimension];
        this.upperBounds = new double[dimension];
        Arrays.fill(lowerBounds, -5.12);
        Arrays.fill(upperBounds, 5.12);
    }

    public RastriginFunction() {
        this(2);
    }

    @Override
    public String getName() {
        return "Rastrigin Function (" + dimension + "D)";
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
        double sum = 10.0 * dimension;
        for (double v : point) {
            sum += (v * v) - (10.0 * Math.cos(2.0 * Math.PI * v));
        }
        return sum;
    }
}
