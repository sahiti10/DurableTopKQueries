package durabletopk;

import java.util.*;
 
public class ObliviousIndexDurableTopK {
     private Map<Integer, Map<Integer, List<Integer>>> topKAtTime;
     private List<Integer> indexedKs;
 
     public ObliviousIndexDurableTopK(List<TemporalObject> objects, List<Integer> ks) {
         this.topKAtTime = new HashMap<>();
         this.indexedKs = ks;
         TreeMap<Integer, List<TemporalObject>> timeIndex = new TreeMap<>();
         for (TemporalObject obj : objects) {
             for (Map.Entry<Integer, Double> entry : obj.timeSeries.entrySet()) {
                 timeIndex.computeIfAbsent(entry.getKey(), x -> new ArrayList<>()).add(obj);
             }
         }
         for (int k : ks) {
             Map<Integer, List<Integer>> timeToTopK = new HashMap<>();
             for (int time : timeIndex.keySet()) {
                 final int currentTime = time;
                 List<TemporalObject> snapshot = timeIndex.get(currentTime);
                 snapshot.sort((a, b) -> Double.compare(b.getValueAt(currentTime), a.getValueAt(currentTime)));
                 List<Integer> topKList = new ArrayList<>();
                 for (int i = 0; i < Math.min(k, snapshot.size()); i++) {
                     topKList.add(snapshot.get(i).id);
                 }
                 timeToTopK.put(time, topKList);
             }
             topKAtTime.put(k, timeToTopK);
         }
     }
 
     public List<Integer> query(int k, int startTime, int endTime, double tau) {
         int nearestK = findClosestIndexedK(k);
         Map<Integer, List<Integer>> topKTimes = topKAtTime.get(nearestK);
         Map<Integer, Integer> count = new HashMap<>();
         int duration = endTime - startTime + 1;
         for (int t = startTime; t <= endTime; t++) {
             List<Integer> topKList = topKTimes.getOrDefault(t, Collections.emptyList());
             for (int id : topKList) {
                 count.put(id, count.getOrDefault(id, 0) + 1);
             }
         }
         List<Integer> result = new ArrayList<>();
         for (Map.Entry<Integer, Integer> entry : count.entrySet()) {
             if ((double) entry.getValue() / duration >= tau) {
                 result.add(entry.getKey());
             }
         }
         return result;
     }
 
     private int findClosestIndexedK(int k) {
         int minDiff = Integer.MAX_VALUE;
         int bestK = indexedKs.get(0);
         for (int idxK : indexedKs) {
             int diff = Math.abs(k - idxK);
             if (diff < minDiff) {
                 minDiff = diff;
                 bestK = idxK;
             }
         }
         return bestK;
     }
 }
