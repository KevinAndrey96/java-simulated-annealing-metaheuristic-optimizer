package com.kevinandrey.optimizer.annealing.cli;

import com.kevinandrey.optimizer.annealing.*;
import com.kevinandrey.optimizer.annealing.benchmarks.*;
import com.kevinandrey.optimizer.annealing.export.HistoryExporter;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Headless Command Line Interface for running simulated annealing optimizations.
 */
public class AnnealingCli {

    public static void main(String[] args) {
        String benchmark = "linear";
        double initialTemp = 1000.0;
        double alpha = 0.95;
        int iterPerTemp = 50;
        int maxIter = 1500;
        Long seed = null;
        String exportCsvPath = null;
        String exportChartPath = null;
        boolean launchGui = false;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (("--benchmark".equals(arg) || "-b".equals(arg)) && i + 1 < args.length) {
                benchmark = args[++i].toLowerCase();
            } else if (("--temp".equals(arg) || "-t".equals(arg)) && i + 1 < args.length) {
                initialTemp = Double.parseDouble(args[++i]);
            } else if (("--alpha".equals(arg) || "-a".equals(arg)) && i + 1 < args.length) {
                alpha = Double.parseDouble(args[++i]);
            } else if (("--iter".equals(arg) || "-i".equals(arg)) && i + 1 < args.length) {
                iterPerTemp = Integer.parseInt(args[++i]);
            } else if (("--max-iter".equals(arg) || "-m".equals(arg)) && i + 1 < args.length) {
                maxIter = Integer.parseInt(args[++i]);
            } else if (("--seed".equals(arg) || "-s".equals(arg)) && i + 1 < args.length) {
                seed = Long.parseLong(args[++i]);
            } else if ("--export-csv".equals(arg) && i + 1 < args.length) {
                exportCsvPath = args[++i];
            } else if ("--export-chart".equals(arg) && i + 1 < args.length) {
                exportChartPath = args[++i];
            } else if ("--gui".equals(arg)) {
                launchGui = true;
            } else if ("--help".equals(arg) || "-h".equals(arg)) {
                printHelp();
                return;
            }
        }

        if (launchGui) {
            try {
                Class<?> guiClass = Class.forName("com.kevinandrey.optimizer.annealing.gui.AnnealingFrame");
                java.lang.reflect.Method launchMethod = guiClass.getMethod("launch");
                launchMethod.invoke(null);
                return;
            } catch (Exception e) {
                System.err.println("Failed to launch GUI (headless environment or missing desktop): " + e.getMessage());
            }
        }

        ObjectiveFunction function;
        List<Constraint> constraints = null;
        Double theoreticalOptimum = null;

        switch (benchmark) {
            case "sphere":
                function = new SphereFunction(3);
                theoreticalOptimum = 0.0;
                break;
            case "rastrigin":
                function = new RastriginFunction(2);
                theoreticalOptimum = 0.0;
                break;
            case "rosenbrock":
                function = new RosenbrockFunction(2);
                theoreticalOptimum = 0.0;
                break;
            case "ackley":
                function = new AckleyFunction(2);
                theoreticalOptimum = 0.0;
                break;
            case "linear":
            default:
                function = new ConstrainedLinearFunction();
                constraints = ConstrainedLinearFunction.createConstraints();
                theoreticalOptimum = ConstrainedLinearFunction.getTheoreticalOptimum();
                break;
        }

        AnnealingParameters params = AnnealingParameters.builder()
                .initialTemperature(initialTemp)
                .coolingSchedule(new CoolingSchedule.Geometric(alpha))
                .iterationsPerTemperature(iterPerTemp)
                .maxIterations(maxIter)
                .randomSeed(seed)
                .build();

        SimulatedAnnealingEngine engine = new SimulatedAnnealingEngine(function, constraints, params);

