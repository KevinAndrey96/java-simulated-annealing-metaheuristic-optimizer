package com.kevinandrey.optimizer.annealing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Thread-safe, deterministic Simulated Annealing Metaheuristic Engine.
 */
public class SimulatedAnnealingEngine {

    /**
     * Listener interface to stream real-time iteration progress.
     */
    @FunctionalInterface
    public interface OptimizationListener {
        void onIteration(OptimizationResult.IterationRecord record);
    }

    private final ObjectiveFunction objectiveFunction;
    private final List<Constraint> constraints;
    private final AnnealingParameters parameters;
    private final List<OptimizationListener> listeners = new ArrayList<>();

    public SimulatedAnnealingEngine(ObjectiveFunction objectiveFunction,
                                    List<Constraint> constraints,
                                    AnnealingParameters parameters) {
        if (objectiveFunction == null) {
            throw new IllegalArgumentException("ObjectiveFunction cannot be null.");
        }
        this.objectiveFunction = objectiveFunction;
        this.constraints = constraints != null ? new ArrayList<>(constraints) : Collections.emptyList();
        this.parameters = parameters != null ? parameters : AnnealingParameters.defaultParameters();
    }

    public SimulatedAnnealingEngine(ObjectiveFunction objectiveFunction, AnnealingParameters parameters) {
        this(objectiveFunction, Collections.emptyList(), parameters);
    }

    public SimulatedAnnealingEngine(ObjectiveFunction objectiveFunction) {
        this(objectiveFunction, Collections.emptyList(), AnnealingParameters.defaultParameters());
    }

    public void addListener(OptimizationListener listener) {
        if (listener != null) {
            this.listeners.add(listener);
        }
    }

    /**
     * Executes the simulated annealing optimization algorithm from a default random or given starting point.
     *
     * @param initialPoint starting point coordinates (can be null for random initialization)
     * @return complete optimization result
     */
    public OptimizationResult optimize(double[] initialPoint) {
        long startTime = System.currentTimeMillis();
        Random random = parameters.getRandomSeed() != null
                ? new Random(parameters.getRandomSeed())
                : new Random();

        int dim = objectiveFunction.getDimension();
        double[] lowerBounds = objectiveFunction.getLowerBounds();
        double[] upperBounds = objectiveFunction.getUpperBounds();
        boolean isMax = objectiveFunction.isMaximization();

        // Initialize current solution
        double[] current = new double[dim];
        if (initialPoint != null && initialPoint.length == dim) {
            System.arraycopy(initialPoint, 0, current, 0, dim);
        } else {
            // Find a feasible random initial point
            current = generateInitialFeasiblePoint(random, lowerBounds, upperBounds);
        }

        double currentEnergy = objectiveFunction.evaluate(current);
        double[] best = Arrays.copyOf(current, dim);
        double bestEnergy = currentEnergy;

        double[] initialSolutionCopy = Arrays.copyOf(current, dim);
        double initialEnergy = currentEnergy;

        double temperature = parameters.getInitialTemperature();
        double initialTemp = temperature;
        CoolingSchedule schedule = parameters.getCoolingSchedule();
        NeighborGenerator generator = parameters.getNeighborGenerator();

        List<OptimizationResult.IterationRecord> history = new ArrayList<>();
        int totalEvaluations = 0;
        int acceptedTransitions = 0;
        int stagnantCycles = 0;
        String terminationReason = "Max iterations reached";
        boolean converged = false;

        int cycle = 0;
        while (cycle < parameters.getMaxIterations()) {
            cycle++;
            int acceptedInCycle = 0;

            for (int step = 0; step < parameters.getIterationsPerTemperature(); step++) {
                totalEvaluations++;

                // Generate neighbor candidate
                double[] candidate = generator.generateNeighbor(current, lowerBounds, upperBounds, temperature, random);

                // Verify feasibility against constraints
                if (!isFeasible(candidate)) {
                    continue; // Infeasible solution rejected
                }

                double candidateEnergy = objectiveFunction.evaluate(candidate);

                // Metropolis acceptance criterion
                boolean accept = AcceptanceCriterion.shouldAccept(currentEnergy, candidateEnergy,
                        temperature, isMax, random);

                if (accept) {
                    current = candidate;
                    currentEnergy = candidateEnergy;
                    acceptedTransitions++;
                    acceptedInCycle++;

                    // Check if new global best
                    boolean isNewBest = isMax ? candidateEnergy > bestEnergy : candidateEnergy < bestEnergy;
                    if (isNewBest) {
                        best = Arrays.copyOf(candidate, dim);
                        bestEnergy = candidateEnergy;
                        stagnantCycles = 0; // Reset stagnation counter
                    }
                }
            }

            // Snapshot record
            OptimizationResult.IterationRecord record = new OptimizationResult.IterationRecord(
                    cycle, temperature, currentEnergy, bestEnergy, current, best, acceptedInCycle
            );
            history.add(record);

            // Notify listeners
            for (OptimizationListener listener : listeners) {
                listener.onIteration(record);
            }

            // Check stagnation
            stagnantCycles++;
            if (stagnantCycles >= parameters.getMaxStagnantIterations()) {
                terminationReason = "Converged due to stagnation limit (" + parameters.getMaxStagnantIterations() + " cycles without improvement)";
                converged = true;
                break;
            }

            // Cool down
            temperature = schedule.nextTemperature(temperature, cycle, initialTemp);
            if (temperature <= parameters.getMinTemperature()) {
                terminationReason = "Minimum temperature threshold reached (T <= " + parameters.getMinTemperature() + ")";
                converged = true;
                break;
            }
        }

        long executionTime = System.currentTimeMillis() - startTime;

        return new OptimizationResult(
                best, bestEnergy,
                initialSolutionCopy, initialEnergy,
                cycle, totalEvaluations, acceptedTransitions,
                executionTime, converged, terminationReason,
                history
        );
    }

    public OptimizationResult optimize() {
        return optimize(null);
    }

    private boolean isFeasible(double[] point) {
        for (Constraint constraint : constraints) {
            if (!constraint.isSatisfied(point)) {
                return false;
            }
        }
        return true;
    }

    private double[] generateInitialFeasiblePoint(Random random, double[] lowerBounds, double[] upperBounds) {
        int dim = lowerBounds.length;
        double[] point = new double[dim];
        int attempts = 0;
        int maxAttempts = 1000;

        while (attempts < maxAttempts) {
            for (int i = 0; i < dim; i++) {
                point[i] = lowerBounds[i] + random.nextDouble() * (upperBounds[i] - lowerBounds[i]);
            }
            if (isFeasible(point)) {
                return point;
            }
            attempts++;
        }

        // If random sampling failed to find feasible point, default to midpoints
        for (int i = 0; i < dim; i++) {
            point[i] = (lowerBounds[i] + upperBounds[i]) / 2.0;
        }
        return point;
    }
}
