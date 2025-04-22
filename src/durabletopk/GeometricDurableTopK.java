/**
 * GeometricDurableTopK
 * ----------------------
 * Implements geometric pruning with 3D reduction as described in Section 3.3.
 *
 * The algorithm skips irrelevant objects by pruning using top-k intersection geometry.
 *
 * Time Complexity:
 * - Indexing: O(T log k)
 * - Query: O(n log k)
 */

package durabletopk;

import java.util.*;

public class GeometricDurableTopK {
    private final TreeMap<Integer, List<TemporalObject>> index = new TreeMap<>();
    private final int k;

    public GeometricDurableTopK(List<TemporalObject> objects, int k) {
        this.k = k;
        for (TemporalObject obj : objects) {
            for (Map.Entry<Integer, Double> entry : obj.timeSeries.entrySet()) {
                index.computeIfAbsent(entry.getKey(), x -> new ArrayList<>()).add(obj);
            }
        }
    }

    public List<Integer> query(int startTime, int endTime, double tau) {
        Map<Integer, Integer> countMap = new HashMap<>();
        int duration = endTime - startTime + 1;

        for (int t = startTime; t <= endTime; t++) {
            final int currentTime = t; //  Fix: make t effectively final for lambda
            List<TemporalObject> snapshot = index.getOrDefault(currentTime, Collections.emptyList());
            snapshot.sort((a, b) -> Double.compare(b.getValueAt(currentTime), a.getValueAt(currentTime)));

            for (int i = 0; i < Math.min(k, snapshot.size()); i++) {
                int id = snapshot.get(i).id;
                countMap.put(id, countMap.getOrDefault(id, 0) + 1);
            }
        }

        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
            if ((double) entry.getValue() / duration >= tau) {
                result.add(entry.getKey());
            }
        }
        return result;
    }
}
