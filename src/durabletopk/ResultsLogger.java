/**
 * ResultsLogger
 * ----------------
 * Utility class for collecting, analyzing, and exporting runtime statistics of durable top-k algorithms.
 *
 * Logs:
 * - Algorithm name
 * - Top-k result size
 * - Runtime in milliseconds
 * - Memory usage in megabytes
 * - Precision and recall vs. PrefixSum (baseline)
 */

 package durabletopk;

 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.*;
 
 public class ResultsLogger {
     private static final List<String> headers = Arrays.asList(
         "Algorithm", "TopKCount", "Runtime(ms)", "MemoryUsed(MB)", "Precision_vs_PrefixSum", "Recall_vs_PrefixSum"
     );
     private static final List<Map<String, Object>> records = new ArrayList<>();
     private static List<Integer> baselineResult = new ArrayList<>();
 
     public static void log(String algo, List<Integer> result, long runtime, long memoryUsed) {
         Set<Integer> current = new HashSet<>(result);
         Set<Integer> baseline = new HashSet<>(baselineResult);
 
         double precision = (current.size() == 0) ? 0.0 :
                 baseline.isEmpty() ? 0.0 : current.stream().filter(baseline::contains).count() / (double) current.size();
         double recall = (baseline.size() == 0) ? 0.0 :
                 current.stream().filter(baseline::contains).count() / (double) baseline.size();
 
         Map<String, Object> row = new LinkedHashMap<>();
         row.put("Algorithm", algo);
         row.put("TopKCount", result.size());
         row.put("Runtime(ms)", runtime);
         row.put("MemoryUsed(MB)", memoryUsed / (1024 * 1024));
         row.put("Precision_vs_PrefixSum", String.format("%.2f", precision));
         row.put("Recall_vs_PrefixSum", String.format("%.2f", recall));
         records.add(row);
 
         if (algo.equals("PrefixSum")) {
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
         } catch (IOException e) {
             System.err.println("Failed to write results CSV: " + e.getMessage());
         }
     }
 }