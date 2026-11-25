package com.kevinandrey.optimizer.annealing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the complete outcome, convergence history and statistics of an optimization run.
 */
public class OptimizationResult {
    private final double[] bestSolution;
    private final double bestEnergy;
    private final double[] initialSolution;
    private final double initialEnergy;
    private final int totalIterations;
    private final int totalEvaluations;
    private final int acceptedTransitions;
    private final long executionTimeMillis;
    private final boolean converged;
    private final String terminationReason;
    private final List<IterationRecord> history;

    public OptimizationResult(double[] bestSolution, double bestEnergy,
                              double[] initialSolution, double initialEnergy,
                              int totalIterations, int totalEvaluations, int acceptedTransitions,
                              long executionTimeMillis, boolean converged, String terminationReason,
                              List<IterationRecord> history) {
        this.bestSolution = bestSolution != null ? Arrays.copyOf(bestSolution, bestSolution.length) : new double[0];
        this.bestEnergy = bestEnergy;
        this.initialSolution = initialSolution != null ? Arrays.copyOf(initialSolution, initialSolution.length) : new double[0];
        this.initialEnergy = initialEnergy;
        this.totalIterations = totalIterations;
        this.totalEvaluations = totalEvaluations;
        this.acceptedTransitions = acceptedTransitions;
        this.executionTimeMillis = executionTimeMillis;
        this.converged = converged;
        this.terminationReason = terminationReason;
        this.history = history != null ? Collections.unmodifiableList(new ArrayList<>(history)) : Collections.emptyList();
    }

    public double[] getBestSolution() { return Arrays.copyOf(bestSolution, bestSolution.length); }
    public double getBestEnergy() { return bestEnergy; }
    public double[] getInitialSolution() { return Arrays.copyOf(initialSolution, initialSolution.length); }
    public double getInitialEnergy() { return initialEnergy; }
    public int getTotalIterations() { return totalIterations; }
    public int getTotalEvaluations() { return totalEvaluations; }
    public int getAcceptedTransitions() { return acceptedTransitions; }
    public long getExecutionTimeMillis() { return executionTimeMillis; }
    public boolean isConverged() { return converged; }
    public String getTerminationReason() { return terminationReason; }
    public List<IterationRecord> getHistory() { return history; }

    public double getAcceptanceRate() {
        return totalEvaluations > 0 ? ((double) acceptedTransitions / totalEvaluations) * 100.0 : 0.0;
    }

    @Override
    public String toString() {
        return String.format("OptimizationResult{bestEnergy=%.6f, bestSolution=%s, iterations=%d, evals=%d, accepted=%d (%.2f%%), time=%dms, reason='%s'}",
                bestEnergy, Arrays.toString(bestSolution), totalIterations, totalEvaluations,
                acceptedTransitions, getAcceptanceRate(), executionTimeMillis, terminationReason);
    }

    /**
     * Snapshot record at a given temperature cycle.
     */
    public static class IterationRecord {
        private final int iteration;
        private final double temperature;
        private final double currentEnergy;
        private final double bestEnergy;
        private final double[] currentPoint;
        private final double[] bestPoint;
        private final int acceptedCount;

        public IterationRecord(int iteration, double temperature, double currentEnergy, double bestEnergy,
                               double[] currentPoint, double[] bestPoint, int acceptedCount) {
            this.iteration = iteration;
            this.temperature = temperature;
            this.currentEnergy = currentEnergy;
            this.bestEnergy = bestEnergy;
            this.currentPoint = currentPoint != null ? Arrays.copyOf(currentPoint, currentPoint.length) : new double[0];
            this.bestPoint = bestPoint != null ? Arrays.copyOf(bestPoint, bestPoint.length) : new double[0];
            this.acceptedCount = acceptedCount;
        }

        public int getIteration() { return iteration; }
        public double getTemperature() { return temperature; }
        public double getCurrentEnergy() { return currentEnergy; }
        public double getBestEnergy() { return bestEnergy; }
        public double[] getCurrentPoint() { return Arrays.copyOf(currentPoint, currentPoint.length); }
        public double[] getBestPoint() { return Arrays.copyOf(bestPoint, bestPoint.length); }
        public int getAcceptedCount() { return acceptedCount; }
    }
}