        System.out.println("================================================================================");
        System.out.println("  SIMULATED ANNEALING METAHEURISTIC OPTIMIZATION ENGINE");
        System.out.println("================================================================================");
        System.out.printf("  Problem:        %s%n", function.getName());
        System.out.printf("  Dimension:      %d variables%n", function.getDimension());
        System.out.printf("  Goal:           %s%n", function.isMaximization() ? "MAXIMIZATION" : "MINIMIZATION");
        System.out.printf("  Initial Temp:   %.2f (Cooling Rate: %.4f)%n", initialTemp, alpha);
        System.out.printf("  Inner Loops:    %d evals per temperature step%n", iterPerTemp);
        System.out.printf("  Max Cycles:     %d%n", maxIter);
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("  Executing annealing optimization...");

        OptimizationResult result = engine.optimize();

        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("  OPTIMIZATION COMPLETED");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("  Termination:    %s%n", result.getTerminationReason());
        System.out.printf("  Initial Energy: %.6f (point: %s)%n", result.getInitialEnergy(), Arrays.toString(result.getInitialSolution()));
        System.out.printf("  Best Energy:    %.6f (point: %s)%n", result.getBestEnergy(), Arrays.toString(result.getBestSolution()));
        if (theoreticalOptimum != null) {
            double gap = Math.abs(result.getBestEnergy() - theoreticalOptimum);
            System.out.printf("  Global Target:  %.6f (Absolute Gap: %.6f)%n", theoreticalOptimum, gap);
        }
        System.out.printf("  Total Evals:    %d%n", result.getTotalEvaluations());
        System.out.printf("  Accepted Steps: %d (%.2f%%)%n", result.getAcceptedTransitions(), result.getAcceptanceRate());
        System.out.printf("  Elapsed Time:   %d ms%n", result.getExecutionTimeMillis());
        System.out.println("================================================================================");

        if (exportCsvPath != null) {
            try {
                File csvFile = new File(exportCsvPath);
                HistoryExporter.exportToCsv(result, csvFile);
                System.out.printf("  [Export] CSV trajectory saved to: %s%n", csvFile.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("  [Error] Failed to export CSV: " + e.getMessage());
            }
        }

        if (exportChartPath != null) {
            try {
                Class<?> chartClass = Class.forName("com.kevinandrey.optimizer.annealing.export.ConvergenceChartGenerator");
                java.lang.reflect.Method method = chartClass.getMethod("exportChartToPng", OptimizationResult.class, File.class, int.class, int.class);
                method.invoke(null, result, new File(exportChartPath), 800, 500);
                System.out.printf("  [Export] Convergence chart saved to: %s%n", new File(exportChartPath).getAbsolutePath());
            } catch (Throwable e) {
                System.err.println("  [Error] Failed to export chart (JFreeChart required on classpath): " + e.getMessage());
            }
        }
    }

    private static void printHelp() {
        System.out.println("Usage: java -jar simulated-annealing-optimizer.jar [OPTIONS]");
        System.out.println("Options:");
        System.out.println("  -b, --benchmark <name>   Benchmark problem: linear, sphere, rastrigin, rosenbrock, ackley");
        System.out.println("  -t, --temp <val>         Initial starting temperature (default: 1000.0)");
        System.out.println("  -a, --alpha <val>        Geometric cooling rate in (0, 1) (default: 0.95)");
        System.out.println("  -i, --iter <n>           Number of perturbation trials per temperature step (default: 50)");
        System.out.println("  -m, --max-iter <n>       Maximum outer temperature reduction cycles (default: 1500)");
        System.out.println("  -s, --seed <long>        Deterministic pseudo-random seed");
        System.out.println("  --export-csv <path>      Export iteration trajectory to CSV file");
        System.out.println("  --export-chart <path>    Export convergence curve as PNG image");
        System.out.println("  --gui                    Launch interactive desktop Swing visualization");
        System.out.println("  -h, --help               Display this help message");
    }
}
