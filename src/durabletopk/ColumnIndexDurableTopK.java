/**
 * ColumnIndexDurableTopK
 * -------------------------
 * Implements column-wise index algorithm (COL) described in Section 4.2.
 *
 * Stores durability information per object per k-value and uses nearest neighbor logic.
 *
 * Time Complexity:
 * - Indexing: O(kT)
 * - Query: O(n)
 */

 package durabletopk;

import java.util.*;

public class ColumnIndexDurableTopK {
    private final Map<Integer, Map<Integer, Integer>> objectDurability;
    private final List<Integer> indexedKs;
    private final int totalTime;

    public ColumnIndexDurableTopK(List<TemporalObject> objects, List<Integer> ks, int totalTime) {
        this.objectDurability = new HashMap<>();
        this.indexedKs = ks;
        this.totalTime = totalTime;

        TreeMap<Integer, List<TemporalObject>> timeIndex = new TreeMap<>();
        for (TemporalObject obj : objects) {
            for (Map.Entry<Integer, Double> entry : obj.timeSeries.entrySet()) {
                timeIndex.computeIfAbsent(entry.getKey(), x -> new ArrayList<>()).add(obj);
            }
        }

        for (int time : timeIndex.keySet()) {
            final int currentTime = time;
            List<TemporalObject> snapshot = timeIndex.get(currentTime);
            snapshot.sort((o1, o2) -> Double.compare(o2.getValueAt(currentTime), o1.getValueAt(currentTime)));

            for (int k : indexedKs) {
                for (int i = 0; i < Math.min(k, snapshot.size()); i++) {
                    int id = snapshot.get(i).id;
                    Map<Integer, Integer> map = objectDurability.computeIfAbsent(id, x -> new HashMap<>());
                    map.put(k, map.getOrDefault(k, 0) + 1);
                }
            }
        }
    }

    public List<Integer> query(int k, double tau) {
        int bestK = findBestApproximateK(k);
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : objectDurability.entrySet()) {
            int id = entry.getKey();
            int timeInTopK = entry.getValue().getOrDefault(bestK, 0);
            if ((double) timeInTopK / totalTime >= tau) {
                result.add(id);
            }
        }
        return result;
    }

    private int findBestApproximateK(int k) {
        int minDiff = Integer.MAX_VALUE;
        int best = indexedKs.get(0);
        for (int idxK : indexedKs) {
            int diff = Math.abs(k - idxK);
            if (diff < minDiff) {
                minDiff = diff;
                best = idxK;
            }
        }
        return best;
    }
}
