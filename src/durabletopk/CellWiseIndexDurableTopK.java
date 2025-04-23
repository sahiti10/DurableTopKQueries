/**
 * CellWiseIndexDurableTopK-> Actual core algo of our paper
 * ---------------------------
 * Implements a two-step greedy approximation algorithm for top-k queries with durability.
 * Builds a durability matrix for various k-values and selects the best one during query time.
 */

 package durabletopk;

 import java.util.*;
 
 public class CellWiseIndexDurableTopK {
     private int[][] durabilityMatrix; 
     private int[][] bestApproxKMap;   
     private int totalTime;            
     private int[] indexedKs;          
     private int maxObjectId;          
 
     public CellWiseIndexDurableTopK(List<TemporalObject> objects, List<Integer> ks, int totalTime) {
         this.totalTime = totalTime;
         Collections.sort(ks);
         this.indexedKs = new int[ks.size()];
         for (int i = 0; i < ks.size(); i++) {
             this.indexedKs[i] = ks.get(i);
         }
         maxObjectId = 0;
         for (TemporalObject obj : objects) {
             if (obj.id > maxObjectId) maxObjectId = obj.id;
         }
         durabilityMatrix = new int[maxObjectId + 1][indexedKs.length];
         bestApproxKMap = new int[maxObjectId + 1][51]; 

         TreeMap<Integer, List<TemporalObject>> timeMap = new TreeMap<>();
         for (TemporalObject obj : objects) {
             for (Map.Entry<Integer, Double> entry : obj.timeSeries.entrySet()) {
                 int time = entry.getKey();
                 if (!timeMap.containsKey(time)) {
                     timeMap.put(time, new ArrayList<>());
                 }
                 timeMap.get(time).add(obj);
             }
         }
         for (int time : timeMap.keySet()) {
             List<TemporalObject> snapshot = timeMap.get(time);
             snapshot.sort((a, b) -> Double.compare(b.getValueAt(time), a.getValueAt(time)));
 
             for (int ki = 0; ki < indexedKs.length; ki++) {
                 int k = indexedKs[ki];
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
 
                 for (int ki = 0; ki < indexedKs.length; ki++) {
                     int k = indexedKs[ki];
                     if (k > qk){
                        continue;}
                     double dur = (double) durabilityMatrix[id][ki] / totalTime;
                     double expectedDur = (double) qk / 50;
                     double error = Math.abs(dur - expectedDur);
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
         int qk = Math.min(k, 50);
         for (int id = 0; id <= maxObjectId; id++) {
             int ki = bestApproxKMap[id][qk];
             if (indexedKs[ki] > k){
                continue;}
             int count = durabilityMatrix[id][ki];
             double dur = (double) count / totalTime;
             if (dur >= tau) {
                 result.add(id);
             }
            }
        return result;
     }
 }
 