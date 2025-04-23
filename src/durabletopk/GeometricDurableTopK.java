package durabletopk;
import java.util.*;
 
public class GeometricDurableTopK {
    private TreeMap<Integer, List<TemporalObject>> timeIndex;
    private int k;
 
    public GeometricDurableTopK(List<TemporalObject> objects, int k) {
        this.k = k;
        this.timeIndex = new TreeMap<>();
 
        for (TemporalObject obj : objects) {
            for (Map.Entry<Integer, Double> entry : obj.timeSeries.entrySet()) {
                 int time = entry.getKey();
                 timeIndex.computeIfAbsent(time, t -> new ArrayList<>()).add(obj);
            }
        }
    }
    
    public List<Integer> query(int startTime, int endTime, double tau) {
        Map<Integer, Integer> countMap = new HashMap<>();
        int duration = endTime - startTime + 1;
        for (int t = startTime; t <= endTime; t++) {
            final int currentTime = t;
            List<TemporalObject> snapshot = timeIndex.getOrDefault(currentTime, new ArrayList<>());
            snapshot.sort((o1, o2) -> Double.compare(o2.getValueAt(currentTime), o1.getValueAt(currentTime)));
             for (int i = 0; i < Math.min(k, snapshot.size()); i++) {
                 int id = snapshot.get(i).id;
                 countMap.put(id, countMap.getOrDefault(id, 0) + 1);
             }
         }
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
            if ((double) entry.getValue() / duration >= tau) { result.add(entry.getKey());}
        }
        return result;
    }
}
 