package durabletopk;
import java.util.*;

public class TemporalObject {
    public int id;  
    public Map<Integer, Double> timeSeries; 
    public TemporalObject(int id) {
        this.id = id;  
        this.timeSeries = new HashMap<>();  
    }
    public void addValue(int time, double value) {
        timeSeries.put(time, value); 
    }
    public double getValueAt(int time) {
        // If the timestamp is not present, return negative infinity so it's ignored in top-k
        return timeSeries.getOrDefault(time, Double.NEGATIVE_INFINITY);
    }
}