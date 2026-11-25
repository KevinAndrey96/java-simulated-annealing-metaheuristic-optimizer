package com.kevinandrey.optimizer.annealing;

import java.util.Random;

/**
 * Generates neighboring candidate solutions within decision space bounds.
 */
public interface NeighborGenerator {

    /**
     * Produces a perturbed neighbor candidate vector from current point.
     *
     * @param currentPoint current solution coordinates
     * @param lowerBounds lower bounds vector
     * @param upperBounds upper bounds vector
     * @param temperature current temperature for adaptive step sizing
     * @param random pseudo-random generator
     * @return perturbed feasible or bounded candidate vector
     */
    double[] generateNeighbor(double[] currentPoint, double[] lowerBounds, double[] upperBounds,
                              double temperature, Random random);

    /**
     * Uniform perturbation neighbor generator with fixed or temperature-scaled step size.
     */
    class Uniform implements NeighborGenerator {
        private final double stepScale;
        private final boolean adaptiveWithTemperature;

        public Uniform(double stepScale, boolean adaptiveWithTemperature) {
            this.stepScale = stepScale;
            this.adaptiveWithTemperature = adaptiveWithTemperature;
        }

        public Uniform(double stepScale) {
            this(stepScale, false);
        }

        public Uniform() {
            this(0.1, false);
        }

        @Override
        public double[] generateNeighbor(double[] currentPoint, double[] lowerBounds, double[] upperBounds,
                                         double temperature, Random random) {
            int dim = currentPoint.length;
            double[] neighbor = new double[dim];
            double effectiveScale = stepScale;
            if (adaptiveWithTemperature && temperature > 0.0) {
                effectiveScale = stepScale * Math.min(1.0, Math.sqrt(temperature));
            }

            for (int i = 0; i < dim; i++) {
                double range = upperBounds[i] - lowerBounds[i];
                double delta = (2.0 * random.nextDouble() - 1.0) * effectiveScale * range;
                double val = currentPoint[i] + delta;
                // Clamp within bounds
                if (val < lowerBounds[i]) val = lowerBounds[i];
                if (val > upperBounds[i]) val = upperBounds[i];
                neighbor[i] = val;
            }
            return neighbor;
        }
    }

    /**
     * Gaussian perturbation neighbor generator.
     */
    class Gaussian implements NeighborGenerator {
        private final double standardDeviation;

        public Gaussian(double standardDeviation) {
            this.standardDeviation = standardDeviation;
        }

        public Gaussian() {
            this(0.1);
        }

        @Override
        public double[] generateNeighbor(double[] currentPoint, double[] lowerBounds, double[] upperBounds,
                                         double temperature, Random random) {
            int dim = currentPoint.length;
            double[] neighbor = new double[dim];

            for (int i = 0; i < dim; i++) {
                double range = upperBounds[i] - lowerBounds[i];
                double delta = random.nextGaussian() * standardDeviation * range;
                double val = currentPoint[i] + delta;
                if (val < lowerBounds[i]) val = lowerBounds[i];
                if (val > upperBounds[i]) val = upperBounds[i];
                neighbor[i] = val;
            }
            return neighbor;
        }
    }
}
