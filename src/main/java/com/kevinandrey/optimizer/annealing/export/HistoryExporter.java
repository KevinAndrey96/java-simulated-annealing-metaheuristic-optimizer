package com.kevinandrey.optimizer.annealing.export;

import com.kevinandrey.optimizer.annealing.OptimizationResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Exporter utility for writing annealing trajectory telemetry into CSV format.
 */
public class HistoryExporter {

    /**
     * Exports optimization history records to CSV file.
     *
     * @param result optimization result with history
     * @param targetFile destination file
     * @throws IOException on write error
     */
    public static void exportToCsv(OptimizationResult result, File targetFile) throws IOException {
        if (targetFile.getParentFile() != null) {
            targetFile.getParentFile().mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile))) {
            writer.write("Iteration,Temperature,CurrentEnergy,BestEnergy,AcceptedCount\n");
            for (OptimizationResult.IterationRecord record : result.getHistory()) {
                writer.write(String.format("%d,%.6f,%.6f,%.6f,%d\n",
                        record.getIteration(),
                        record.getTemperature(),
                        record.getCurrentEnergy(),
                        record.getBestEnergy(),
                        record.getAcceptedCount()));
            }
        }
    }
}
