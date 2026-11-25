package com.kevinandrey.optimizer.annealing;

/**
 * Strategy interface defining temperature decay schedules during annealing.
 */
public interface CoolingSchedule {

    /**
     * Calculates the next temperature in the cooling process.
     *
     * @param currentTemperature current temperature T_k
     * @param iteration current temperature cycle index k
     * @param initialTemperature initial starting temperature T_0
     * @return new reduced temperature T_{k+1}
     */
    double nextTemperature(double currentTemperature, int iteration, double initialTemperature);

    /**
     * Descriptive name of the cooling strategy.
     */
    String getName();

    /**
     * Geometric cooling schedule: T_{k+1} = alpha * T_k (alpha in (0, 1), typically 0.80 - 0.999).
     */
    class Geometric implements CoolingSchedule {
        private final double alpha;

        public Geometric(double alpha) {
            if (alpha <= 0.0 || alpha >= 1.0) {
                throw new IllegalArgumentException("Alpha must be strictly in (0.0, 1.0), provided: " + alpha);
            }
            this.alpha = alpha;
        }

        public double getAlpha() {
            return alpha;
        }

        @Override
        public double nextTemperature(double currentTemperature, int iteration, double initialTemperature) {
            return currentTemperature * alpha;
        }

        @Override
        public String getName() {
            return "Geometric (alpha=" + alpha + ")";
        }
    }

    /**
     * Linear cooling schedule: T_{k+1} = max(T_min, T_k - beta).
     */
    class Linear implements CoolingSchedule {
        private final double beta;

        public Linear(double beta) {
            if (beta <= 0.0) {
                throw new IllegalArgumentException("Beta step must be positive, provided: " + beta);
            }
            this.beta = beta;
        }

        @Override
        public double nextTemperature(double currentTemperature, int iteration, double initialTemperature) {
            return Math.max(1e-8, currentTemperature - beta);
        }

        @Override
        public String getName() {
            return "Linear (beta=" + beta + ")";
        }
    }

    /**
     * Logarithmic cooling schedule (Hajek / Geman-Geman): T_k = T_0 / ln(1 + k).
     * Guaranteed asymptotic convergence to global optimum under infinite time.
     */
    class Logarithmic implements CoolingSchedule {
        private final double factor;

        public Logarithmic(double factor) {
            this.factor = factor;
        }

        public Logarithmic() {
            this(1.0);
        }

        @Override
        public double nextTemperature(double currentTemperature, int iteration, double initialTemperature) {
            return initialTemperature / (1.0 + factor * Math.log(1.0 + iteration));
        }

        @Override
        public String getName() {
            return "Logarithmic (factor=" + factor + ")";
        }
    }

    /**
     * Exponential cooling schedule: T_k = T_0 * exp(-lambda * k).
     */
    class Exponential implements CoolingSchedule {
        private final double lambda;

        public Exponential(double lambda) {
            if (lambda <= 0.0) {
                throw new IllegalArgumentException("Lambda must be positive, provided: " + lambda);
            }
            this.lambda = lambda;
        }

        @Override
        public double nextTemperature(double currentTemperature, int iteration, double initialTemperature) {
            return initialTemperature * Math.exp(-lambda * iteration);
        }

        @Override
        public String getName() {
            return "Exponential (lambda=" + lambda + ")";
        }
    }
}
