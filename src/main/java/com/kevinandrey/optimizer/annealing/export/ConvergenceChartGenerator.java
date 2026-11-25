package com.kevinandrey.optimizer.annealing.export;

import com.kevinandrey.optimizer.annealing.OptimizationResult;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.File;
import java.io.IOException;

/**
 * Headless chart rendering generator using JFreeChart.
 */
public class ConvergenceChartGenerator {

    /**
     * Builds a JFreeChart object representing best energy and current energy across cycles.
     */
    public static JFreeChart createConvergenceChart(OptimizationResult result, String title) {
        XYSeries bestSeries = new XYSeries("Best Energy (Optimal)");
        XYSeries currentSeries = new XYSeries("Current State Energy");

        for (OptimizationResult.IterationRecord rec : result.getHistory()) {
            bestSeries.add(rec.getIteration(), rec.getBestEnergy());
            currentSeries.add(rec.getIteration(), rec.getCurrentEnergy());
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(bestSeries);
        dataset.addSeries(currentSeries);

        return ChartFactory.createXYLineChart(
                title != null ? title : "Simulated Annealing Convergence Trajectory",
                "Temperature Cycle (Iteration)",
                "Objective Value (Energy)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
    }

    /**
     * Exports convergence chart as a PNG image file.
     *
     * @param result optimization result
     * @param targetFile destination PNG file
     * @param width image width in pixels
     * @param height image height in pixels
     * @throws IOException on write error
     */
    public static void exportChartToPng(OptimizationResult result, File targetFile, int width, int height) throws IOException {
        if (targetFile.getParentFile() != null) {
            targetFile.getParentFile().mkdirs();
        }
        JFreeChart chart = createConvergenceChart(result, "Simulated Annealing Convergence");
        ChartUtils.saveChartAsPNG(targetFile, chart, width, height);
    }
}
