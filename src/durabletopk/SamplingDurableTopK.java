/**
 * SamplingDurableTopK
 * ---------------------
 * Implements randomized sampling algorithm for approximate durable top-k.
 *
 * This method samples a subset of timestamps and estimates object durability.
 *
 * Time Complexity:
 * - Indexing: O(1)
 * - Query: O(S log k), where S = number of sampled timestamps
 */

package durabletopk;

import java.util.*;

public class SamplingDurableTopK {
    private final TreeMap<Integer, List<TemporalObject>> index = new TreeMap<>();

    public SamplingDurableTopK(List<TemporalObject> objects) {
        for (TemporalObject obj : objects) {
            for (Map.Entry<Integer, Double> entry : obj.timeSeries.entrySet()) {
                index.computeIfAbsent(entry.getKey(), x -> new ArrayList<>()).add(obj);
            }
        }
    }

    public List<Integer> query(int k, int startTime, int endTime, double tau, int sampleSize) {
        Random rand = new Random();
        List<Integer> sampledTimes = new ArrayList<>();

        for (int i = 0; i < sampleSize; i++) {
            int t = startTime + rand.nextInt(endTime - startTime + 1);
            sampledTimes.add(t);
        }

        Map<Integer, Integer> countMap = new HashMap<>();
        for (int t : sampledTimes) {
            final int currentTime = t; // make lambda-safe
            List<TemporalObject> snapshot = index.getOrDefault(currentTime, Collections.emptyList());
            snapshot.sort((a, b) -> Double.compare(b.getValueAt(currentTime), a.getValueAt(currentTime)));

            for (int i = 0; i < Math.min(k, snapshot.size()); i++) {
                int id = snapshot.get(i).id;
                countMap.put(id, countMap.getOrDefault(id, 0) + 1);
            }
        }

        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
            if ((double) entry.getValue() / sampleSize >= tau) {
                result.add(entry.getKey());
            }
        }
        return result;
    }
}
