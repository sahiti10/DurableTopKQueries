/**
 * ExperimentalRunner
 * ---------------------
 * Main entry point for running durable top-k experiments.
 *
 * Runs all 7 algorithms on one or more datasets, tracks runtime and memory usage, and compares result sets for accuracy evaluation.
 *
 * Supports:
 * - Demo mode (predefined dataset and parameters)
 * - Evaluation mode (benchmarking across multiple datasets)
 * - Export of results via ResultsLogger
 */

 package durabletopk;

 import java.util.*;
 import java.util.function.Supplier;
 
 public class ExperimentalRunner {
     public static void main(String[] args) throws Exception {
         // === EXECUTION + DEMO Dataset ===
         String demoDataset = "data/dense_stock_synthetic.csv";
         runAllAlgorithms("DEMO", demoDataset, true, true);
 
         // === TESTING/EVALUATION Datasets ===
         String[] evalDatasets = {
            "data/Florida_Temp_Data_Preprocessed.csv",
            "data/ar1_dataset.csv"
         };
 
         for (String path : evalDatasets) {
             runAllAlgorithms("EVAL", path, false, true);
         }
 
         ResultsLogger.exportCSV("results_summary.csv");
     }
 
     public static void runAllAlgorithms(String mode, String filePath, boolean printResults, boolean timeIt) throws Exception {
         System.out.println("\n=========================================");
         System.out.println((mode.equals("DEMO") ? "Running DEMO on" : "Evaluating") + " Dataset: " + filePath);
         System.out.println("=========================================");
 
         List<TemporalObject> objects = DataLoader.loadFromCSV(filePath);
 
         int totalTime = objects.stream()
                 .flatMap(obj -> obj.timeSeries.keySet().stream())
                 .max(Integer::compareTo)
                 .orElse(0);
 
         int k = 10;
         double tau = 0.05;
         int startTime = 1;
         int endTime = 1000;
 
         List<Integer> ks = Arrays.asList(5, 10, 15);
 
         System.out.println("Config: start=" + startTime + ", end=" + endTime + ", k=" + k + ", tau=" + tau);
         System.out.println("------------------------------------------------");
 
         runWithMetrics("PrefixSum", timeIt, () -> {
             PrefixSumDurableTopK prefix = new PrefixSumDurableTopK(objects, k, startTime, endTime);
             return prefix.query(startTime, endTime, tau);
         }, printResults);
 
         runWithMetrics("IntervalIndex", timeIt, () -> {
             IntervalIndexDurableTopK interval = new IntervalIndexDurableTopK(objects, k);
             return interval.query(startTime, endTime, tau);
         }, printResults);
 
         runWithMetrics("Geometric", timeIt, () -> {
             GeometricDurableTopK geometric = new GeometricDurableTopK(objects, k);
             return geometric.query(startTime, endTime, tau);
         }, printResults);
 
         runWithMetrics("Sampling", timeIt, () -> {
             SamplingDurableTopK sampling = new SamplingDurableTopK(objects);
             return sampling.query(k, startTime, endTime, tau, 20);
         }, printResults);
 
         runWithMetrics("ObliviousIndex", timeIt, () -> {
             ObliviousIndexDurableTopK dos = new ObliviousIndexDurableTopK(objects, ks);
             return dos.query(k, startTime, endTime, tau);
         }, printResults);
 
         runWithMetrics("ColumnIndex", timeIt, () -> {
             ColumnIndexDurableTopK col = new ColumnIndexDurableTopK(objects, ks, totalTime);
             return col.query(k, tau);
         }, printResults);
 
         runWithMetrics("CellWiseIndex", timeIt, () -> {
             CellWiseIndexDurableTopK cel = new CellWiseIndexDurableTopK(objects, ks, totalTime);
             return cel.query(k, tau);
         }, printResults);
     }
 
     public static void runWithMetrics(String name, boolean timeIt, Supplier<List<Integer>> method, boolean printResult) {
         Runtime rt = Runtime.getRuntime();
         long memBefore = rt.totalMemory() - rt.freeMemory();
         long start = System.currentTimeMillis();
         List<Integer> result = method.get();
         long end = System.currentTimeMillis();
         long memAfter = rt.totalMemory() - rt.freeMemory();
         long runtime = end - start;
         long memUsed = memAfter - memBefore;
 
         if (timeIt) {
             System.out.println(name + " completed in " + runtime + " ms");
         }
         if (printResult) {
             print(name, result);
         }
 
         ResultsLogger.log(name, result, runtime, memUsed);
     }
 
     public static void print(String label, List<Integer> result) {
         System.out.println(label + " Top-k Result: " + result);
         System.out.println("----------------------------------");
     }
 }