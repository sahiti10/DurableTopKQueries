package durabletopk;

import java.util.*;
import java.util.function.Supplier;

public class VariableKRun {
    public static void main(String[] args) throws Exception {
        String dataset = "data/dense_stock_synthetic.csv";
        runAllAlgorithms(dataset, true, true);
        ResultsLogger.exportCSV("results_summary.csv");
    }

    public static void runAllAlgorithms(String filePath, boolean printResults, boolean timeIt) throws Exception {
        System.out.println("\n---------------------------------------");
        System.out.println("\n---------------------------------------");
        System.out.println("Running Variable-k on Dataset: " + filePath);
        System.out.println("\n---------------------------------------");

        List<TemporalObject> objects = LoadCSVData.loadFromCSV(filePath);

        int totalTime = objects.stream()
                .flatMap(obj -> obj.timeSeries.keySet().stream())
                .mapToInt(t -> t)
                .max()
                .orElse(0);

        double tau = 0.05;
        int startTime = 1;
        int endTime = Math.min(1000, totalTime);
        List<Integer> ks = Arrays.asList(5, 10, 15, 20);
        List<Integer> indexedKs = Arrays.asList(5, 10, 15, 20);

        System.out.println("Config: start=" + startTime + ", end=" + endTime + ", tau=" + tau);
        System.out.println("------------------------------------------------");

        for (int k : ks) {
            System.out.println("\n>>> Running for k = " + k);

            runWithMetrics("PrefixSum_k=" + k, timeIt, () -> {
                PrefixSumDurableTopK prefix = new PrefixSumDurableTopK(objects, k, startTime, endTime);
                return limitK(prefix.query(startTime, endTime, tau), k);
            }, printResults);

            runWithMetrics("IntervalIndex_k=" + k, timeIt, () -> {
                IntervalIndexDurableTopK interval = new IntervalIndexDurableTopK(objects, k);
                return limitK(interval.query(startTime, endTime, tau), k);
            }, printResults);

            runWithMetrics("Geometric_k=" + k, timeIt, () -> {
                GeometricDurableTopK geometric = new GeometricDurableTopK(objects, k);
                return limitK(geometric.query(startTime, endTime, tau), k);
            }, printResults);

            runWithMetrics("Sampling_k=" + k, timeIt, () -> {
                SamplingDurableTopK sampling = new SamplingDurableTopK(objects);
                return limitK(sampling.query(k, startTime, endTime, tau, 20), k);
            }, printResults);

            runWithMetrics("ObliviousIndex_k=" + k, timeIt, () -> {
                ObliviousIndexDurableTopK dos = new ObliviousIndexDurableTopK(objects, indexedKs);
                return limitK(dos.query(k, startTime, endTime, tau), k);
            }, printResults);

            runWithMetrics("ColumnIndex_k=" + k, timeIt, () -> {
                ColumnIndexDurableTopK col = new ColumnIndexDurableTopK(objects, indexedKs, totalTime);
                return limitK(col.query(k, tau), k);
            }, printResults);

            runWithMetrics("CellWiseIndex_k=" + k, timeIt, () -> {
                CellWiseIndexDurableTopK cel = new CellWiseIndexDurableTopK(objects, indexedKs, totalTime);
                return limitK(cel.query(k, tau), k);
            }, printResults);
        }
    }

    public static void runWithMetrics(String name, boolean timeIt, Supplier<List<Integer>> method, boolean printResult) {
        long beforeUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long start = System.currentTimeMillis();
        List<Integer> result = method.get();
        long end = System.currentTimeMillis();
        long afterUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long runtime = end - start;
        long memUsed = afterUsedMem - beforeUsedMem;

        if (timeIt) {
            System.out.println(name + " completed in " + runtime + " ms");
        }
        if (printResult) {
            System.out.println(name + " Top-k Result: " + result);
            System.out.println("----------------------------------");
        }
        ResultsLogger.log(name, result, runtime, memUsed);
    }

    private static List<Integer> limitK(List<Integer> result, int k) {
        return result.stream().sorted().limit(k).toList();
    }
}