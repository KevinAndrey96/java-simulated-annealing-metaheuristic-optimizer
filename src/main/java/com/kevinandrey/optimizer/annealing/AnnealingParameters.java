package com.kevinandrey.optimizer.annealing;

/**
 * Immutable configuration settings for the Simulated Annealing engine.
 */
public class AnnealingParameters {
    private final double initialTemperature;
    private final double minTemperature;
    private final int iterationsPerTemperature;
    private final int maxIterations;
    private final int maxStagnantIterations;
    private final CoolingSchedule coolingSchedule;
    private final NeighborGenerator neighborGenerator;
    private final Long randomSeed;

    private AnnealingParameters(Builder builder) {
        this.initialTemperature = builder.initialTemperature;
        this.minTemperature = builder.minTemperature;
        this.iterationsPerTemperature = builder.iterationsPerTemperature;
        this.maxIterations = builder.maxIterations;
        this.maxStagnantIterations = builder.maxStagnantIterations;
        this.coolingSchedule = builder.coolingSchedule;
        this.neighborGenerator = builder.neighborGenerator;
        this.randomSeed = builder.randomSeed;
    }

    public double getInitialTemperature() { return initialTemperature; }
    public double getMinTemperature() { return minTemperature; }
    public int getIterationsPerTemperature() { return iterationsPerTemperature; }
    public int getMaxIterations() { return maxIterations; }
    public int getMaxStagnantIterations() { return maxStagnantIterations; }
    public CoolingSchedule getCoolingSchedule() { return coolingSchedule; }
    public NeighborGenerator getNeighborGenerator() { return neighborGenerator; }
    public Long getRandomSeed() { return randomSeed; }

    public static Builder builder() {
        return new Builder();
    }

    public static AnnealingParameters defaultParameters() {
        return builder().build();
    }

    public static class Builder {
        private double initialTemperature = 1000.0;
        private double minTemperature = 1e-4;
        private int iterationsPerTemperature = 50;
        private int maxIterations = 2000;
        private int maxStagnantIterations = 300;
        private CoolingSchedule coolingSchedule = new CoolingSchedule.Geometric(0.95);
        private NeighborGenerator neighborGenerator = new NeighborGenerator.Uniform(0.05);
        private Long randomSeed = null;

        public Builder initialTemperature(double initialTemperature) {
            this.initialTemperature = initialTemperature;
            return this;
        }

        public Builder minTemperature(double minTemperature) {
            this.minTemperature = minTemperature;
            return this;
        }

        public Builder iterationsPerTemperature(int iterationsPerTemperature) {
            this.iterationsPerTemperature = iterationsPerTemperature;
            return this;
        }

        public Builder maxIterations(int maxIterations) {
            this.maxIterations = maxIterations;
            return this;
        }

        public Builder maxStagnantIterations(int maxStagnantIterations) {
            this.maxStagnantIterations = maxStagnantIterations;
            return this;
        }

        public Builder coolingSchedule(CoolingSchedule coolingSchedule) {
            this.coolingSchedule = coolingSchedule;
            return this;
        }

        public Builder neighborGenerator(NeighborGenerator neighborGenerator) {
            this.neighborGenerator = neighborGenerator;
            return this;
        }

        public Builder randomSeed(Long randomSeed) {
            this.randomSeed = randomSeed;
            return this;
        }

        public AnnealingParameters build() {
            return new AnnealingParameters(this);
        }
    }
}
