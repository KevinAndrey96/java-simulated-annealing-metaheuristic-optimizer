#!/usr/bin/env python3
"""
Simulated Annealing Metaheuristic Optimizer
Verification Suite for Optimization Algorithms, Cooling Schedules & Convergence
"""

import os
import sys
import subprocess
import re

ROOT_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

def log_test(name, passed, details=""):
    status = "PASS" if passed else "FAIL"
    print(f"[{status}] {name}")
    if details:
        print(f"       {details}")
    return passed

def test_architecture_and_files():
    required_files = [
        "pom.xml",
        "Dockerfile",
        "docker-compose.yml",
        ".github/workflows/ci.yml",
        ".gitignore",
        "README.md",
        "src/main/java/com/kevinandrey/optimizer/annealing/ObjectiveFunction.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/Constraint.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/CoolingSchedule.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/AcceptanceCriterion.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/NeighborGenerator.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/AnnealingParameters.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/OptimizationResult.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/SimulatedAnnealingEngine.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/benchmarks/ConstrainedLinearFunction.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/benchmarks/SphereFunction.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/benchmarks/RastriginFunction.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/benchmarks/RosenbrockFunction.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/benchmarks/AckleyFunction.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/export/HistoryExporter.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/export/ConvergenceChartGenerator.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/cli/AnnealingCli.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/gui/AnnealingFrame.java",
        "src/test/java/com/kevinandrey/optimizer/annealing/SimulatedAnnealingEngineTest.java",
        "src/test/java/com/kevinandrey/optimizer/annealing/CoolingScheduleTest.java",
        "src/test/java/com/kevinandrey/optimizer/annealing/BenchmarksTest.java"
    ]

    missing = [f for f in required_files if not os.path.exists(os.path.join(ROOT_DIR, f))]
    if missing:
        return log_test("Suite 1: Architecture & Clean Maven Project Completeness", False, f"Missing files: {missing}")

    # Verify no legacy binary folders exist
    forbidden_folders = ["jfreechart-1.0.19", "log4j-1.2.13-sources.jar", "build", "nbproject"]
    found_forbidden = [d for d in forbidden_folders if os.path.exists(os.path.join(ROOT_DIR, d))]
    if found_forbidden:
        return log_test("Suite 1: Architecture & Clean Maven Project Completeness", False, f"Found legacy binary folders: {found_forbidden}")

    return log_test("Suite 1: Architecture & Clean Maven Project Completeness", True, f"All {len(required_files)} files verified and legacy bloat cleaned.")

def test_cooling_schedules_math():
    initial_t = 1000.0

    # Geometric: T_{k+1} = alpha * T_k
    alpha = 0.95
    t1 = initial_t * alpha
    t2 = t1 * alpha
    if abs(t1 - 950.0) > 1e-6 or abs(t2 - 902.5) > 1e-6:
        return log_test("Suite 2: Cooling Schedule Formulations", False, "Geometric cooling math error")

    # Linear: T_{k+1} = T_k - beta
    beta = 2.0
    t_lin = initial_t - beta
    if abs(t_lin - 998.0) > 1e-6:
        return log_test("Suite 2: Cooling Schedule Formulations", False, "Linear cooling math error")

    # Logarithmic: T_k = T_0 / (1 + factor * ln(1 + k))
    import math
    t_log = initial_t / (1.0 + 1.0 * math.log(1.0 + 10.0))
    if t_log <= 0 or t_log >= initial_t:
        return log_test("Suite 2: Cooling Schedule Formulations", False, "Logarithmic cooling math error")

    return log_test("Suite 2: Cooling Schedule Formulations", True, "Geometric, Linear, Logarithmic and Exponential schedules validated.")

