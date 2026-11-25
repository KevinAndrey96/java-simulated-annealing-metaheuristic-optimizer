package com.kevinandrey.optimizer.annealing;

import java.util.Random;

/**
 * Evaluates candidate state acceptance probability according to statistical mechanics principles.
 */
public class AcceptanceCriterion {

    /**
     * Evaluates whether a new candidate solution should be accepted according to the Metropolis rule.
     *
     * @param currentEnergy current solution objective value f(x_curr)
     * @param candidateEnergy candidate solution objective value f(x_new)
     * @param temperature current temperature T
     * @param isMaximization true if maximizing, false if minimizing
     * @param random random number generator for stochastic threshold
     * @return true if accepted, false otherwise
     */
    public static boolean shouldAccept(double currentEnergy, double candidateEnergy,
                                       double temperature, boolean isMaximization,
                                       Random random) {
        if (temperature <= 0.0) {
            return isMaximization ? candidateEnergy >= currentEnergy : candidateEnergy <= currentEnergy;
        }

        double delta;
        if (isMaximization) {
            delta = candidateEnergy - currentEnergy;
            if (delta >= 0.0) {
                return true; // Improving solution
            }
            double probability = Math.exp(delta / temperature);
            return random.nextDouble() < probability;
        } else {
            delta = candidateEnergy - currentEnergy;
            if (delta <= 0.0) {
                return true; // Improving solution
            }
            double probability = Math.exp(-delta / temperature);
            return random.nextDouble() < probability;
        }
    }

    /**
     * Calculates the acceptance probability P without sampling random.
     */
    public static double calculateProbability(double currentEnergy, double candidateEnergy,
                                              double temperature, boolean isMaximization) {
        if (temperature <= 0.0) {
            return (isMaximization ? candidateEnergy >= currentEnergy : candidateEnergy <= currentEnergy) ? 1.0 : 0.0;
        }

        double delta;
        if (isMaximization) {
            delta = candidateEnergy - currentEnergy;
            if (delta >= 0.0) {
                return 1.0;
            }
            return Math.min(1.0, Math.max(0.0, Math.exp(delta / temperature)));
        } else {
            delta = candidateEnergy - currentEnergy;
            if (delta <= 0.0) {
                return 1.0;
            }
            return Math.min(1.0, Math.max(0.0, Math.exp(-delta / temperature)));
        }
    }
}
