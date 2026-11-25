package com.kevinandrey.optimizer.annealing;

import com.kevinandrey.optimizer.annealing.benchmarks.ConstrainedLinearFunction;
import com.kevinandrey.optimizer.annealing.benchmarks.SphereFunction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimulatedAnnealingEngineTest {

    @Test
    void testConstrainedLinearOptimizationFindsTheoreticalOptimum() {
        ConstrainedLinearFunction function = new ConstrainedLinearFunction();
        List<Constraint> constraints = ConstrainedLinearFunction.createConstraints();

        AnnealingParameters params = AnnealingParameters.builder()
                .initialTemperature(1000.0)
                .coolingSchedule(new CoolingSchedule.Geometric(0.96))
                .iterationsPerTemperature(80)
                .maxIterations(1500)
                .randomSeed(42L)
                .build();

        SimulatedAnnealingEngine engine = new SimulatedAnnealingEngine(function, constraints, params);
        OptimizationResult result = engine.optimize();

        assertNotNull(result);
        assertTrue(result.getBestEnergy() > 28.0, "Expected linear function to approach optimum (31.0), got: " + result.getBestEnergy());
        assertEquals(31.0, ConstrainedLinearFunction.getTheoreticalOptimum(), 1e-4);

        // Verify all constraints are satisfied at the best solution
        for (Constraint c : constraints) {
            assertTrue(c.isSatisfied(result.getBestSolution()), "Constraint violated: " + c.getName());
        }
    }

    @Test
    void testSphereFunctionMinimizationApproachesZero() {
        SphereFunction sphere = new SphereFunction(3);
        AnnealingParameters params = AnnealingParameters.builder()
                .initialTemperature(500.0)
                .coolingSchedule(new CoolingSchedule.Geometric(0.95))
                .iterationsPerTemperature(50)
                .maxIterations(1200)
                .randomSeed(12345L)
                .build();

        SimulatedAnnealingEngine engine = new SimulatedAnnealingEngine(sphere, params);
        OptimizationResult result = engine.optimize();

        assertNotNull(result);
        assertTrue(result.getBestEnergy() < 0.1, "Expected Sphere function to minimize near 0.0, got: " + result.getBestEnergy());
    }

    @Test
    void testDeterminismWithFixedRandomSeed() {
        SphereFunction sphere = new SphereFunction(2);
        AnnealingParameters params = AnnealingParameters.builder()
                .initialTemperature(100.0)
                .coolingSchedule(new CoolingSchedule.Geometric(0.90))
                .iterationsPerTemperature(20)
                .maxIterations(200)
                .randomSeed(9999L)
                .build();

        SimulatedAnnealingEngine engine1 = new SimulatedAnnealingEngine(sphere, params);
        SimulatedAnnealingEngine engine2 = new SimulatedAnnealingEngine(sphere, params);

        OptimizationResult res1 = engine1.optimize(new double[]{2.0, 2.0});
        OptimizationResult res2 = engine2.optimize(new double[]{2.0, 2.0});

        assertEquals(res1.getBestEnergy(), res2.getBestEnergy(), 1e-6);
        assertArrayEquals(res1.getBestSolution(), res2.getBestSolution(), 1e-6);
    }
}
