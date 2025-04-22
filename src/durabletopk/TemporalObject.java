/**
 * TemporalObject
 * -----------------
 * Represents a temporal object with an ID and time-series values.
 * Each object maintains a map of timestamp to score/value, allowing retrieval of its value at any point in time.
 * Used as the core data model across all algorithms.
 */

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
        return timeSeries.getOrDefault(time, Double.NEGATIVE_INFINITY);
    }
}