package durabletopk;

import java.util.*;

/**
 Core class to compute and query durability-based Top-K indexes for temporal objects over a series of time snapshots.
 */
public class CellWiseIndexDurableTopK {
    private int[][] durabilityMatrix;
    private int[][] bestKApproxMap;
    private int totalTimestamps;
    private int[] topKValues;
    private int maxObjectId;

    /*
    Initializes the index using a list of temporal objects, target K values, and total time points.
     */
    public CellWiseIndexDurableTopK(List<TemporalObject> objects, List<Integer> kList, int totalT) {
        if (objects == null || kList == null || totalT <= 0) {
            throw new IllegalArgumentException("Invalid input to DurableTopK constructor.");
        }
        this.totalTimestamps = totalT;
        Collections.sort(kList);
        topKValues = new int[kList.size()];
        for (int i = 0; i < kList.size(); i++) {
            topKValues[i] = kList.get(i);
        }
        maxObjectId = objects.stream().mapToInt(obj -> obj.id).max().orElse(0); // Determine max object ID to size arrays
        durabilityMatrix = new int[maxObjectId + 1][topKValues.length];
        bestKApproxMap = new int[maxObjectId + 1][51]; // For query-k values from 1 to 50
        TreeMap<Integer, List<TemporalObject>> snapshotMap = new TreeMap<>();
        for (TemporalObject obj : objects) {
            for (Map.Entry<Integer, Double> entry : obj.timeSeries.entrySet()) {
                int timestamp = entry.getKey();
                snapshotMap.computeIfAbsent(timestamp, k -> new ArrayList<>()).add(obj);
            }
        }
        // Count how often each object appears in the top-K at each time point
        for (int timestamp : snapshotMap.keySet()) {
            List<TemporalObject> snapshot = snapshotMap.get(timestamp);
            // Sort objects by value at current timestamp in descending order
            snapshot.sort((a, b) -> Double.compare(b.getValueAt(timestamp), a.getValueAt(timestamp)));
            for (int kIndex = 0; kIndex < topKValues.length; kIndex++) {
                int k = topKValues[kIndex];
                int limit = Math.min(k, snapshot.size());
                for (int i = 0; i < limit; i++) {
                    int objectId = snapshot.get(i).id;
                    durabilityMatrix[objectId][kIndex]++;
                }
            }
        }

        // Precompute the best approximate k-index for any query-k from 1 to 50
        for (int objectId = 0; objectId <= maxObjectId; objectId++) {
            for (int queryK = 1; queryK <= 50; queryK++) {
                double bestError = Double.MAX_VALUE;
                int bestKIndex = -1;
                for (int kIndex = 0; kIndex < topKValues.length; kIndex++) {
                    int k = topKValues[kIndex];
                    if (k > queryK) continue;
                    double actualDurability = (double) durabilityMatrix[objectId][kIndex] / totalTimestamps;
                    double targetDurability = (double) queryK / 50.0;
                    double error = Math.abs(actualDurability - targetDurability);
                    if (error < bestError) {
                        bestError = error;
                        bestKIndex = kIndex;
                    }
                }
                bestKApproxMap[objectId][queryK] = bestKIndex;
            }
        }
    }

    /*
    Returns a list of object IDs that have durability >= tau for a given k value.
    */
    public List<Integer> query(int k, double tau) {
        if (k <= 0 || tau < 0 || tau > 1) {
            throw new IllegalArgumentException("Invalid query parameters!!");
        }
        List<Integer> resultIds = new ArrayList<>();
        int queryK = Math.min(k, 50); // Limit query-k to 50 for approximation
        for (int objectId = 0; objectId <= maxObjectId; objectId++) {
            int kIndex = bestKApproxMap[objectId][queryK];
            if (kIndex < 0 || topKValues[kIndex] > k) continue;
            double dur = (double) durabilityMatrix[objectId][kIndex] / totalTimestamps;
            if (dur >= tau) {
                resultIds.add(objectId);
            }
        }
        return resultIds;
    }
}
