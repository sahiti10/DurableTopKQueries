/**
 * IntervalIndexDurableTopK
 * --------------------------
 * Implements interval index based algorithm as discussed in Section 3.2.
 *
 * It indexes the timestamps when each object appears in the top-k,and uses interval filtering at query time.
 *
 * Time Complexity:
 * - Indexing: O(T log k)
 * - Query: O(n log T)
 */

package durabletopk;

import java.util.*;

public class IntervalIndexDurableTopK {
    private final Map<Integer, List<Integer>> topKTimes = new HashMap<>();

    public IntervalIndexDurableTopK(List<TemporalObject> objects, int k) {
        TreeMap<Integer, List<TemporalObject>> timeMap = new TreeMap<>();

        // Build time-wise map of objects
        for (TemporalObject obj : objects) {
            for (Map.Entry<Integer, Double> entry : obj.timeSeries.entrySet()) {
                int t = entry.getKey();
                timeMap.computeIfAbsent(t, x -> new ArrayList<>()).add(obj);
            }
        }

        // For each time point, get top-k objects and index their time
        for (Map.Entry<Integer, List<TemporalObject>> entry : timeMap.entrySet()) {
            final int currentTime = entry.getKey(); // Fix for lambda
            List<TemporalObject> snapshot = entry.getValue();
            snapshot.sort((a, b) -> Double.compare(b.getValueAt(currentTime), a.getValueAt(currentTime)));

            for (int i = 0; i < Math.min(k, snapshot.size()); i++) {
                int id = snapshot.get(i).id;
                topKTimes.computeIfAbsent(id, x -> new ArrayList<>()).add(currentTime);
            }
        }
    }

    public List<Integer> query(int startTime, int endTime, double tau) {
        List<Integer> result = new ArrayList<>();
        int duration = endTime - startTime + 1;

        for (Map.Entry<Integer, List<Integer>> entry : topKTimes.entrySet()) {
            int count = 0;
            for (int t : entry.getValue()) {
                if (t >= startTime && t <= endTime) count++;
            }
            if ((double) count / duration >= tau) {
                result.add(entry.getKey());
            }
        }
        return result;
    }
}
