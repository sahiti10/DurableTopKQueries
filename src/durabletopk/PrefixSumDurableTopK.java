package durabletopk;
import java.util.*;
 
public class PrefixSumDurableTopK {
    private final Map<Integer, int[]> prefixMap = new HashMap<>();
 
    public PrefixSumDurableTopK(List<TemporalObject> objects, int k, int startTime, int endTime) {
        TreeMap<Integer, List<TemporalObject>> timeMap = new TreeMap<>();
        Map<Integer, int[]> counts = new HashMap<>();
        for (TemporalObject obj : objects) {
            for (Map.Entry<Integer, Double> entry : obj.timeSeries.entrySet()) {
                int t = entry.getKey();
                if (t >= startTime && t <= endTime){
                    timeMap.computeIfAbsent(t, x -> new ArrayList<>()).add(obj);}
            }
         }
        for (int t = startTime; t <= endTime; t++) {
            final int currentTime = t;
            List<TemporalObject> snapshot = timeMap.getOrDefault(currentTime, new ArrayList<>());
            snapshot.sort((a, b) -> Double.compare(b.getValueAt(currentTime), a.getValueAt(currentTime)));
            for (int i = 0; i < Math.min(k, snapshot.size()); i++) {
                int id = snapshot.get(i).id;
                counts.computeIfAbsent(id, x -> new int[endTime + 2])[currentTime]++;
            }
        }
        for (Map.Entry<Integer, int[]> entry : counts.entrySet()) {
            int[] p = entry.getValue();
            for (int i = startTime + 1; i <= endTime; i++) {
                p[i] += p[i - 1];
            }
            prefixMap.put(entry.getKey(), p);
        }
    }
 
    public List<Integer> query(int startTime, int endTime, double tau) {
        List<Integer> result = new ArrayList<>();
        int duration = endTime - startTime + 1;
        for (Map.Entry<Integer, int[]> entry : prefixMap.entrySet()) {
            int[] prefix = entry.getValue();
            int count = prefix[endTime] - prefix[startTime - 1];
            if ((double) count / duration >= tau) {
                result.add(entry.getKey());
            }
        }
        return result;}
}