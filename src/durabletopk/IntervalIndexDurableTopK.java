package durabletopk;
import java.util.*;
 
public class IntervalIndexDurableTopK {
    private Map<Integer, List<Integer>> topKTimes;
 
    public IntervalIndexDurableTopK(List<TemporalObject> objects, int k) {
        topKTimes = new HashMap<>();
        TreeMap<Integer, List<TemporalObject>> timeMap = new TreeMap<>();
        for (TemporalObject obj : objects) {
            for (Map.Entry<Integer, Double> entry : obj.timeSeries.entrySet()) {
                int t = entry.getKey();
                timeMap.computeIfAbsent(t, x -> new ArrayList<>()).add(obj);
            }
        }
        for (Map.Entry<Integer, List<TemporalObject>> entry : timeMap.entrySet()) {
            final int currentTime = entry.getKey();
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
                if (t >= startTime && t <= endTime) {
                    count++;
                }
            }
            if ((double) count / duration >= tau) { result.add(entry.getKey());}
        }
        return result;
    }
}
 
