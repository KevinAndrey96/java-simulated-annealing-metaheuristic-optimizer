package com.kevinandrey.optimizer.annealing.benchmarks;

import com.kevinandrey.optimizer.annealing.ObjectiveFunction;
import java.util.Arrays;

/**
 * Ackley multimodal benchmark:
 * Minimize f(x) = -20*exp(-0.2*sqrt((1/d)*sum(x_i^2))) - exp((1/d)*sum(cos(2*pi*x_i))) + 20 + e
 * Bounds: [-32.768, 32.768]
 * Global minimum: x* = 0 with f(x*) = 0.0.
 */
public class AckleyFunction implements ObjectiveFunction {
    private final int dimension;
    private final double[] lowerBounds;
    private final double[] upperBounds;

    public AckleyFunction(int dimension) {
        this.dimension = dimension;
        this.lowerBounds = new double[dimension];
        this.upperBounds = new double[dimension];
        Arrays.fill(lowerBounds, -32.768);
        Arrays.fill(upperBounds, 32.768);
    }

    public AckleyFunction() {
        this(2);
    }

    @Override
    public String getName() {
        return "Ackley Function (" + dimension + "D)";
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
        double sum1 = 0.0;
        double sum2 = 0.0;
        for (double v : point) {
            sum1 += v * v;
            sum2 += Math.cos(2.0 * Math.PI * v);
        }
        double term1 = -20.0 * Math.exp(-0.2 * Math.sqrt(sum1 / dimension));
        double term2 = -Math.exp(sum2 / dimension);
        return term1 + term2 + 20.0 + Math.E;
    }
}
