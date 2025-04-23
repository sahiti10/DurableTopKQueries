package durabletopk;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ResultsLogger {
    private static final List<String> headers = Arrays.asList(
        "Algorithm", "TopKCount", "Runtime(ms)", "MemoryUsed(MB)",
        "Precision_vs_PrefixSum", "Recall_vs_PrefixSum", "F1_score"
    );
    private static final List<Map<String, Object>> records = new ArrayList<>();
    private static List<Integer> baselineResult = new ArrayList<>();
    private static double bestF1 = 0;
    private static int bestObjectId = -1;
    public static void log(String algo, List<Integer> result, long runtime, long memoryUsed) {
        Set<Integer> current = new HashSet<>(result);
        Set<Integer> baseline = new HashSet<>(baselineResult);
        double truePositives = current.stream().filter(baseline::contains).count();
        double precision = current.isEmpty() ? 0.0 : truePositives / current.size();
        double recall = baseline.isEmpty() ? 0.0 : truePositives / baseline.size();
        double f1 = (precision + recall > 0) ? 2 * (precision * recall) / (precision + recall) : 0.0;
        if (f1 > bestF1) {
            bestF1 = f1;
            bestObjectId = result.stream().filter(baseline::contains).findFirst().orElse(-1);
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("Algorithm", algo);
        row.put("TopKCount", result.size());
        row.put("Runtime(ms)", runtime);
        row.put("MemoryUsed(MB)", memoryUsed / (1024 * 1024));
        row.put("Precision_vs_PrefixSum", String.format("%.2f", precision));
        row.put("Recall_vs_PrefixSum", String.format("%.2f", recall));
        row.put("F1_score", String.format("%.2f", f1));
        records.add(row);
        if (algo.startsWith("PrefixSum")) {
            baselineResult = result;
        }
    }

    public static void exportCSV(String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(String.join(",", headers) + "\n");
            for (Map<String, Object> row : records) {
                for (String header : headers) {
                    writer.write(row.getOrDefault(header, "").toString());
                    writer.write(",");
                }
                writer.write("\n");
            }
            System.out.println("Exported results to " + filename);
            System.out.println("-------------------------------------------");
            System.out.println("Best F1 Score: " + String.format("%.2f", bestF1));
            if (bestObjectId != -1) {
                System.out.println("Representative Top-k Object ID (Highest F1): " + bestObjectId);
            }
        } catch (IOException e) {
            System.err.println("Failed to write results CSV: " + e.getMessage());
        }
    }
}