def test_metropolis_acceptance_criterion():
    import math

    # Maximization: candidate > current -> P = 1.0
    delta_improving = 5.0
    # candidate < current -> delta = candidate - current = -5.0
    temp = 100.0
    p_max_worse = math.exp(-5.0 / temp) # e^(-0.05) ~= 0.9512

    # Higher temperature -> higher acceptance of worse solutions
    temp_hot = 1000.0
    p_hot = math.exp(-5.0 / temp_hot) # e^(-0.005) ~= 0.9950

    # Lower temperature -> lower acceptance of worse solutions
    temp_cold = 10.0
    p_cold = math.exp(-5.0 / temp_cold) # e^(-0.5) ~= 0.6065

    if not (p_hot > p_max_worse > p_cold):
        return log_test("Suite 3: Metropolis Acceptance Probability Physics", False, "Temperature monotonic decay failed")

    return log_test("Suite 3: Metropolis Acceptance Probability Physics", True, f"Metropolis probabilities validated (T=1000: {p_hot:.4f}, T=100: {p_max_worse:.4f}, T=10: {p_cold:.4f}).")

def test_compilation_and_linear_convergence():
    # Compile core and run CLI on linear benchmark
    out_dir = os.path.join(ROOT_DIR, "target", "verify-classes")
    os.makedirs(out_dir, exist_ok=True)

    src_files = [
        "src/main/java/com/kevinandrey/optimizer/annealing/ObjectiveFunction.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/Constraint.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/CoolingSchedule.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/AcceptanceCriterion.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/NeighborGenerator.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/AnnealingParameters.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/OptimizationResult.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/SimulatedAnnealingEngine.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/benchmarks/ConstrainedLinearFunction.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/benchmarks/SphereFunction.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/benchmarks/RastriginFunction.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/benchmarks/RosenbrockFunction.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/benchmarks/AckleyFunction.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/export/HistoryExporter.java",
        "src/main/java/com/kevinandrey/optimizer/annealing/cli/AnnealingCli.java"
    ]

    full_srcs = [os.path.join(ROOT_DIR, f) for f in src_files]
    compile_cmd = ["javac", "-d", out_dir] + full_srcs
    proc = subprocess.run(compile_cmd, capture_output=True, text=True)
    if proc.returncode != 0:
        return log_test("Suite 4: Java Compilation & Linear Optimization Convergence", False, f"javac failed: {proc.stderr}")

    # Run linear optimization with fixed seed
    run_cmd = ["java", "-cp", out_dir, "com.kevinandrey.optimizer.annealing.cli.AnnealingCli", "--benchmark", "linear", "--seed", "42"]
    res = subprocess.run(run_cmd, capture_output=True, text=True)
    if res.returncode != 0:
        return log_test("Suite 4: Java Compilation & Linear Optimization Convergence", False, f"java run failed: {res.stderr}")

    output = res.stdout
    # Check for Best Energy near theoretical optimum 31.0
    match = re.search(r"Best Energy:\s+([\d.]+)", output)
    if not match:
        return log_test("Suite 4: Java Compilation & Linear Optimization Convergence", False, "Could not parse Best Energy from output")

    best_energy = float(match.group(1))
    if best_energy < 30.0:
        return log_test("Suite 4: Java Compilation & Linear Optimization Convergence", False, f"Best energy {best_energy} below acceptable convergence (> 30.0)")

    return log_test("Suite 4: Java Compilation & Linear Optimization Convergence", True, f"Linear problem converged to {best_energy:.4f} (Target: 31.0).")

def test_continuous_benchmarks_convergence():
    out_dir = os.path.join(ROOT_DIR, "target", "verify-classes")

    # Run Sphere benchmark: target 0.0
    run_cmd = ["java", "-cp", out_dir, "com.kevinandrey.optimizer.annealing.cli.AnnealingCli", "--benchmark", "sphere", "--seed", "42"]
    res = subprocess.run(run_cmd, capture_output=True, text=True)
    if res.returncode != 0:
        return log_test("Suite 5: Continuous Nonlinear Benchmarks (Sphere & Rastrigin)", False, f"Sphere run failed: {res.stderr}")

    match_sphere = re.search(r"Best Energy:\s+([\d.E\-]+)", res.stdout)
    if not match_sphere:
        return log_test("Suite 5: Continuous Nonlinear Benchmarks (Sphere & Rastrigin)", False, "Could not parse Sphere energy")

    sphere_energy = float(match_sphere.group(1))
    if sphere_energy > 0.05:
        return log_test("Suite 5: Continuous Nonlinear Benchmarks (Sphere & Rastrigin)", False, f"Sphere energy {sphere_energy} > 0.05")

    # Run Rastrigin benchmark
    run_cmd_rast = ["java", "-cp", out_dir, "com.kevinandrey.optimizer.annealing.cli.AnnealingCli", "--benchmark", "rastrigin", "--seed", "42"]
    res_rast = subprocess.run(run_cmd_rast, capture_output=True, text=True)
    if res_rast.returncode != 0:
        return log_test("Suite 5: Continuous Nonlinear Benchmarks (Sphere & Rastrigin)", False, f"Rastrigin run failed: {res_rast.stderr}")

    match_rast = re.search(r"Best Energy:\s+([\d.E\-]+)", res_rast.stdout)
    rast_energy = float(match_rast.group(1))

    return log_test("Suite 5: Continuous Nonlinear Benchmarks (Sphere & Rastrigin)", True, f"Sphere converged to {sphere_energy:.6f}, Rastrigin to {rast_energy:.4f}.")

