/**
 * CellWiseIndexDurableTopK
 * ---------------------------
 * Implements the greedy two-phase approximation algorithm CEL from Section 4.3.
 *
 * CEL precomputes a cell-level durability matrix and uses a best-k approximation map to answer queries efficiently.
 *
 * This is the core contribution of the paper and provides the best accuracy-runtime tradeoff.
 *
 * Time Complexity:
 * - Indexing: O(kT)
 * - Query: O(n)
 */

package durabletopk;

import java.util.*;

public class CellWiseIndexDurableTopK {
    private final int[][] durabilityMatrix; // [objectId][indexedKIndex]
    private final int[][] bestApproxKMap;   // [objectId][queryK] = best indexedK index
    private final int totalTime;
    private final int[] indexedKs;
    private final int maxObjectId;

    public CellWiseIndexDurableTopK(List<TemporalObject> objects, List<Integer> indexedKs, int totalTime) {
        this.totalTime = totalTime;
        this.indexedKs = indexedKs.stream().sorted().mapToInt(i -> i).toArray();
        this.maxObjectId = objects.stream().mapToInt(o -> o.id).max().orElse(0);

        durabilityMatrix = new int[maxObjectId + 1][this.indexedKs.length];
        bestApproxKMap = new int[maxObjectId + 1][51];

        TreeMap<Integer, List<TemporalObject>> timeIndex = new TreeMap<>();
        for (TemporalObject obj : objects) {
            for (Map.Entry<Integer, Double> entry : obj.timeSeries.entrySet()) {
                timeIndex.computeIfAbsent(entry.getKey(), x -> new ArrayList<>()).add(obj);
            }
        }

        for (int time : timeIndex.keySet()) {
            final int currentTime = time;
            List<TemporalObject> snapshot = timeIndex.get(currentTime);
            snapshot.sort((a, b) -> Double.compare(b.getValueAt(currentTime), a.getValueAt(currentTime)));

            for (int ki = 0; ki < this.indexedKs.length; ki++) {
                int k = this.indexedKs[ki];
                for (int i = 0; i < Math.min(k, snapshot.size()); i++) {
                    int id = snapshot.get(i).id;
                    durabilityMatrix[id][ki]++;
                }
            }
        }

        for (int id = 0; id <= maxObjectId; id++) {
            for (int qk = 1; qk <= 50; qk++) {
                double minError = Double.MAX_VALUE;
                int bestKi = 0;
                for (int ki = 0; ki < this.indexedKs.length; ki++) {
                    int k = this.indexedKs[ki];
                    if (k > qk) continue;
                    double dur = (double) durabilityMatrix[id][ki] / totalTime;
                    double error = Math.abs(dur - ((double) qk / 50));
                    if (error < minError) {
                        minError = error;
                        bestKi = ki;
                    }
                }
                bestApproxKMap[id][qk] = bestKi;
            }
        }
    }

    public List<Integer> query(int k, double tau) {
        List<Integer> result = new ArrayList<>();
        int kIndex = Arrays.binarySearch(indexedKs, k);
        if (kIndex < 0) kIndex = -(kIndex + 1);

        for (int id = 0; id <= maxObjectId; id++) {
            int bestKi = bestApproxKMap[id][Math.min(k, 50)];
            if (indexedKs[bestKi] > k) continue;
            int count = durabilityMatrix[id][bestKi];
            double durability = (double) count / totalTime;
            if (durability >= tau) {
                result.add(id);
            }
        }

        return result;
    }
}