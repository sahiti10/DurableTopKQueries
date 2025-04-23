package durabletopk;
import java.util.*;
 
public class SamplingDurableTopK {
 private TreeMap<Integer, List<TemporalObject>> index;
     public SamplingDurableTopK(List<TemporalObject> objects) {
         index = new TreeMap<>();
         for (TemporalObject obj : objects) {
             for (Map.Entry<Integer, Double> entry : obj.timeSeries.entrySet()) {
                 int time = entry.getKey();
                 index.computeIfAbsent(time, x -> new ArrayList<>()).add(obj);
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
             List<TemporalObject> snapshot = index.getOrDefault(t, Collections.emptyList());
             snapshot.sort((a, b) -> Double.compare(b.getValueAt(t), a.getValueAt(t)));
             for (int i = 0; i < Math.min(k, snapshot.size()); i++) {
                 int id = snapshot.get(i).id;
                 countMap.put(id, countMap.getOrDefault(id, 0) + 1);
             }
         }
         List<Integer> result = new ArrayList<>();
         for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
             double ratio = (double) entry.getValue() / sampleSize;
             if (ratio >= tau) {
                 result.add(entry.getKey());
             }
            }
        return result;
     }
 }
