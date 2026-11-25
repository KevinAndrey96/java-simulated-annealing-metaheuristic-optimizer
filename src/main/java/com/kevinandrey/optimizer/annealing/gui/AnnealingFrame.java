package com.kevinandrey.optimizer.annealing.gui;

import com.kevinandrey.optimizer.annealing.*;
import com.kevinandrey.optimizer.annealing.benchmarks.*;
import com.kevinandrey.optimizer.annealing.export.ConvergenceChartGenerator;
import com.kevinandrey.optimizer.annealing.export.HistoryExporter;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Modern Swing interactive dashboard for Simulated Annealing optimization.
 */
public class AnnealingFrame extends JFrame {

    private final JComboBox<String> benchmarkCombo;
    private final JTextField tempField;
    private final JTextField alphaField;
    private final JTextField iterField;
    private final JTextField maxIterField;
    private final JButton runButton;
    private final JButton exportCsvButton;
    private final JProgressBar progressBar;
    private final JLabel statusLabel;
    private final JLabel bestResultLabel;
    private final JTable resultsTable;
    private final DefaultTableModel tableModel;
    private final JPanel chartContainer;

    private OptimizationResult lastResult;

    public AnnealingFrame() {
        super("Simulated Annealing Metaheuristic Optimizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);

        // Control Panel
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder("Annealing Hyperparameters"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        benchmarkCombo = new JComboBox<>(new String[]{
                "Constrained Linear Problem (2*x1 + x2)",
                "Sphere Function (3D)",
                "Rastrigin Function (2D)",
                "Rosenbrock Banana Function (2D)",
                "Ackley Function (2D)"
        });

        tempField = new JTextField("1000.0", 6);
        alphaField = new JTextField("0.95", 5);
        iterField = new JTextField("50", 4);
        maxIterField = new JTextField("1200", 5);

        runButton = new JButton("Run Simulated Annealing");
        runButton.setFont(runButton.getFont().deriveFont(Font.BOLD));
        runButton.setBackground(new Color(30, 130, 76));
        runButton.setForeground(Color.WHITE);

        exportCsvButton = new JButton("Export Trajectory CSV");
        exportCsvButton.setEnabled(false);

        gbc.gridx = 0; gbc.gridy = 0;
        controlPanel.add(new JLabel("Benchmark:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        controlPanel.add(benchmarkCombo, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        controlPanel.add(new JLabel("Initial Temp (T0):"), gbc);
        gbc.gridx = 1;
        controlPanel.add(tempField, gbc);

        gbc.gridx = 2;
        controlPanel.add(new JLabel("Cooling Rate (alpha):"), gbc);
        gbc.gridx = 3;
        controlPanel.add(alphaField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        controlPanel.add(new JLabel("Inner Loops (Markov):"), gbc);
        gbc.gridx = 1;
        controlPanel.add(iterField, gbc);

        gbc.gridx = 2;
        controlPanel.add(new JLabel("Max Cycles:"), gbc);
        gbc.gridx = 3;
        controlPanel.add(maxIterField, gbc);

        gbc.gridx = 4; gbc.gridy = 0; gbc.gridheight = 2;
        controlPanel.add(runButton, gbc);

        gbc.gridy = 2; gbc.gridheight = 1;
        controlPanel.add(exportCsvButton, gbc);

        // Chart Container
        chartContainer = new JPanel(new BorderLayout());
        chartContainer.setBorder(BorderFactory.createTitledBorder("Convergence Trajectory"));
        chartContainer.setBackground(Color.WHITE);
        JLabel placeholder = new JLabel("Click 'Run Simulated Annealing' to visualize optimization convergence.", SwingConstants.CENTER);
        chartContainer.add(placeholder, BorderLayout.CENTER);

        // Table Panel
        String[] columnNames = {"Cycle", "Temperature", "Current Energy", "Best Energy"};
        tableModel = new DefaultTableModel(columnNames, 0);
        resultsTable = new JTable(tableModel);
        resultsTable.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setPreferredSize(new Dimension(380, 200));
        scrollPane.setBorder(BorderFactory.createTitledBorder("Optimization Log"));

        // Split center
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chartContainer, scrollPane);
        splitPane.setResizeWeight(0.65);

        // Status bar
        JPanel statusPanel = new JPanel(new BorderLayout(10, 0));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        statusLabel = new JLabel("Ready.");
        bestResultLabel = new JLabel("Best: -");
        bestResultLabel.setFont(bestResultLabel.getFont().deriveFont(Font.BOLD));

        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(progressBar, BorderLayout.CENTER);
        statusPanel.add(bestResultLabel, BorderLayout.EAST);

        // Main Layout
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(controlPanel, BorderLayout.NORTH);
        getContentPane().add(splitPane, BorderLayout.CENTER);
        getContentPane().add(statusPanel, BorderLayout.SOUTH);

        // Wire Actions
        runButton.addActionListener(e -> executeOptimization());
        exportCsvButton.addActionListener(e -> exportCsv());
    }

    private void executeOptimization() {
        runButton.setEnabled(false);
        exportCsvButton.setEnabled(false);
        progressBar.setValue(0);
        statusLabel.setText("Optimizing...");
        tableModel.setRowCount(0);

        int selectedIndex = benchmarkCombo.getSelectedIndex();
        double initialTemp = Double.parseDouble(tempField.getText().trim());
        double alpha = Double.parseDouble(alphaField.getText().trim());
        int iterPerTemp = Integer.parseInt(iterField.getText().trim());
        int maxIter = Integer.parseInt(maxIterField.getText().trim());

        SwingWorker<OptimizationResult, Void> worker = new SwingWorker<OptimizationResult, Void>() {
            @Override
            protected OptimizationResult doInBackground() {
                ObjectiveFunction func;
                List<Constraint> constraints = null;

                switch (selectedIndex) {
                    case 1: func = new SphereFunction(3); break;
                    case 2: func = new RastriginFunction(2); break;
                    case 3: func = new RosenbrockFunction(2); break;
                    case 4: func = new AckleyFunction(2); break;
                    case 0:
                    default:
                        func = new ConstrainedLinearFunction();
                        constraints = ConstrainedLinearFunction.createConstraints();
                        break;
                }

                AnnealingParameters params = AnnealingParameters.builder()
                        .initialTemperature(initialTemp)
                        .coolingSchedule(new CoolingSchedule.Geometric(alpha))
                        .iterationsPerTemperature(iterPerTemp)
                        .maxIterations(maxIter)
                        .build();

                SimulatedAnnealingEngine engine = new SimulatedAnnealingEngine(func, constraints, params);
                return engine.optimize();
            }

            @Override
            protected void done() {
                try {
                    lastResult = get();
                    updateUiWithResult(lastResult);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AnnealingFrame.this,
                            "Error during optimization: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    runButton.setEnabled(true);
                    exportCsvButton.setEnabled(lastResult != null);
                }
            }
        };

        worker.execute();
    }

    private void updateUiWithResult(OptimizationResult result) {
        progressBar.setValue(100);
        statusLabel.setText("Finished in " + result.getExecutionTimeMillis() + " ms (" + result.getTerminationReason() + ")");
        bestResultLabel.setText(String.format("Best Energy: %.6f at %s", result.getBestEnergy(), Arrays.toString(result.getBestSolution())));

        // Populate table (sampled for performance)
        List<OptimizationResult.IterationRecord> history = result.getHistory();
        int step = Math.max(1, history.size() / 150);
        for (int i = 0; i < history.size(); i += step) {
            OptimizationResult.IterationRecord rec = history.get(i);
            tableModel.addRow(new Object[]{
                    rec.getIteration(),
                    String.format("%.4f", rec.getTemperature()),
                    String.format("%.4f", rec.getCurrentEnergy()),
                    String.format("%.4f", rec.getBestEnergy())
            });
        }

        // Render Chart
        JFreeChart chart = ConvergenceChartGenerator.createConvergenceChart(result, (String) benchmarkCombo.getSelectedItem());
        chartContainer.removeAll();
        chartContainer.add(new ChartPanel(chart), BorderLayout.CENTER);
        chartContainer.revalidate();
        chartContainer.repaint();
    }

    private void exportCsv() {
        if (lastResult == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("annealing_trajectory.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                HistoryExporter.exportToCsv(lastResult, chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "CSV exported successfully!", "Export", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to export: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void launch() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            AnnealingFrame frame = new AnnealingFrame();
            frame.setVisible(true);
        });
    }

    public static void main(String[] args) {
        launch();
    }
}