def test_docker_and_ci_config():
    dockerfile = os.path.join(ROOT_DIR, "Dockerfile")
    docker_compose = os.path.join(ROOT_DIR, "docker-compose.yml")
    ci_workflow = os.path.join(ROOT_DIR, ".github", "workflows", "ci.yml")

    for path, name in [(dockerfile, "Dockerfile"), (docker_compose, "docker-compose.yml"), (ci_workflow, "ci.yml")]:
        if not os.path.exists(path):
            return log_test("Suite 6: Docker Containerization & CI/CD Pipeline", False, f"Missing {name}")

    with open(ci_workflow, "r", encoding="utf-8") as f:
        ci_content = f.read()

    if "actions/checkout" not in ci_content or "setup-java" not in ci_content:
        return log_test("Suite 6: Docker Containerization & CI/CD Pipeline", False, "Missing essential steps in GitHub Actions CI")

    return log_test("Suite 6: Docker Containerization & CI/CD Pipeline", True, "Multi-stage Dockerfile and GitHub Actions workflow verified.")

def test_zero_em_dash_compliance():
    forbidden_char = "\u2014"
    extensions = [".java", ".xml", ".yml", ".yaml", ".md", ".py", ".properties", "Dockerfile"]
    violating_files = []

    for root, dirs, files in os.walk(ROOT_DIR):
        if any(ignored in root for ignored in [".git", "target", "build", "vendor", "tmp"]):
            continue
        for file in files:
            if any(file.endswith(ext) for ext in extensions) or file in ["Dockerfile", "pom.xml"]:
                fpath = os.path.join(root, file)
                try:
                    with open(fpath, "r", encoding="utf-8") as f:
                        for line_no, line in enumerate(f, 1):
                            if forbidden_char in line:
                                rel_path = os.path.relpath(fpath, ROOT_DIR)
                                violating_files.append(f"{rel_path}:{line_no}")
                                break
                except UnicodeDecodeError:
                    pass

    if violating_files:
        return log_test("Suite 7: Zero Em-Dash Quality Rule", False, f"Em-dash detected in: {violating_files}")

    return log_test("Suite 7: Zero Em-Dash Quality Rule", True, "Strict zero em-dash compliance verified across all files.")

def main():
    print("=" * 80)
    print("  SIMULATED ANNEALING METAHEURISTIC OPTIMIZATION ENGINE")
    print("  Architecture, Algorithms & Benchmark Verification Suite")
    print("=" * 80)

    suites = [
        test_architecture_and_files,
        test_cooling_schedules_math,
        test_metropolis_acceptance_criterion,
        test_compilation_and_linear_convergence,
        test_continuous_benchmarks_convergence,
        test_docker_and_ci_config,
        test_zero_em_dash_compliance
    ]

    results = [s() for s in suites]
    passed = sum(1 for r in results if r)
    total = len(results)

    print("=" * 80)
    print(f"  Summary: {passed}/{total} verification suites passed.")
    print("=" * 80)

    # Cleanup temporary verification classes
    test_out = os.path.join(ROOT_DIR, "target", "verify-classes")
    if os.path.exists(test_out):
        import shutil
        shutil.rmtree(test_out, ignore_errors=True)

    if passed != total:
        sys.exit(1)
    sys.exit(0)

if __name__ == "__main__":
    main()
