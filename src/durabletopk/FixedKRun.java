package durabletopk;

import java.util.*;
import java.util.function.Supplier;

public class FixedKRun {
    public static void main(String[] args) throws Exception {
        String demoDataset = "data/dense_stock_synthetic.csv";
        runAllAlgorithms("DEMO", demoDataset, true, true);

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

        List<TemporalObject> objects = LoadCSVData.loadFromCSV(filePath);

        int totalTime = objects.stream()
                .flatMap(obj -> obj.timeSeries.keySet().stream())
                .mapToInt(t -> t)
                .max()
                .orElse(0);

        int k = 10;
        double tau = 0.05;
        int startTime = 1;
        int endTime = Math.min(1000, totalTime);
        List<Integer> indexedKs = Arrays.asList(5, 10, 15, 20);

        System.out.println("Config: start=" + startTime + ", end=" + endTime + ", k=" + k + ", tau=" + tau);
        System.out.println("------------------------------------------------");

        runWithMetrics("PrefixSum", timeIt, () -> {
            PrefixSumDurableTopK prefix = new PrefixSumDurableTopK(objects, k, startTime, endTime);
            return prefix.query(startTime, endTime, tau).stream().limit(k).toList();
        }, printResults);

        runWithMetrics("IntervalIndex", timeIt, () -> {
            IntervalIndexDurableTopK interval = new IntervalIndexDurableTopK(objects, k);
            return interval.query(startTime, endTime, tau).stream().limit(k).toList();
        }, printResults);

        runWithMetrics("Geometric", timeIt, () -> {
            GeometricDurableTopK geometric = new GeometricDurableTopK(objects, k);
            return geometric.query(startTime, endTime, tau).stream().limit(k).toList();
        }, printResults);

        runWithMetrics("Sampling", timeIt, () -> {
            SamplingDurableTopK sampling = new SamplingDurableTopK(objects);
            return sampling.query(k, startTime, endTime, tau, 20).stream().limit(k).toList();
        }, printResults);

        runWithMetrics("ObliviousIndex", timeIt, () -> {
            ObliviousIndexDurableTopK dos = new ObliviousIndexDurableTopK(objects, indexedKs);
            return dos.query(k, startTime, endTime, tau).stream().limit(k).toList();
        }, printResults);

        runWithMetrics("ColumnIndex", timeIt, () -> {
            ColumnIndexDurableTopK col = new ColumnIndexDurableTopK(objects, indexedKs, totalTime);
            return col.query(k, tau).stream().limit(k).toList();
        }, printResults);

        runWithMetrics("CellWiseIndex", timeIt, () -> {
            CellWiseIndexDurableTopK cel = new CellWiseIndexDurableTopK(objects, indexedKs, totalTime);
            return cel.query(k, tau).stream().limit(k).toList();
        }, printResults);
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
}
