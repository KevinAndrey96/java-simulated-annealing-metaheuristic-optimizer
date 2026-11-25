package com.kevinandrey.optimizer.annealing;

import com.kevinandrey.optimizer.annealing.benchmarks.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BenchmarksTest {

    @Test
    void testSphereGlobalMinimumIsZeroAtOrigin() {
        SphereFunction sphere = new SphereFunction(3);
        double valueAtZero = sphere.evaluate(new double[]{0.0, 0.0, 0.0});
        assertEquals(0.0, valueAtZero, 1e-9);

        double valueAtPoint = sphere.evaluate(new double[]{1.0, 2.0, 3.0});
        assertEquals(14.0, valueAtPoint, 1e-9);
    }

    @Test
    void testRastriginGlobalMinimumIsZeroAtOrigin() {
        RastriginFunction rastrigin = new RastriginFunction(2);
        double valueAtZero = rastrigin.evaluate(new double[]{0.0, 0.0});
        assertEquals(0.0, valueAtZero, 1e-9);
    }

    @Test
    void testRosenbrockGlobalMinimumIsZeroAtOnes() {
        RosenbrockFunction rosenbrock = new RosenbrockFunction(2);
        double valueAtOnes = rosenbrock.evaluate(new double[]{1.0, 1.0});
        assertEquals(0.0, valueAtOnes, 1e-9);
    }

    @Test
    void testAckleyGlobalMinimumIsZeroAtOrigin() {
        AckleyFunction ackley = new AckleyFunction(2);
        double valueAtZero = ackley.evaluate(new double[]{0.0, 0.0});
        assertEquals(0.0, valueAtZero, 1e-9);
    }

    @Test
    void testConstrainedLinearEvaluatesCorrectly() {
        ConstrainedLinearFunction func = new ConstrainedLinearFunction();
        double val = func.evaluate(new double[]{13.0, 5.0});
        assertEquals(31.0, val, 1e-9);
    }
}
