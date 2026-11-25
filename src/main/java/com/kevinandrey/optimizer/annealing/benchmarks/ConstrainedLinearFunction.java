package com.kevinandrey.optimizer.annealing.benchmarks;

import com.kevinandrey.optimizer.annealing.Constraint;
import com.kevinandrey.optimizer.annealing.ObjectiveFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Original linear programming problem:
 * Maximize f(x1, x2) = 2*x1 + x2
 * Subject to:
 *   r1: 2*x1 + 5*x2 <= 60
 *   r2: 3*x1 + x2 <= 44
 *   r3: x1 + x2 <= 18
 *   x1 >= 0, x2 >= 0
 *
 * Theoretical global maximum: x* = (13.0, 5.0) with f(x*) = 31.0.
 */
public class ConstrainedLinearFunction implements ObjectiveFunction {

    @Override
    public String getName() {
        return "Linear Programming Problem (2*x1 + x2 with 3 constraints)";
    }

    @Override
    public int getDimension() {
        return 2;
    }

    @Override
    public double[] getLowerBounds() {
        return new double[]{0.0, 0.0};
    }

    @Override
    public double[] getUpperBounds() {
        return new double[]{15.0, 12.0};
    }

    @Override
    public boolean isMaximization() {
        return true;
    }

    @Override
    public double evaluate(double[] point) {
        return (2.0 * point[0]) + point[1];
    }

    public static List<Constraint> createConstraints() {
        List<Constraint> constraints = new ArrayList<>();

        // Constraint 1: 2*x1 + 5*x2 <= 60
        constraints.add(new Constraint() {
            @Override
            public String getName() { return "r1: 2*x1 + 5*x2 <= 60"; }
            @Override
            public boolean isSatisfied(double[] point) { return evaluate(point) <= 60.0 + 1e-6; }
            @Override
            public double evaluate(double[] point) { return (2.0 * point[0]) + (5.0 * point[1]); }
        });

        // Constraint 2: 3*x1 + x2 <= 44
        constraints.add(new Constraint() {
            @Override
            public String getName() { return "r2: 3*x1 + x2 <= 44"; }
            @Override
            public boolean isSatisfied(double[] point) { return evaluate(point) <= 44.0 + 1e-6; }
            @Override
            public double evaluate(double[] point) { return (3.0 * point[0]) + point[1]; }
        });

        // Constraint 3: x1 + x2 <= 18
        constraints.add(new Constraint() {
            @Override
            public String getName() { return "r3: x1 + x2 <= 18"; }
            @Override
            public boolean isSatisfied(double[] point) { return evaluate(point) <= 18.0 + 1e-6; }
            @Override
            public double evaluate(double[] point) { return point[0] + point[1]; }
        });

        return constraints;
    }

    public static double getTheoreticalOptimum() {
        return 31.0;
    }

    public static double[] getOptimalPoint() {
        return new double[]{13.0, 5.0};
    }
}
